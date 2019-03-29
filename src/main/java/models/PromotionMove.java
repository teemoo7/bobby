package models;

import models.pieces.Piece;

public class PromotionMove extends Move {
    private final Piece promotedPiece;

    public PromotionMove(Move move, Piece promotedPiece) {
        super(move.getPiece(), move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        this.promotedPiece = promotedPiece;
        setChecking(move.isChecking());
        setTaking(move.isTaking());
    }

    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    public String getPrettyNotation() {
        return super.getPrettyNotation() + " (promoted to " + getPromotedPiece().getClass().getSimpleName() + ")";
    }
}
