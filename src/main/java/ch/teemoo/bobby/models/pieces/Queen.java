package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

public class Queen extends Piece {
    public Queen(Color color) {
        super(color, 10);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2655";
        } else {
            return "\u265B";
        }
    }

    @Override
    public Piece copy() {
        Piece clone = new Queen(color);
		clone.setId(id);
		return clone;
	}
}
