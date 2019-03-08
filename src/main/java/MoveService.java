import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import models.Board;
import models.Color;
import models.Move;
import models.Position;
import models.pieces.Bishop;
import models.pieces.King;
import models.pieces.Knight;
import models.pieces.Pawn;
import models.pieces.Piece;
import models.pieces.Queen;
import models.pieces.Rook;

public class MoveService {
	public List<Move> computeMoves(Board board, Piece piece, int posX, int posY) {
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
		} else {
			throw new RuntimeException("Unexpected piece type");
		}

		return moves.stream().filter(move -> {
			Board boardAfterMove = board.clone();
			boardAfterMove.doMove(move);

			// Checking opponent's king
			move.setChecking(isInCheck(boardAfterMove, swap(color)));

			return isValidSituation(boardAfterMove, color);
		}).collect(Collectors.toList());
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
		return moves;
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
		return computeDiagonalMoves(piece, posX, posY, board, 8);
	}

	private List<Move> computeDiagonalMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
		List<Move> moves = new ArrayList<>();
		// right up /
		for (int i = 0; i < Math.min(Math.min(7-posX, 7-posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i+1, i+1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// right down \
		for (int i = 0; i < Math.min(Math.min(7-posX, posY), maxDistance); i++) {
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
		for (int i = 0; i < Math.min(Math.min(posX, 7-posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i-1, i+1, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		return moves;
	}

	private List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board) {
		return computeStraightMoves(piece, posX, posY, board, 8);
	}

	private List<Move> computeStraightMoves(Piece piece, int posX, int posY, Board board, int maxDistance) {
		List<Move> moves = new ArrayList<>();
		// up
		for (int i = 0; i < Math.min(7-posY, maxDistance); i++) {
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
		for (int i = 0; i < Math.min(7-posX, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i+1, 0, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		return moves;
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
						move.setTaking(true);
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
					move.setTaking(true);
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
		return move.getToX() > 7 || move.getToY() > 7 || move.getToX() < 0 || move.getToY() < 0;
	}

	private boolean isValidSituation(Board boardAfterMove, Color color) {
		// check kings mutual distance
		Optional<Position> king1 = findKingPosition(boardAfterMove, color);
		Optional<Position> king2 = findKingPosition(boardAfterMove, swap(color));
		if (!king1.isPresent() || !king2.isPresent()) {
			// cannot eat king (would mean being checkmate)
			return false;
		}
		if (Math.min(Math.abs(king1.get().getX() - king2.get().getX()), Math.abs(king1.get().getY() - king2.get().getY())) <= 1) {
			return false;
		}

		return !isInCheck(boardAfterMove, color);
	}

	private boolean isInCheck(Board board, Color color) {
		final Position kingPosition = findKingPosition(board, color).orElseThrow(() -> new RuntimeException("King not found"));

		return isInStraightCheck(board, kingPosition, color)
			|| isInDiagonalCheck(board, kingPosition, color)
			|| isInLCheck(board, kingPosition, color)
			|| isInPawnCheck(board, kingPosition, color);
	}

	private boolean isInPawnCheck(Board board, Position kingPosition, Color color) {
		int factor = color == Color.WHITE ? -1 : 1;
		int y = kingPosition.getY() + factor;
		if (y >= 0 && y < 8) {
			List<Piece> destinations = new ArrayList<>();
			if (kingPosition.getX() > 1) {
				board.getPiece(kingPosition.getX() - 1, y).ifPresent(destinations::add);
			}
			if (kingPosition.getX() < 7) {
				board.getPiece(kingPosition.getX() + 1, y).ifPresent(destinations::add);
			}
			return destinations.stream()
				.anyMatch(destination -> destination.getColor() != color && destination instanceof Pawn);
		}
		return false;
	}

	private boolean isInLCheck(Board board, Position kingPosition, Color color) {
		final Piece fakeKnight = new Knight(color);
		return computeDiagonalMoves(fakeKnight, kingPosition.getX(), kingPosition.getY(), board).stream()
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

	private Optional<Position> findKingPosition(Board board, Color color) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
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

	private Color swap(Color color) {
		if (color == Color.WHITE) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}
}
