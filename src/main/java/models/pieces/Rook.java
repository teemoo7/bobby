package models.pieces;

import models.Color;

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
    public Piece clone() {
        Piece clone = new Rook(color);
		clone.setId(id);
		return clone;
	}
}
