package models.pieces;

import models.Color;

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
        return new King(color);
    }
}
