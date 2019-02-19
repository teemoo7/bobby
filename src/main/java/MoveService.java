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

			// try one forward
			moves.add(new Move(piece, posX, posY, posX, posY + factor));

			// try two forward if initial position
			if (posY == initialY) {
				moves.add(new Move(piece, posX, posY, posX, posY + 2 * factor));
			}
		} else if (piece instanceof Knight) {
			moves.add(new Move(piece, posX, posY, posX + 1, posY + 2));
			moves.add(new Move(piece, posX, posY, posX + 1, posY - 2));
			moves.add(new Move(piece, posX, posY, posX - 1, posY + 2));
			moves.add(new Move(piece, posX, posY, posX - 1, posY - 2));
			moves.add(new Move(piece, posX, posY, posX + 2, posY + 1));
			moves.add(new Move(piece, posX, posY, posX + 2, posY - 1));
			moves.add(new Move(piece, posX, posY, posX - 2, posY + 1));
			moves.add(new Move(piece, posX, posY, posX - 2, posY - 1));
		} else if (piece instanceof Bishop) {
			moves.addAll(computeBishopMoves(piece, posX, posY));
		} else if (piece instanceof Rook) {
			moves.addAll(computeRookMoves(piece, posX, posY));
		} else if (piece instanceof Queen) {
			moves.addAll(computeRookMoves(piece, posX, posY));
			moves.addAll(computeBishopMoves(piece, posX, posY));
		} else {
			// king
			moves.add(new Move(piece, posX, posY, posX + 1, posY));
			moves.add(new Move(piece, posX, posY, posX - 1, posY));
			moves.add(new Move(piece, posX, posY, posX, posY + 1));
			moves.add(new Move(piece, posX, posY, posX, posY - 1));
			moves.add(new Move(piece, posX, posY, posX + 1, posY + 1));
			moves.add(new Move(piece, posX, posY, posX + 1, posY - 1));
			moves.add(new Move(piece, posX, posY, posX - 1, posY - 1));
			moves.add(new Move(piece, posX, posY, posX - 1, posY + 1));
		}

		return moves.stream().filter(move -> isValidMove(move, board, piece)).collect(Collectors.toList());
	}

	private List<Move> computeBishopMoves(Piece piece, int posX, int posY) {
		List<Move> moves = new ArrayList<>();
		// right up /
		int distanceMax = Math.min(7-posX, 7-posY);
		for (int i = 0; i < distanceMax; i++) {
			moves.add(new Move(piece, posX, posY, posX + i, posY + i));
		}
		// right down \
		distanceMax = Math.min(7-posX, posY);
		for (int i = 0; i < distanceMax; i++) {
			moves.add(new Move(piece, posX, posY, posX + i, posY - i));
		}
		// left down /
		distanceMax = Math.min(posX, posY);
		for (int i = 0; i < distanceMax; i++) {
			moves.add(new Move(piece, posX, posY, posX - i, posY - i));
		}
		// left up \
		distanceMax = Math.min(posX, 7-posY);
		for (int i = 0; i < distanceMax; i++) {
			moves.add(new Move(piece, posX, posY, posX - i, posY + i));
		}
		return moves;
	}

	private List<Move> computeRookMoves(Piece piece, int posX, int posY) {
		List<Move> moves = new ArrayList<>();
		// up
		for (int i = 0; i < 7-posY; i++) {
			moves.add(new Move(piece, posX, posY, posX, posY + i));
		}
		// down
		for (int i = 0; i < posY; i++) {
			moves.add(new Move(piece, posX, posY, posX, posY - i));
		}
		// left
		for (int i = 0; i < posX; i++) {
			moves.add(new Move(piece, posX, posY, posX - i, posY));
		}
		// right
		for (int i = 0; i < 7-posX; i++) {
			moves.add(new Move(piece, posX, posY, posX + i, posY));
		}
		return moves;
	}

	private boolean isValidMove(Move move, Board board, Piece piece) {
		// Board boundaries
		if (move.getToX() > 7 || move.getToY() > 7 || move.getToX() < 0 || move.getToY() < 0) {
			return false;
		}

		// Check room on destination square
		Optional<Piece> destinationPiece = board.getPiece(move.getToX(), move.getToY());
		if (destinationPiece.isPresent()) {
			// Same color means move not possible
			if (destinationPiece.get().getColor() == piece.getColor()) {
				return false;
			} else {
				// Check if destination piece can be taken
				if (piece instanceof Pawn) {
					// Pawn only takes in diagonal
					if (move.getFromX() == move.getToX()) {
						return false;
					}
				}
			}
		} else {
			// Empty destination

			//todo: check kings mutual distance
		}
		// compute board after move
		Board boardAfterMove = board.clone();
		boardAfterMove.withMove(move);
		return !isInCheck(boardAfterMove, piece.getColor());
	}

	private boolean isInCheck(Board board, Color color) {
		final Position kingPosition = findKingPosition(board, color).orElseThrow(() -> new RuntimeException("No more king!"));
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

	private Optional<Position> findKingPosition(Board board, Color color) {
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				Optional<Piece> pieceOpt = board.getPiece(x, y);
				if (pieceOpt.isPresent()) {
					Piece piece = pieceOpt.get();
					if (piece.getColor() == color && piece instanceof King) {
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
