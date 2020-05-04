package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PieceTest {

    @Test
    public void testPawn() {
        Piece whitePawn = new Pawn(Color.WHITE);
        assertThat(whitePawn.getUnicode()).isEqualTo("♙");
        Piece blackPawn = new Pawn(Color.BLACK);
        assertThat(blackPawn.getUnicode()).isEqualTo("♟");
        //assertThat(blackPawn.getId()).isNotEqualTo(whitePawn.getId());
        assertThat(blackPawn.getValue()).isEqualTo(whitePawn.getValue());
        assertThat(blackPawn.getValue()).isEqualTo(1);
    }

    @Test
    public void testKnight() {
        Piece whiteKnight = new Knight(Color.WHITE);
        assertThat(whiteKnight.getUnicode()).isEqualTo("♘");
        Piece blackKnight = new Knight(Color.BLACK);
        assertThat(blackKnight.getUnicode()).isEqualTo("♞");
        //assertThat(blackKnight.getId()).isNotEqualTo(whiteKnight.getId());
        assertThat(blackKnight.getValue()).isEqualTo(whiteKnight.getValue());
        assertThat(blackKnight.getValue()).isEqualTo(3);
    }

    @Test
    public void testBishop() {
        Piece whiteBishop = new Bishop(Color.WHITE);
        assertThat(whiteBishop.getUnicode()).isEqualTo("♗");
        Piece blackBischop = new Bishop(Color.BLACK);
        assertThat(blackBischop.getUnicode()).isEqualTo("♝");
        //assertThat(blackBischop.getId()).isNotEqualTo(whiteBishop.getId());
        assertThat(blackBischop.getValue()).isEqualTo(whiteBishop.getValue());
        assertThat(blackBischop.getValue()).isEqualTo(3);
    }

    @Test
    public void testRook() {
        Piece whiteRook = new Rook(Color.WHITE);
        assertThat(whiteRook.getUnicode()).isEqualTo("♖");
        Piece blackRook = new Rook(Color.BLACK);
        assertThat(blackRook.getUnicode()).isEqualTo("♜");
        //assertThat(blackRook.getId()).isNotEqualTo(whiteRook.getId());
        assertThat(blackRook.getValue()).isEqualTo(whiteRook.getValue());
        assertThat(blackRook.getValue()).isEqualTo(5);
    }

    @Test
    public void testQueen() {
        Piece whiteQueen = new Queen(Color.WHITE);
        assertThat(whiteQueen.getUnicode()).isEqualTo("♕");
        Piece blackQueen = new Queen(Color.BLACK);
        assertThat(blackQueen.getUnicode()).isEqualTo("♛");
        //assertThat(blackQueen.getId()).isNotEqualTo(whiteQueen.getId());
        assertThat(blackQueen.getValue()).isEqualTo(whiteQueen.getValue());
        assertThat(blackQueen.getValue()).isEqualTo(10);
    }

    @Test
    public void testKing() {
        Piece whiteKing = new King(Color.WHITE);
        assertThat(whiteKing.getUnicode()).isEqualTo("♔");
        Piece blackKing = new King(Color.BLACK);
        assertThat(blackKing.getUnicode()).isEqualTo("♚");
        assertThat(blackKing.getValue()).isEqualTo(whiteKing.getValue());
        assertThat(blackKing.getValue()).isEqualTo(100);
    }

    @Test
    public void testFromUnicodeChar() {
        assertThat(Piece.fromUnicodeChar('♜')).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(Piece.fromUnicodeChar('♞')).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(Piece.fromUnicodeChar('♝')).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(Piece.fromUnicodeChar('♛')).isInstanceOf(Queen.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(Piece.fromUnicodeChar('♚')).isInstanceOf(King.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(Piece.fromUnicodeChar('♟')).isInstanceOf(Pawn.class).hasFieldOrPropertyWithValue("color", Color.BLACK);

        assertThat(Piece.fromUnicodeChar('♖')).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(Piece.fromUnicodeChar('♘')).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(Piece.fromUnicodeChar('♗')).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(Piece.fromUnicodeChar('♕')).isInstanceOf(Queen.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(Piece.fromUnicodeChar('♔')).isInstanceOf(King.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(Piece.fromUnicodeChar('♙')).isInstanceOf(Pawn.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
    }
}
