package ch.teemoo.bobby.models.moves;

import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;

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

    public String getUciNotation() {
        String promotionInfo;
        if (promotedPiece instanceof Queen) {
            promotionInfo = "q";
        } else if (promotedPiece instanceof Rook) {
            promotionInfo = "r";
        } else if (promotedPiece instanceof Knight) {
            promotionInfo = "n";
        } else if (promotedPiece instanceof Bishop) {
            promotionInfo = "b";
        } else {
            throw new RuntimeException("Unexpected promoted piece");
        }
        return super.getUciNotation() + promotionInfo;
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
