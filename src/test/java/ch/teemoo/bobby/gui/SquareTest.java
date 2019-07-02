package ch.teemoo.bobby.gui;

import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SquareTest {

    @Test
    public void testSquare() {
        Piece queen = new Queen(Color.BLACK);
        Position position = new Position(3, 0);
        Square square = new Square(queen, position, Background.DARK);
        assertThat(square.getText()).isEqualTo("♛");
        assertThat(square.getPiece()).isEqualTo(queen);
        assertThat(square.getPosition()).isEqualTo(position);
        assertThat(square.getFont()).isEqualTo(new Font("Sans Serif", Font.PLAIN, 48));
        assertThat(square.getHorizontalAlignment()).isEqualTo(SwingConstants.CENTER);
        assertThat(square.isOpaque()).isTrue();
        assertThat(square.getBackground()).isEqualTo(Background.DARK.getColor());
    }

    @Test
    public void testSpecialCaseMacBlackPawn() {
        Piece pawn = new Pawn(Color.BLACK);
        Square square = new Square(pawn, null, Background.LIGHT);
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            assertThat(square.getText()).isEqualTo("P");
        } else {
            assertThat(square.getText()).isEqualTo("♟");
        }
    }

    @Test
    public void testSetPiece() {
        Piece pawn = new Pawn(Color.WHITE);
        Square square = new Square(pawn, null, Background.LIGHT);
        assertThat(square.getPiece()).isEqualTo(pawn);
        assertThat(square.getText()).isEqualTo("♙");

        Piece knight = new Knight(Color.BLACK);
        square.setPiece(knight);
        assertThat(square.getPiece()).isEqualTo(knight);
        assertThat(square.getText()).isEqualTo("♞");
    }
}
