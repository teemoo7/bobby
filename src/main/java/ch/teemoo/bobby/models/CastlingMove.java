package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.pieces.Piece;

public class CastlingMove extends Move {
    private final Piece rook;
    private final int rookFromX;
    private final int rookFromY;
    private final int rookToX;
    private final int rookToY;

    public CastlingMove(Piece king, int fromX, int fromY, int toX, int toY, Piece rook, int rookFromX, int rookFromY, int rookToX, int rookToY) {
        super(king, fromX, fromY, toX, toY);
        this.rook = rook;
        this.rookFromX = rookFromX;
        this.rookFromY = rookFromY;
        this.rookToX = rookToX;
        this.rookToY = rookToY;
    }

    public Piece getRook() {
        return rook;
    }

    public int getRookFromX() {
        return rookFromX;
    }

    public int getRookFromY() {
        return rookFromY;
    }

    public int getRookToX() {
        return rookToX;
    }

    public int getRookToY() {
        return rookToY;
    }

    @Override
    public String getBasicNotation() {
        String notation;
        if (rookFromX == 0) {
            notation = "0-0-0";
        } else if (rookFromX == 7) {
            notation = "0-0";
        } else {
            throw new RuntimeException("Unexpected rook position");
        }
        if (isChecking()) {
            notation = notation + "+";
        }
        return notation;
    }
}
