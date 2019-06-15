package ch.teemoo.bobby.services;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;
import static ch.teemoo.bobby.models.Board.SIZE;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.*;
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

	private final static int MAX_MOVE = SIZE - 1;
	private final static int WORST = -1000;
	private final static int BEST = 1000;
	private final static int NEUTRAL = 0;
	private final static int[][] heatmapCenter = generateCenteredHeatmap();

	public List<Move> computeAllMoves(Board board, Color color, boolean withAdditionalInfo) {
		return computeBoardMoves(board, color, withAdditionalInfo, false);
	}

	public List<Move> computeMoves(Board board, Piece piece, int posX, int posY, boolean withAdditionalInfo) {
		List<Move> moves = new ArrayList<>();
		final Color color = piece.getColor();

		if (piece instanceof Pawn) {
			moves.addAll(computePawnMoves(piece, posX, posY, board));
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
			moves.addAll(computeCastlingMoves(piece, posX, posY, board));
		} else {
			throw new RuntimeException("Unexpected piece type");
		}

		if (withAdditionalInfo) {
			return moves.stream().filter(move -> {
				Board boardAfterMove = board.clone();
				boardAfterMove.doMove(move);

				// Checking opponent's king
				move.setChecking(isInCheck(boardAfterMove, swap(color)));

				return isValidSituation(boardAfterMove, color);
			}).collect(Collectors.toList());
		} else {
			return moves;
		}
	}

	public GameState getGameState(Board board, Color colorToPlay, List<Move> history) {
		if (!canMove(board, colorToPlay)) {
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

	public Optional<Position> findKingPosition(Board board, Color color) {
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

	public Move selectMove(Game game, int depth) {
		return selectMove(game.getBoard(), game.getToPlay(), game.getHistory(), depth);
	}

	private Move selectMove(Board board, Color color, List<Move> history, int depth) {
		// Evaluate each move given the points of the pieces and the checkmate possibility, then select highest

		List<Move> moves = computeAllMoves(board, color, true);
		Map<Move, Integer> moveScores = new ConcurrentHashMap<>(moves.size());

		final Color opponentColor = swap(color);
		final Position opponentKingOriginalPosition = findKingPosition(board, opponentColor)
				.orElseThrow(() -> new RuntimeException("King expected here"));

		for(Move move: moves) {
			Position opponentKingPosition = opponentKingOriginalPosition;
			Board boardAfter = board.clone();
			boardAfter.doMove(move);
			List<Move> historyCopy = new ArrayList<>(history);
			historyCopy.add(move);
			final GameState gameState = getGameState(boardAfter, opponentColor, historyCopy);

			int score = evaluateBoard(boardAfter, color, color, gameState, opponentKingPosition);
			if (score >= BEST) {
				return move;
			}

			// Compute the probable next move for the opponent and see if our current move is a real benefit in the end
			if (depth >= 1 && gameState.isInProgress()) {
				Move opponentMove = selectMove(boardAfter, opponentColor, historyCopy, depth-1);
				boardAfter.doMove(opponentMove);
				historyCopy.add(opponentMove);
				if (opponentMove.getPiece() instanceof King) {
					// We must consider the current king position
					opponentKingPosition = new Position(opponentMove.getToX(), opponentMove.getToY());
				}
				final GameState gameStateAfterOpponent = getGameState(boardAfter, color, historyCopy);
				score = evaluateBoard(boardAfter, color, opponentColor, gameStateAfterOpponent, opponentKingPosition);

				if (depth >= 2 && gameStateAfterOpponent.isInProgress()) {
					//todo: determine which pieces move must be evaluated to reduce computation time
					Move nextMove = selectMove(boardAfter, color, historyCopy, depth - 2);
					boardAfter.doMove(nextMove);
					historyCopy.add(nextMove);
					final GameState gameStateAfterOpponentAfterMove = getGameState(boardAfter, opponentColor, historyCopy);
					score = evaluateBoard(boardAfter, color, color, gameStateAfterOpponentAfterMove, opponentKingPosition);
				}
			}
			moveScores.put(move, score);
		}
		if (depth == 2) {
			//todo: for debugging
			logger.debug(moveScores.entrySet().stream()
					.sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(e -> e.getKey().toString() + "=" + e.getValue().toString()).collect(
							Collectors.joining(", ")));
		}
		return getBestMove(moveScores);
	}

	private int evaluateBoard(Board board, Color colorToEvaluate, Color lastPlayer, GameState gameState, Position opponentKingPosition) {
		int gameStateScore = NEUTRAL;
		if (!gameState.isInProgress()) {
			// Game is over
			if (gameState.isLost()) {
				if (lastPlayer == colorToEvaluate) {
					// Opponent is checkmate, that the best move to do!
					gameStateScore = BEST;
				} else {
					// I am checkmate, that the worst move to do!
					gameStateScore = WORST;
				}
			} else if (gameState.isDraw()) {
				// Let us be aggressive, a draw is not a good move, we want to win
				gameStateScore -= 20;
			}
			return gameStateScore;
		}

		// Basically, taking a piece improves your situation
		int piecesValue = getPiecesValueSum(board, colorToEvaluate);
		int opponentPiecesValue = getPiecesValueSum(board, swap(colorToEvaluate));
		int piecesScore = piecesValue-opponentPiecesValue;

		//fixme: we should compute moves for pawns in case of taking, not straight moves
		List<Move> allMoves = computeAllMoves(board, colorToEvaluate, false);
		int[][] heatmapOpponentKing = getHeatmapAroundLocation(opponentKingPosition.getX(), opponentKingPosition.getY());
		int heatScore = allMoves.stream().mapToInt(
				m -> heatmapCenter[m.getToX()][m.getToY()] + heatmapOpponentKing[m.getToX()][m.getToY()]).sum();

		return gameStateScore + 10 * piecesScore + heatScore;
	}

	private int getPiecesValueSum(Board board, Color color) {
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

	private Move getBestMove(Map<Move, Integer> moveScores) {
		return getMaxScoreWithRandomChoice(moveScores)
				.orElseThrow(() -> new RuntimeException("At least one move must be done"));
	}

	private Optional<Move> getMaxScoreWithRandomChoice(Map<Move, Integer> moveScores) {
		// Instead of just search for the max score, we search for all moves that have the max score, and if there are
		// more than one move, then we randomly choose one. It shall give a bit of variation in games.
		if (moveScores.isEmpty()) {
			return Optional.empty();
		}
		List<Move> bestMoves = new ArrayList<>();
		Integer highestScore = null;
		for (Map.Entry<Move, Integer> entry: moveScores.entrySet()) {
			if (highestScore == null || entry.getValue() > highestScore) {
				highestScore = entry.getValue();
				bestMoves.clear();
				bestMoves.add(entry.getKey());
			} else if (highestScore.intValue() == entry.getValue()) {
				bestMoves.add(entry.getKey());
			}
		}
		return Optional.of(bestMoves.get(new Random().nextInt(bestMoves.size())));
	}

	private boolean canMove(Board board, Color color) {
		List<Move> moves = computeBoardMoves(board, color, true, true);
		return !moves.isEmpty();
	}

	private List<Move> computeBoardMoves(Board board, Color color, boolean withAdditionalInfo, boolean returnFirstPieceMoves) {
		List<Move> moves = new ArrayList<>();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				Optional<Piece> piece = board.getPiece(i, j);
				if (piece.isPresent() && piece.get().getColor() == color) {
					List<Move> pieceMoves = computeMoves(board, piece.get(), i, j, withAdditionalInfo);
					if (!pieceMoves.isEmpty() && returnFirstPieceMoves) {
						return pieceMoves;
					}
					moves.addAll(pieceMoves);
				}
			}
		}
		return moves;
	}

	private boolean isInCheck(Board board, Color color) {
		final Position kingPosition = findKingPosition(board, color).orElseThrow(() -> new RuntimeException("King not found"));

		return isInStraightCheck(board, kingPosition, color)
			|| isInDiagonalCheck(board, kingPosition, color)
			|| isInLCheck(board, kingPosition, color)
			|| isInPawnCheck(board, kingPosition, color);
	}

	private List<Move> computePawnMoves(Piece piece, int posX, int posY, Board board) {
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

		// try one forward diagonal (only for taking)
		Optional<Move> move3 = getAllowedMove(piece, posX, posY, -1, factor, board);
		move3.ifPresent(moves::add);
		Optional<Move> move4 = getAllowedMove(piece, posX, posY, 1, factor, board);
		move4.ifPresent(moves::add);

		return moves.stream().map(move -> {
			if (move.getToY() == initialY + factor * 6) {
				// Pawn promotion - simplified, always a queen
				return new PromotionMove(move, new Queen(move.getPiece().getColor()));
			} else {
				return move;
			}
		}).collect(Collectors.toList());
	}

	private List<Move> computeLShapeMoves(Piece piece, int posX, int posY, Board board) {
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

	private List<Move> computeDiagonalMoves(Piece piece, int posX, int posY, Board board) {
		return computeDiagonalMoves(piece, posX, posY, board, SIZE);
	}

	private List<Move> computeDiagonalMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
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

	private List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board) {
		return computeStraightMoves(piece, posX, posY, board, SIZE);
	}

	private List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
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

	private List<Move> computeCastlingMoves(Piece piece, int posX, int posY, Board board) {
		//todo: take game history into account, both king and rook must not have moved yet

		if (!isValidKingPositionForCastling(piece, posX, posY, board)) {
			return Collections.emptyList();
		}

		List<Move> moves = new ArrayList<>();

		// Queenside castling theoretical positions
		getCastlingMove(board, piece, posX, posY, 2, 0, 3).ifPresent(moves::add);
		// Kingside castling theoretical positions
		getCastlingMove(board, piece, posX, posY, 6, 7, 5).ifPresent(moves::add);

		return moves;
	}

	private Optional<Move> getCastlingMove(Board board, Piece piece, int kingFromX, int kingFromY, int kingToX,
		int rookFromX, int rookToX) {
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
			Board boardAfter = board.clone();
			boardAfter.doMove(new Move(piece, kingFromX, kingFromY, x, kingFromY));
			if (isInCheck(boardAfter, color)) {
				return Optional.empty();
			}
		}

		return Optional
			.of(new CastlingMove(piece, kingFromX, kingFromY, kingToX, kingFromY, rookOpt.get(), rookFromX, kingFromY,
				rookToX, kingFromY));
	}

	private boolean isValidKingPositionForCastling(Piece piece, int posX, int posY, Board board) {
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
		if (!(kingOpt.isPresent() && kingOpt.get().equals(piece))) {
			return false;
		}
		return !isInCheck(board, color);
	}

	private Optional<Move> getAllowedMove(Piece piece, int posX, int posY, int deltaX, int deltaY, Board board) {
		Move move = new Move(piece, posX, posY, posX + deltaX, posY + deltaY);
		if (isOutOfBounds(move)) {
			return Optional.empty();
		}
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
		return Optional.of(move);
	}

	private boolean isOutOfBounds(Move move) {
		// Board boundaries
		return move.getToX() > MAX_MOVE || move.getToY() > MAX_MOVE || move.getToX() < 0 || move.getToY() < 0;
	}

	private boolean isValidSituation(Board boardAfterMove, Color color) {
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

	private boolean isInPawnCheck(Board board, Position kingPosition, Color color) {
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

	private boolean isInLCheck(Board board, Position kingPosition, Color color) {
		final Piece fakeKnight = new Knight(color);
		return computeLShapeMoves(fakeKnight, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Knight;
		});
	}

	private boolean isInDiagonalCheck(Board board, Position kingPosition, Color color) {
		final Piece fakeBishop = new Bishop(color);
		return computeDiagonalMoves(fakeBishop, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Bishop || takenPiece instanceof Queen;
		});
	}

	private boolean isInStraightCheck(Board board, Position kingPosition, Color color) {
		Piece fakeRook = new Rook(color);
		return computeStraightMoves(fakeRook, kingPosition.getX(), kingPosition.getY(), board).stream()
			.filter(Move::isTaking).anyMatch(move -> {
			Piece takenPiece = board.getPiece(move.getToX(), move.getToY())
				.orElseThrow(() -> new RuntimeException("Cannot take an empty piece!"));
			return takenPiece instanceof Rook || takenPiece instanceof Queen;
		});
	}

	private int[][] getHeatmapAroundLocation(int x, int y) {
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

	private static int[][] generateCenteredHeatmap() {
		int[][] heatmap = new int[Board.SIZE][Board.SIZE];
		for (int i = 0; i < Board.SIZE; i++) {
			for (int j = 0; j < Board.SIZE; j++) {
				int heat = 0;
				if ((i == 3 || i == 4) && (j == 3 || j == 4)) {
					heat = 2;
				} else if ((i == 2 || i == 5) && (j == 2 || j == 5)) {
					heat = 1;
				}
				heatmap[i][j] = heat;
			}
		}
		return heatmap;
	}
}
