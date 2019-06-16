package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

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
    public Piece copy() {
        Piece clone = new Bishop(color);
        clone.setId(id);
        return clone;
    }
}
