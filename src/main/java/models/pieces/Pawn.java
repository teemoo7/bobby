package models.pieces;

import models.Color;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(color, 1);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2659";
        } else {
            return "P";
            //return "\u265F";
        }
    }

    @Override
    public Piece clone() {
        Piece clone = new Pawn(color);
		clone.setId(id);
		return clone;
	}
}
