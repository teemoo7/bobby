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
    public Piece copy() {
        return new King(color);
	}
}
