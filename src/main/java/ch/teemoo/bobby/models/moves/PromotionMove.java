package ch.teemoo.bobby.models.moves;

import ch.teemoo.bobby.models.pieces.Piece;

public class PromotionMove extends Move {
    private final Piece promotedPiece;

    public PromotionMove(Move move, Piece promotedPiece) {
        super(move.getPiece(), move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        this.promotedPiece = promotedPiece;
        setChecking(move.isChecking());
        setTookPiece(move.getTookPiece());
    }

    public Piece getPromotedPiece() {
        return promotedPiece;
    }

    public String getPrettyNotation() {
        return super.getPrettyNotation() + " (promoted to " + getPromotedPiece().getClass().getSimpleName() + ")";
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
