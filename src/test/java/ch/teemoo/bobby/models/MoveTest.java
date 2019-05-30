package ch.teemoo.bobby.models;

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
    public void testGetPrettyNotation() {
        Piece rook = new Rook(Color.WHITE);
        Move move = new Move(rook, 0, 0, 0, 5);
        assertThat(move.getPrettyNotation()).isEqualTo("WHITE a1-a6 (Rook)");
    }

    @Test
    public void testFromBasicNotation() {
        String notation = "a1-a6+";
        Move move = Move.fromBasicNotation(notation);
        assertThat(move.getBasicNotation()).isEqualTo(notation);
        assertThat(move.isChecking()).isTrue();
        assertThat(move.getPiece()).isNull();
        assertThat(move.getFromX()).isEqualTo(0);
        assertThat(move.getFromY()).isEqualTo(0);
        assertThat(move.getToX()).isEqualTo(0);
        assertThat(move.getToY()).isEqualTo(5);
    }

    @Test
    public void testFromBasicNotationEmpty() {
        assertThatIllegalArgumentException().isThrownBy(() -> Move.fromBasicNotation(""));
        assertThatIllegalArgumentException().isThrownBy(() -> Move.fromBasicNotation(null));
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
