package models.pieces;

import models.Color;

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
    public Piece clone() {
        return new Knight(color);
    }
}
