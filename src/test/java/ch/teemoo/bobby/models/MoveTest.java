package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class MoveTest {

    @Test
    public void testMoveInit() {
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(rook, 0, 0, 0, 5);
        assertThat(move.getPiece()).isEqualTo(rook);
        assertThat(move.getFromX()).isEqualTo(0);
        assertThat(move.getFromY()).isEqualTo(0);
        assertThat(move.getToX()).isEqualTo(0);
        assertThat(move.getToY()).isEqualTo(5);
        assertThat(move.isTaking()).isFalse();
        assertThat(move.isChecking()).isFalse();
    }

    @Test
    public void testGetBasicNotation() {
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(rook, 0, 0, 0, 5);
        assertThat(move.getBasicNotation()).isEqualTo("a1-a6");
        move.setChecking(true);
        assertThat(move.getBasicNotation()).isEqualTo("a1-a6+");
    }

    @Test
    public void testGetBasicNotationCastling() {
        Piece rook = new Rook(Color.WHITE);
        Piece king = new King(Color.WHITE);
        Move move = new CastlingMove(king, 4, 0, 2, 0, rook, 0, 0, 3, 0);
        assertThat(move.getBasicNotation()).isEqualTo("0-0-0");
        move = new CastlingMove(king, 4, 0, 6, 0, rook, 7, 0, 5, 0);
        assertThat(move.getBasicNotation()).isEqualTo("0-0");
    }

    @Test
    public void testGetBasicNotationTaking() {
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(rook, 0, 0, 0, 5);
        move.setTookPiece(new Queen(Color.BLACK));
        assertThat(move.getBasicNotation()).isEqualTo("a1xa6");
        move.setChecking(true);
        assertThat(move.getBasicNotation()).isEqualTo("a1xa6+");
    }

    @Test
    public void testGetPrettyNotation() {
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(rook, 0, 0, 0, 5);
        assertThat(move.getPrettyNotation()).isEqualTo("WHITE a1-a6 (Rook)");
    }

    @Test
    public void testFromBasicNotation() {
        String notation = "a1-a6+";
        Move move = Move.fromBasicNotation(notation, Color.WHITE);
        assertThat(move.getBasicNotation()).isEqualTo(notation);
        assertThat(move.isChecking()).isTrue();
        assertThat(move.isTaking()).isFalse();
        assertThat(move.getPiece()).isNull();
        assertThat(move.getFromX()).isEqualTo(0);
        assertThat(move.getFromY()).isEqualTo(0);
        assertThat(move.getToX()).isEqualTo(0);
        assertThat(move.getToY()).isEqualTo(5);
    }

    @Test
    public void testFromBasicNotationTaking() {
        String notation = "a1xa6+";
        Move move = Move.fromBasicNotation(notation, Color.BLACK);
        assertThat(move.getBasicNotation()).isEqualTo(notation);
        assertThat(move.isChecking()).isTrue();
        assertThat(move.isTaking()).isTrue();
        assertThat(move.getPiece()).isNull();
        assertThat(move.getFromX()).isEqualTo(0);
        assertThat(move.getFromY()).isEqualTo(0);
        assertThat(move.getToX()).isEqualTo(0);
        assertThat(move.getToY()).isEqualTo(5);
    }

    @Test
    public void testFromBasicNotationCastling() {
        String notation = "0-0-0";
        Move move = Move.fromBasicNotation(notation, Color.BLACK);
        assertThat(move.getBasicNotation()).isEqualTo(notation);
        assertThat(move).isInstanceOf(CastlingMove.class);
        CastlingMove castlingMove = (CastlingMove) move;
        assertThat(castlingMove.isChecking()).isFalse();
        assertThat(castlingMove.isTaking()).isFalse();
        assertThat(castlingMove.getPiece()).isNull();
        assertThat(castlingMove.getRook()).isNull();
        assertThat(castlingMove.getFromX()).isEqualTo(4);
        assertThat(castlingMove.getFromY()).isEqualTo(7);
        assertThat(castlingMove.getToX()).isEqualTo(2);
        assertThat(castlingMove.getToY()).isEqualTo(7);
        assertThat(castlingMove.getRookFromX()).isEqualTo(0);
        assertThat(castlingMove.getRookFromY()).isEqualTo(7);
        assertThat(castlingMove.getRookToX()).isEqualTo(3);
        assertThat(castlingMove.getRookToY()).isEqualTo(7);

        notation = "0-0";
        move = Move.fromBasicNotation(notation, Color.WHITE);
        assertThat(move.getBasicNotation()).isEqualTo(notation);
        assertThat(move).isInstanceOf(CastlingMove.class);
        castlingMove = (CastlingMove) move;
        assertThat(castlingMove.isChecking()).isFalse();
        assertThat(castlingMove.isTaking()).isFalse();
        assertThat(castlingMove.getPiece()).isNull();
        assertThat(castlingMove.getRook()).isNull();
        assertThat(castlingMove.getFromX()).isEqualTo(4);
        assertThat(castlingMove.getFromY()).isEqualTo(0);
        assertThat(castlingMove.getToX()).isEqualTo(6);
        assertThat(castlingMove.getToY()).isEqualTo(0);
        assertThat(castlingMove.getRookFromX()).isEqualTo(7);
        assertThat(castlingMove.getRookFromY()).isEqualTo(0);
        assertThat(castlingMove.getRookToX()).isEqualTo(5);
        assertThat(castlingMove.getRookToY()).isEqualTo(0);
    }

    @Test
    public void testFromBasicNotationEmpty() {
        assertThatIllegalArgumentException().isThrownBy(() -> Move.fromBasicNotation("", Color.WHITE));
        assertThatIllegalArgumentException().isThrownBy(() -> Move.fromBasicNotation(null, Color.WHITE));
        assertThatIllegalArgumentException().isThrownBy(() -> Move.fromBasicNotation("a1-a6+", null));
    }

    @Test
    public void testToString() {
        Move move = new Move(new Rook(Color.WHITE), 0, 0, 0, 5);
        assertThat(move.toString()).isEqualTo(move.getPrettyNotation());
    }

    @Test
    public void testEqualsForPosition() {
        Move move = new Move(new Rook(Color.WHITE), 0, 0, 0, 5);
        Move move2 = new Move(new Rook(Color.BLACK), move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        assertThat(move.equalsForPositions(move2)).isTrue();
        assertThat(move.equals(move2)).isFalse();
    }

    @Test
    public void testEquals() {
        Piece rook1 = new Rook(Color.WHITE);
        Piece rook2 = new Rook(Color.WHITE);
        Move move = new Move(rook1, 0, 0, 0, 5);
        Move move2 = new Move(rook2, move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        Move move3 = new Move(rook1, move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        assertThat(move.equals(move2)).isFalse();
        assertThat(move.equals(move3)).isTrue();
    }

}
