package ch.teemoo.bobby.services;

import static java.util.stream.Collectors.toList;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;
import static ch.teemoo.bobby.models.Board.SIZE;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.GameState;
import ch.teemoo.bobby.models.MoveAnalysis;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.moves.CastlingMove;
import ch.teemoo.bobby.models.moves.EnPassantMove;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.moves.PromotionMove;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoveService {
	private final static Logger logger = LoggerFactory.getLogger(MoveService.class);
	private final static Random RANDOM = new Random();

	public final static int WORST = -1000;
	public final static int BEST = 1000;
	public final static int NEUTRAL = 0;
	public final static int DRAW_PENALTY = -20;

	public final static int OPENING_MOVES_COUNT = 5;
	public final static int OPENING_MISTAKE_PENALTY = -10;
	public final static int KING_MOVE_MISTAKE_PENALTY = -20;
	public final static int CASTLING_BONUS = 15;

	private final static int MAX_MOVE = SIZE - 1;
	private final static int[][] heatmapCenter = generateCenteredHeatmap();

	public List<Move> computeAllMoves(Board board, Color color, List<Move> history, boolean withAdditionalInfo) {
		return computeAllMoves(board, color, history, withAdditionalInfo, false);
	}

	public List<Move> computeAllMoves(Board board, Color color, List<Move> history, boolean withAdditionalInfo,
		boolean takingMovesOnly) {
		return computeBoardMoves(board, color, history, withAdditionalInfo, false, takingMovesOnly);
	}

	public List<Move> computeMoves(Board board, Piece piece, int posX, int posY, List<Move> history,
		boolean withAdditionalInfo, boolean takingMovesOnly) {
		List<Move> moves = new ArrayList<>();
		final Color color = piece.getColor();

		if (piece instanceof Pawn) {
			moves.addAll(computePawnMoves(piece, posX, posY, board, history, takingMovesOnly));
		} else if (piece instanceof Knight) {
			moves.addAll(computeLShapeMoves(piece, posX, posY, board));
		} else if (piece instanceof Bishop) {
			moves.addAll(computeDiagonalMoves(piece, posX, posY, board));
		} else if (piece instanceof Rook) {
			moves.addAll(computeStraightMoves(piece, posX, posY, board));
		} else if (piece instanceof Queen) {
			moves.addAll(computeStraightMoves(piece, posX, posY, board));
			moves.addAll(computeDiagonalMoves(piece, posX, posY, board));
		} else if (piece instanceof King) {
			moves.addAll(computeStraightMoves(piece, posX, posY, board, 1));
			moves.addAll(computeDiagonalMoves(piece, posX, posY, board, 1));
			if (!takingMovesOnly) {
				moves.addAll(computeCastlingMoves(piece, posX, posY, board, history));
			}
		} else {
			throw new RuntimeException("Unexpected piece type");
		}

		if (withAdditionalInfo) {
			return moves.stream().filter(move -> {
				board.doMove(move);

				// Checking opponent's king
				move.setChecking(isInCheck(board, swap(color)));

				boolean valid = isValidSituation(board, color);
				board.undoMove(move);
				return valid;
			}).collect(toList());
		} else {
			return moves;
		}
	}

	public GameState getGameState(Board board, Color colorToPlay, List<Move> history) {
		if (!canMove(board, colorToPlay, history)) {
			if (isInCheck(board, colorToPlay)) {
				// Checkmate
				return GameState.LOSS;
			} else {
				// Stalemate
				return GameState.DRAW_STALEMATE;
			}
		}

		if (history.size() >= 10) {
			Move move6 = history.get(history.size()-1);
			Move move4 = history.get(history.size()-5);
			Move move2 = history.get(history.size()-9);
			Move move5 = history.get(history.size()-2);
			Move move3 = history.get(history.size()-6);
			Move move1 = history.get(history.size()-10);
			if (move6.equals(move4) && move6.equals(move2) && move5.equals(move3) && move5.equals(move1)) {
				// Threefold repetition
				return GameState.DRAW_THREEFOLD;
			}
		}

		if (history.size() >= 50) {
			List<Move> last50Moves = history.subList(history.size() - 50, history.size() - 1);
			if (last50Moves.stream().noneMatch(move -> move.isTaking() || move.getPiece() instanceof Pawn)) {
				// 50-move (no pawn moved, no capture)
				return GameState.DRAW_50_MOVES;
			}
		}

		return GameState.IN_PROGRESS;
	}

	public boolean isDrawAcceptable(Game game) {
		Board board = game.getBoard();
		Color opponentColor = game.getToPlay();
		Color color = swap(opponentColor);
		List<Move> history = game.getHistory();
		GameState gameState = getGameState(board, opponentColor, history);
		Position kingPosition =
			findKingPosition(board, color).orElseThrow(() -> new RuntimeException("King expected here"));
		Position opponentKingPosition =
			findKingPosition(board, opponentColor).orElseThrow(() -> new RuntimeException("King expected here"));
		int score = evaluateBoard(board, color, color, gameState, opponentKingPosition, kingPosition, history);
		int opponentScore = evaluateBoard(board, opponentColor, color, gameState, kingPosition, opponentKingPosition, history);
		return opponentScore + DRAW_PENALTY > score;
	}

	public Move selectMove(Game game, int depth, LocalDateTime computationTimeout) {
		MoveAnalysis moveAnalysis =
			selectMove(game.getBoard(), game.getToPlay(), game.getHistory(), depth, true, computationTimeout);
		return moveAnalysis.getMove();
	}

	private MoveAnalysis selectMove(Board board, Color color, List<Move> history, int depth, boolean isTopDepth,
		LocalDateTime computationTimeout) {
		// Evaluate each move given the points of the pieces and the checkmate possibility, then select highest

		List<Move> moves = computeAllMoves(board, color, history,true);

		final Color opponentColor = swap(color);
		final Position opponentKingPosition = findKingPosition(board, opponentColor)
				.orElseThrow(() -> new RuntimeException("King expected here"));
		final Position myKingOriginalPosition = findKingPosition(board, color)
				.orElseThrow(() -> new RuntimeException("King expected here"));

		Map<MoveAnalysis, Integer> moveScores = moves.stream().map(
			move -> computeMoveAnalysis(board, color, history, depth, opponentColor, opponentKingPosition,
				myKingOriginalPosition, move, computationTimeout))
			.collect(Collectors.toMap(Function.identity(), MoveAnalysis::getScore));

		if (isTopDepth) {
			logger.debug(moveScores.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(e -> e.getKey().getMove().toString() + "=" + e.getValue().toString()).collect(
							Collectors.joining(", ")));
		}
		return getBestMove(moveScores);
	}

	private MoveAnalysis computeMoveAnalysis(Board board, Color color, List<Move> history, int depth,
		Color opponentColor, Position opponentKingPosition, Position myKingOriginalPosition, Move move,
		LocalDateTime computationTimeout) {
		MoveAnalysis moveAnalysis = new MoveAnalysis(move);

		if (computationTimeout != null && computationTimeout.isBefore(LocalDateTime.now())) {
			// Timeout reached, returning default values for the move
			return moveAnalysis;
		}

		Position myKingPosition = myKingOriginalPosition;
		if (move.getPiece() instanceof King) {
			myKingPosition = new Position(move.getToX(), move.getToY());
		}
		Board boardAfter = board.copy();
		boardAfter.doMove(move);
		history.add(move);
		final GameState gameState = getGameState(boardAfter, opponentColor, history);

		int score = evaluateBoard(boardAfter, color, color, gameState, opponentKingPosition, myKingPosition, history);
		moveAnalysis.setScore(score);
		if (score >= BEST) {
			history.remove(move);
			return moveAnalysis;
		}

		// Compute the probable next move for the opponent and see if our current move is a real benefit in the end
		if (depth >= 1 && gameState.isInProgress()) {
			MoveAnalysis opponentMoveAnalysis =
				selectMove(boardAfter, opponentColor, history, depth - 1, false, computationTimeout);
			moveAnalysis.setScore(-opponentMoveAnalysis.getScore());
		}
		//board.undoMove(move);
		history.remove(move);
		return moveAnalysis;
	}

	Optional<Position> findKingPosition(Board board, Color color) {
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				Optional<Piece> pieceOpt = board.getPiece(x, y);
				if (pieceOpt.isPresent()) {
					Piece piece = pieceOpt.get();
					if (piece instanceof King && piece.getColor() == color) {
						return Optional.of(new Position(x, y));
					}
				}
			}
		}
		return Optional.empty();
	}

	int evaluateBoard(Board board, Color colorToEvaluate, Color lastPlayer, GameState gameState, Position opponentKingPosition, Position myKingPosition, List<Move> history) {
		if (!gameState.isInProgress()) {
			int gameStateScore = NEUTRAL;
			// Game is over
			if (gameState.isLost()) {
				if (lastPlayer == colorToEvaluate) {
					// Opponent is checkmate, that is the best move to do!
					gameStateScore = BEST;
				} else {
					// I am checkmate, that is the worst move to do!
					gameStateScore = WORST;
				}
			} else if (gameState.isDraw()) {
				// Let us be aggressive, a draw is not a good move, we want to win
				gameStateScore += DRAW_PENALTY;
			}
			return gameStateScore;
		}

		int piecesScore = getPiecesScore(board, colorToEvaluate);
		int heatScore = getHeatScore(board, colorToEvaluate, opponentKingPosition, myKingPosition, history);
		int developmentScore = getDevelopmentScore(colorToEvaluate, history);

		return 10 * piecesScore + heatScore + developmentScore;
	}

	private int getPiecesScore(Board board, Color color) {
		// Basically, taking a piece improves your situation
		int piecesValue = getPiecesValueSum(board, color);
		int opponentPiecesValue = getPiecesValueSum(board, swap(color));
		return piecesValue-opponentPiecesValue;
	}

	int getHeatScore(Board board, Color color, Position opponentKingPosition, Position myKingPosition,
		List<Move> history) {
		// Should focus the fire on the center of the board and around the opponent's king
		List<Move> allMoves = computeAllMoves(board, color, history,false, true);
		int[][] heatmapOpponentKing = getHeatmapAroundLocation(opponentKingPosition.getX(), opponentKingPosition.getY());
		int myHeatScore = allMoves.stream().mapToInt(
				m -> heatmapCenter[m.getToX()][m.getToY()] + heatmapOpponentKing[m.getToX()][m.getToY()]).sum();
		List<Move> allOpponentMoves = computeAllMoves(board, swap(color), history,false, true);
		int[][] heatmapMyKing = getHeatmapAroundLocation(myKingPosition.getX(), myKingPosition.getY());
		int opponentHeatScore = allOpponentMoves.stream().mapToInt(
				m -> heatmapCenter[m.getToX()][m.getToY()] + heatmapMyKing[m.getToX()][m.getToY()]).sum();
		return myHeatScore - opponentHeatScore;
	}

	int getDevelopmentScore(Color color, List<Move> history) {
		// Development strategy is key to avoid being late compared the opponent
		int myDevelopmentScore = getDevelopmentBonus(
			history.stream().filter(move -> move.getPiece().getColor() == color).collect(toList()));
		int opponentDevelopmentScore = getDevelopmentBonus(
			history.stream().filter(move -> move.getPiece().getColor() == swap(color)).collect(toList()));
		return myDevelopmentScore - opponentDevelopmentScore;
	}

	int getDevelopmentBonus(List<Move> myHistory) {
		int bonus = 0;
		if (myHistory.size() <= OPENING_MOVES_COUNT) {
			// Still in the opening
			List<Piece> openingPieces = myHistory.stream().filter(m -> !(m instanceof CastlingMove)).map(Move::getPiece)
				.filter(p -> !(p instanceof Pawn)).collect(toList());
			// Should not use a major piece for now
			if (openingPieces.stream().anyMatch(p -> p instanceof Queen || p instanceof Rook || p instanceof King)) {
				bonus += OPENING_MISTAKE_PENALTY;
			}
			// Should not move twice the same piece
			Set<Piece> distinctOpeningPieces = new HashSet<>(openingPieces);
			if (distinctOpeningPieces.size() != openingPieces.size()) {
				bonus += OPENING_MISTAKE_PENALTY;
			}
		}
		// Castling is always good to secure the King
		if (myHistory.stream().anyMatch(m -> m instanceof CastlingMove)) {
			bonus += CASTLING_BONUS;
		} else {
			// But any other king move drops the right for castling
			if (myHistory.stream().anyMatch(m -> m.getPiece() instanceof King)) {
				bonus += KING_MOVE_MISTAKE_PENALTY;
			}
		}
		return bonus;
	}

	int getPiecesValueSum(Board board, Color color) {
		int sum = 0;
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				Optional<Piece> pieceOpt = board.getPiece(i, j);
				if (pieceOpt.isPresent() && pieceOpt.get().getColor() == color) {
					sum += pieceOpt.get().getValue();
				}
			}
		}
		return sum;
	}

	MoveAnalysis getBestMove(Map<MoveAnalysis, Integer> moveScores) {
		return getMaxScoreWithRandomChoice(moveScores)
				.orElseThrow(() -> new RuntimeException("At least one move must be done"));
	}

	Optional<MoveAnalysis> getMaxScoreWithRandomChoice(Map<MoveAnalysis, Integer> moveScores) {
		// Instead of just search for the max score, we search for all moves that have the max score, and if there are
		// more than one move, then we randomly choose one. It shall give a bit of variation in games.
		if (moveScores.isEmpty()) {
			return Optional.empty();
		}
		List<MoveAnalysis> bestMoves = new ArrayList<>();
		Integer highestScore = null;
		for (Map.Entry<MoveAnalysis, Integer> entry: moveScores.entrySet()) {
			if (highestScore == null || entry.getValue() > highestScore) {
				highestScore = entry.getValue();
				bestMoves.clear();
				bestMoves.add(entry.getKey());
			} else if (highestScore.intValue() == entry.getValue()) {
				bestMoves.add(entry.getKey());
			}
		}
		return Optional.of(bestMoves.get(RANDOM.nextInt(bestMoves.size())));
	}

	boolean canMove(Board board, Color color, List<Move> history) {
		List<Move> moves = computeBoardMoves(board, color, history,true, true, false);
		return !moves.isEmpty();
	}

	List<Move> computeBoardMoves(Board board, Color color, List<Move> history, boolean withAdditionalInfo,
		boolean returnFirstPieceMoves, boolean takingMovesOnly) {
		List<Move> moves = new ArrayList<>();
		List<PiecePosition> piecePositions = new ArrayList<>();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				Optional<Piece> piece = board.getPiece(i, j);
				if (piece.isPresent() && piece.get().getColor() == color) {
					piecePositions.add(new PiecePosition(piece.get(), new Position(i, j)));
				}
			}
		}

		piecePositions.sort((p1, p2) -> p2.getPiece().getValue() - p1.getPiece().getValue());

		for (PiecePosition piecePosition: piecePositions) {
			List<Move> pieceMoves = computeMoves(board, piecePosition.getPiece(), piecePosition.getPosition().getX(),
				piecePosition.getPosition().getY(), history, withAdditionalInfo, takingMovesOnly);
			if (!pieceMoves.isEmpty() && returnFirstPieceMoves) {
				return pieceMoves;
			}
			moves.addAll(pieceMoves);
		}
		return moves;
	}

	boolean isInCheck(Board board, Color color) {
		final Position kingPosition = findKingPosition(board, color).orElseThrow(() -> new RuntimeException("King not found"));

		return isInStraightCheck(board, kingPosition, color)
			|| isInDiagonalCheck(board, kingPosition, color)
			|| isInLCheck(board, kingPosition, color)
			|| isInPawnCheck(board, kingPosition, color);
	}

	List<Move> computePawnMoves(Piece piece, int posX, int posY, Board board, List<Move> history,
		boolean takingMovesOnly) {
		List<Move> moves = new ArrayList<>();
		final Color color = piece.getColor();
		// color matters for pawns since they cannot go back
		int factor;
		int initialY;
		if (color == Color.WHITE) {
			factor = 1;
			initialY = 1; // second row
		} else {
			factor = -1;
			initialY = 6; // second to last row
		}

		if (!takingMovesOnly) {
			// try one forward (no taking)
			Optional<Move> move1 = getAllowedMove(piece, posX, posY, 0, factor, board);
			if (move1.isPresent()) {
				moves.add(move1.get());
				// try two forward if initial position (no taking)
				if (posY == initialY) {
					Optional<Move> move2 = getAllowedMove(piece, posX, posY, 0, 2 * factor, board);
					move2.ifPresent(moves::add);
				}
			}
		}

		// try one forward diagonal (only for taking)
		Optional<Move> move3 = getAllowedMove(piece, posX, posY, -1, factor, board, !takingMovesOnly);
		move3.ifPresent(moves::add);
		Optional<Move> move4 = getAllowedMove(piece, posX, posY, 1, factor, board, !takingMovesOnly);
		move4.ifPresent(moves::add);

		// en-passant moves
		if (!history.isEmpty()) {
			Move lastMove = history.get(history.size() - 1);
			if (lastMove.getPiece() instanceof Pawn && lastMove.getFromY() - lastMove.getToY() == (2 * factor)
				&& lastMove.getToY() == posY && (lastMove.getToX() == posX - 1 || lastMove.getToX() == posX + 1)) {
				EnPassantMove move =
					new EnPassantMove(new Move(piece, posX, posY, lastMove.getToX(), posY + factor), lastMove.getToX(),
						lastMove.getToY());
				move.setTookPiece(board.getPiece(lastMove.getToX(), lastMove.getToY())
					.orElseThrow(() -> new RuntimeException("En-passant move expects a piece here")));
				moves.add(move);
			}
		}

		// promotion special moves
		List<Move> movesWithPromotion = new ArrayList<>();
		moves.forEach(move -> {
			if (move.getToY() == initialY + factor * 6) {
				movesWithPromotion.add(new PromotionMove(move, new Queen(move.getPiece().getColor())));
				movesWithPromotion.add(new PromotionMove(move, new Knight(move.getPiece().getColor())));
				movesWithPromotion.add(new PromotionMove(move, new Bishop(move.getPiece().getColor())));
				movesWithPromotion.add(new PromotionMove(move, new Rook(move.getPiece().getColor())));
			} else {
				movesWithPromotion.add(move);
			}
		});
		return movesWithPromotion;
	}

	List<Move> computeLShapeMoves(Piece piece, int posX, int posY, Board board) {
		List<Move> moves = new ArrayList<>();
		getAllowedMove(piece, posX, posY, 1, 2, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, 1, -2, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, -1, 2, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, -1, -2, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, 2, 1, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, 2, -1, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, -2, 1, board).ifPresent(moves::add);
		getAllowedMove(piece, posX, posY, -2, -1, board).ifPresent(moves::add);
		return moves;
	}

	List<Move> computeDiagonalMoves(Piece piece, int posX, int posY, Board board) {
		return computeDiagonalMoves(piece, posX, posY, board, SIZE);
	}

	List<Move> computeDiagonalMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
		List<Move> moves = new ArrayList<>();
		// right up /
		for (int i = 0; i < Math.min(Math.min(MAX_MOVE-posX, MAX_MOVE-posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i+1, i+1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// right down \
		for (int i = 0; i < Math.min(Math.min(MAX_MOVE-posX, posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i+1, -i-1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left down /
		for (int i = 0; i < Math.min(Math.min(posX, posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i-1, -i-1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left up \
		for (int i = 0; i < Math.min(Math.min(posX, MAX_MOVE-posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i-1, i+1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		return moves;
	}

	List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board) {
		return computeStraightMoves(piece, posX, posY, board, SIZE);
	}

	List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
		List<Move> moves = new ArrayList<>();
		// up
		for (int i = 0; i < Math.min(MAX_MOVE-posY, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, 0, i+1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// down
		for (int i = 0; i < Math.min(posY, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, 0, -i-1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left
		for (int i = 0; i < Math.min(posX, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i-1, 0, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// right
		for (int i = 0; i < Math.min(MAX_MOVE-posX, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i+1, 0, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		return moves;
	}

	List<Move> computeCastlingMoves(Piece piece, int posX, int posY, Board board, List<Move> history) {
		if (!isValidKingPositionForCastling(piece, posX, posY, board)) {
			return Collections.emptyList();
		}

		List<Move> moves = new ArrayList<>();

		// Queenside castling theoretical positions
		getCastlingMove(board, piece, posX, posY, 2, 0, 3, history).ifPresent(moves::add);
		// Kingside castling theoretical positions
		getCastlingMove(board, piece, posX, posY, 6, 7, 5, history).ifPresent(moves::add);

		return moves;
	}

	Optional<Move> getCastlingMove(Board board, Piece piece, int kingFromX, int kingFromY, int kingToX,
		int rookFromX, int rookToX, List<Move> history) {

		if (history.stream().anyMatch(m ->
			(m.getFromX() == kingFromX && m.getFromY() == kingFromY)
				|| (m.getFromX() == rookFromX && m.getFromY() == kingFromY))) {
			return Optional.empty();
		}

		final Color color = piece.getColor();

		// Check rook position
		Optional<Piece> rookOpt = board.getPiece(rookFromX, kingFromY);
		if (!(rookOpt.isPresent() && rookOpt.get() instanceof Rook && rookOpt.get().getColor() == color)) {
			return Optional.empty();
		}

		// Check room between rook and king
		for (int x = Math.min(rookFromX, kingFromX) + 1; x < Math.max(kingFromX, rookFromX); x++) {
			Optional<Piece> pieceBetween = board.getPiece(x, kingFromY);
			if (pieceBetween.isPresent()) {
				return Optional.empty();
			}
		}

		// Check that king does not cross fire during move
		for (int x = Math.min(kingToX, kingFromX + 1); x < Math.max(kingFromX, kingToX + 1); x++) {
			Move move = new Move(piece, kingFromX, kingFromY, x, kingFromY);
			board.doMove(move);
			boolean inCheck = isInCheck(board, color);
			board.undoMove(move);
			if (inCheck) {
				return Optional.empty();
			}
		}

		return Optional
			.of(new CastlingMove(piece, kingFromX, kingFromY, kingToX, kingFromY, rookOpt.get(), rookFromX, kingFromY,
				rookToX, kingFromY));
	}

	boolean isValidKingPositionForCastling(Piece piece, int posX, int posY, Board board) {
		final Color color = piece.getColor();
		if (posX != 4) {
			return false;
		} else {
			if (color == Color.WHITE) {
				if (posY != 0) {
					return false;
				}
			} else {
				if (posY != 7) {
					return false;
				}
			}
		}
		Optional<Piece> kingOpt = board.getPiece(posX, posY);
		if (kingOpt.isEmpty() || !(kingOpt.get() instanceof King) || kingOpt.get().getColor() != color) {
			return false;
		}
		return !isInCheck(board, color);
	}

	Optional<Move> getAllowedMove(Piece piece, int posX, int posY, int deltaX, int deltaY, Board board) {
		return getAllowedMove(piece, posX, posY, deltaX, deltaY, board, true);
	}

	Optional<Move> getAllowedMove(Piece piece, int posX, int posY, int deltaX, int deltaY, Board board,
		boolean checkTakingDestPiece) {
		Move move = new Move(piece, posX, posY, posX + deltaX, posY + deltaY);
		if (isOutOfBounds(move)) {
			return Optional.empty();
		}
		if (checkTakingDestPiece) {
			Optional<Piece> destPiece = board.getPiece(move.getToX(), move.getToY());
			if (piece instanceof Pawn) {
				if (deltaX == 0) {
					// Move forward, taking is not allowed, dest must be free
					if (destPiece.isPresent()) {
						return Optional.empty();
					}
				} else {
					// Move diagonal, taking is mandatory, dest must be other color
					if (destPiece.isPresent()) {
						if (destPiece.get().getColor() != piece.getColor()) {
							move.setTookPiece(destPiece.get());
						} else {
							return Optional.empty();
						}
					} else {
						return Optional.empty();
					}
				}
			} else {
				if (destPiece.isPresent()) {
					if (destPiece.get().getColor() != piece.getColor()) {
						move.setTookPiece(destPiece.get());
					} else {
						// Same color
						return Optional.empty();
					}
				}
			}
		}
		return Optional.of(move);
	}

	boolean isOutOfBounds(Move move) {
		// Board boundaries
		return move.getToX() > MAX_MOVE || move.getToY() > MAX_MOVE || move.getToX() < 0 || move.getToY() < 0;
	}

	boolean isValidSituation(Board boardAfterMove, Color color) {
		// check kings mutual distance
		Optional<Position> king1 = findKingPosition(boardAfterMove, color);
		Optional<Position> king2 = findKingPosition(boardAfterMove, swap(color));
		if (!king1.isPresent() || !king2.isPresent()) {
			// cannot eat king (would mean being checkmate)
			return false;
		}
		if (Math.max(Math.abs(king1.get().getX() - king2.get().getX()), Math.abs(king1.get().getY() - king2.get().getY())) <= 1) {
			return false;
		}

		return !isInCheck(boardAfterMove, color);
	}

	boolean isInPawnCheck(Board board, Position kingPosition, Color color) {
		int factor = color == Color.BLACK ? -1 : 1;
		int y = kingPosition.getY() + factor;
		if (y >= 0 && y <= MAX_MOVE) {
			List<Piece> destinations = new ArrayList<>();
			if (kingPosition.getX() >= 1) {
				board.getPiece(kingPosition.getX() - 1, y).ifPresent(destinations::add);
			}
			if (kingPosition.getX() < MAX_MOVE) {
				board.getPiece(kingPosition.getX() + 1, y).ifPresent(destinations::add);
			}
			return destinations.stream()
				.anyMatch(destination -> destination.getColor() != color && destination instanceof Pawn);
		}
		return false;
	}

	boolean isInLCheck(Board board, Position kingPosition, Color color) {
		final Piece fakeKnight = new Knight(color);
		return computeLShapeMoves(fakeKnight, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Knight;
		});
	}

	boolean isInDiagonalCheck(Board board, Position kingPosition, Color color) {
		final Piece fakeBishop = new Bishop(color);
		return computeDiagonalMoves(fakeBishop, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Bishop || takenPiece instanceof Queen;
		});
	}

	boolean isInStraightCheck(Board board, Position kingPosition, Color color) {
		Piece fakeRook = new Rook(color);
		return computeStraightMoves(fakeRook, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Rook || takenPiece instanceof Queen;
		});
	}

	int[][] getHeatmapAroundLocation(int x, int y) {
		int[][] heatmap = new int[Board.SIZE][Board.SIZE];
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				int distanceToHeat = Math.max(Math.abs(x-i), Math.abs(y-j));
				int heat;
				switch (distanceToHeat) {
					case 0:
						heat = 3;
						break;
					case 1:
						heat = 2;
						break;
					case 2:
						heat = 1;
						break;
					default:
						heat = 0;
				}
				heatmap[i][j] = heat;
			}
		}
		return heatmap;
	}

	static int[][] generateCenteredHeatmap() {
		int[][] heatmap = new int[Board.SIZE][Board.SIZE];
		heatmap[3][3] = 2;
		heatmap[3][4] = 2;
		heatmap[4][3] = 2;
		heatmap[4][4] = 2;
		heatmap[2][3] = 1;
		heatmap[2][4] = 1;
		heatmap[5][3] = 1;
		heatmap[5][4] = 1;
		heatmap[3][2] = 1;
		heatmap[3][5] = 1;
		heatmap[4][2] = 1;
		heatmap[4][5] = 1;
		return heatmap;
	}

	private static class PiecePosition {
		private final Piece piece;
		private final Position position;

		public PiecePosition(Piece piece, Position position) {
			this.piece = piece;
			this.position = position;
		}

		public Piece getPiece() {
			return piece;
		}

		public Position getPosition() {
			return position;
		}
	}
}
