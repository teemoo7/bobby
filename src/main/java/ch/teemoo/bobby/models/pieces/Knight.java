package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color, 3);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2658";
        } else {
            return "\u265E";
        }
    }

    @Override
    public Piece copy() {
        Piece clone = new Knight(color);
		clone.setId(id);
		return clone;
	}
}
