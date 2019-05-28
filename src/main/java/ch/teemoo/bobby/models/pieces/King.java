package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

public class King extends Piece {
    public King(Color color) {
        super(color, 100);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2654";
        } else {
            return "\u265A";
        }
    }

    @Override
    public Piece clone() {
        Piece clone = new King(color);
		clone.setId(id);
		return clone;
	}
}
