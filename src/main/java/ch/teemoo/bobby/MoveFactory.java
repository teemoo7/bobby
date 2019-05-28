package ch.teemoo.bobby;

import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Piece;

public class MoveFactory {
	private MoveFactory() {
	}

	public static Move createMove(Piece piece, Position from, Position to) {
		return new Move(piece, from.getX(), from.getY(), to.getX(), to.getY());
	}
}
