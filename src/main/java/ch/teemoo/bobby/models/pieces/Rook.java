package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color, 5);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2656";
        } else {
            return "\u265C";
        }
    }

    @Override
    public Piece copy() {
        return new Rook(color);
	}
}
