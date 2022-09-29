package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.moves.PromotionMove;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PromotionMoveTest {

    @Test
    public void testInit() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece queen = new Queen(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        move.setChecking(true);
        move.setTookPiece(new Bishop(Color.BLACK));
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

    @Test
    public void testGetUciNotationQueen() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece queen = new Queen(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, queen);
        assertThat(promotionMove.getUciNotation()).isEqualTo("e7e8q");
    }

    @Test
    public void testGetUciNotationRook() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, rook);
        assertThat(promotionMove.getUciNotation()).isEqualTo("e7e8r");
    }

    @Test
    public void testGetUciNotationBishop() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece bishop = new Bishop(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, bishop);
        assertThat(promotionMove.getUciNotation()).isEqualTo("e7e8b");
    }

    @Test
    public void testGetUciNotationKnight() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece knight = new Knight(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, knight);
        assertThat(promotionMove.getUciNotation()).isEqualTo("e7e8n");
    }

    @Test
    public void testGetUciNotationInvalidPiece() {
        Piece pawn = new Pawn(Color.WHITE);
        Piece king = new King(Color.WHITE);
        Move move = new Move(pawn, 4, 6, 4, 7);
        PromotionMove promotionMove = new PromotionMove(move, king);
        ThrowableAssert.ThrowingCallable callable = promotionMove::getUciNotation;
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(callable).withMessage("Unexpected promoted piece");
    }
}
