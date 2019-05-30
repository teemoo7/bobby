package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.pieces.Piece;
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

}
