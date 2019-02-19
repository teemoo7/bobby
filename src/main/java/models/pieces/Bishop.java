package models.pieces;

import models.Color;

public class Bishop extends Piece {
    public Bishop(Color color) {
        super(color, 3);
    }

    @Override
    public String getUnicode() {
        if (color == Color.WHITE) {
            return "\u2657";
        } else {
            return "\u265D";
        }
    }

    @Override
    public Piece clone() {
        return new Bishop(color);
    }
}
