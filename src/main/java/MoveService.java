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
	public List<Move> computeMoves(Board board, Piece piece, int posX, int posY, boolean withCheck) {
		List<Move> moves = new ArrayList<>();

		if (piece instanceof Pawn) {
			// color matters for pawns since they cannot go back
			int factor;
			int initialY;
			if (piece.getColor() == Color.WHITE) {
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

		// Compute isUnderCheck / isChecking for each move

		return moves.stream().filter(move -> isValidMove(move, board, piece)).collect(Collectors.toList());
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
			Optional<Move> move = getAllowedMove(piece, posX, posY, i, i, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// right down \
		for (int i = 0; i < Math.min(Math.min(7-posX, posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i, -i, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left down /
		for (int i = 0; i < Math.min(Math.min(posX, posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i, -i, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left up \
		for (int i = 0; i < Math.min(Math.min(posX, 7-posY), maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i, i, board);
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
			Optional<Move> move = getAllowedMove(piece, posX, posY, 0, i, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// down
		for (int i = 0; i < Math.min(posY, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, 0, -i, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// left
		for (int i = 0; i < Math.min(posX, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, -i, 0, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		// right
		for (int i = 0; i < Math.min(7-posX, maxDistance); i++) {
			Optional<Move> move = getAllowedMove(piece, posX, posY, i, 0, board);
			move.ifPresent(moves::add);
			if (!move.isPresent() || move.get().isTaking()) {
				break;
			}
		}
		return moves;
	}

	private Optional<Move> getAllowedMove(Piece piece, int posX, int posY, int deltaX, int deltaY, Board board) {
		Move move = new Move(piece, posX, posY, posX + deltaX, posY + deltaY);
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

	private boolean isValidMove(Move move, Board board, Piece piece) {
		// Board boundaries
		if (move.getToX() > 7 || move.getToY() > 7 || move.getToX() < 0 || move.getToY() < 0) {
			return false;
		}

		// compute board after move
		Board boardAfterMove = board.clone();
		boardAfterMove.withMove(move);

		// check kings mutual distance
		Position king1 = findKingPosition(boardAfterMove, piece.getColor());
		Position king2 = findKingPosition(boardAfterMove, swap(piece.getColor()));
		if (Math.min(Math.abs(king1.getX() - king2.getX()), Math.abs(king1.getY() - king2.getY())) <= 1) {
			return false;
		}

		//todo: return !isInCheck(boardAfterMove, piece.getColor());
		return true;
	}

	private boolean isInCheck(Board board, Color color) {
		final Position kingPosition = findKingPosition(board, color);
		final Color otherColor = swap(color);

		//one naive implementation could be to compute all moves for all pieces and check if the destination of any move is where the king is, which would mean check
		// but it is way to expensive, so the preferred method here is to find the king and check the straight, diagonal and L moves
		// todo: implement smart method

		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				Optional<Piece> pieceOpt = board.getPiece(x, y);
				if (pieceOpt.isPresent()) {
					Piece piece = pieceOpt.get();
					if (piece.getColor() == otherColor) {
						List<Move> moves = computeMoves(board, piece, x, y, false);
						if (moves.stream().anyMatch(move -> move.getToX() == kingPosition.getX() && move.getToY() == kingPosition.getY())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private Position findKingPosition(Board board, Color color) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				Optional<Piece> pieceOpt = board.getPiece(x, y);
				if (pieceOpt.isPresent()) {
					Piece piece = pieceOpt.get();
					if (piece.getColor() == color && piece instanceof King) {
						return new Position(x, y);
					}
				}
			}
		}
		throw new RuntimeException("No more king!");
	}

	private Color swap(Color color) {
		if (color == Color.WHITE) {
			return Color.BLACK;
		} else {
			return Color.WHITE;
		}
	}
}
