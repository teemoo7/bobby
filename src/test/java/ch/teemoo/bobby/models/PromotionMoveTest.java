package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PromotionMoveTest {

    @Test
    public void testInit() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece queen = new Queen(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        move.setChecking(true);
        move.setTaking(true);
        PromotionMove promotionMove = new PromotionMove(move, queen);
        assertThat(promotionMove.getFromX()).isEqualTo(move.getFromX());
        assertThat(promotionMove.getFromY()).isEqualTo(move.getFromY());
        assertThat(promotionMove.getToX()).isEqualTo(move.getToX());
        assertThat(promotionMove.getToY()).isEqualTo(move.getToY());
        assertThat(promotionMove.getPiece()).isEqualTo(pawn);
        assertThat(promotionMove.getPromotedPiece()).isEqualTo(queen);
        assertThat(promotionMove.isChecking()).isTrue();
        assertThat(promotionMove.isTaking()).isTrue();
    }

    @Test
    public void testGetPrettyNotation() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece queen = new Queen(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, queen);
        assertThat(promotionMove.getPrettyNotation()).isEqualTo("WHITE e7-e8 (Pawn) (promoted to Queen)");
    }
}
