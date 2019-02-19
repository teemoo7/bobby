package models.pieces;

import models.Color;

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
    public Piece clone() {
        return new Queen(color);
    }
}
