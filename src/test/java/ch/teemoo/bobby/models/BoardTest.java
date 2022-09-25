package ch.teemoo.bobby.models;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.CastlingMove;
import ch.teemoo.bobby.models.moves.EnPassantMove;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.moves.PromotionMove;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.players.Human;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BoardTest {

    private Game game;
    private Board initialBoard;

    @BeforeEach
    public void setUp() {
        // The game board will have the default pieces positions, which will be used in the tests below
        this.game = new Game(new Human("Human"), new Human("Human2"));
        this.initialBoard = game.getBoard();
    }

    @Test
    public void testGetPiece() {
        // Get white queen
        Piece piece = initialBoard.getBoard()[0][3];
        assertThat(piece).isNotNull().isInstanceOf(Queen.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(initialBoard.getPiece(3, 0)).isPresent().get().isEqualTo(piece);

        // Get empty location
        piece = initialBoard.getBoard()[2][0];
        assertThat(piece).isNull();
        assertThat(initialBoard.getPiece(0, 2)).isEmpty();
    }

    @Test
    public void testToString() {
        String expected = "" + //
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" + //
                "♟ ♟ ♟ ♟ ♟ ♟ ♟ ♟ \n" + //
                "                \n" + //
                "                \n" + //
                "                \n" + //
                "                \n" + //
                "♙ ♙ ♙ ♙ ♙ ♙ ♙ ♙ \n" + //
                "♖ ♘ ♗ ♕ ♔ ♗ ♘ ♖ \n";
        assertThat(initialBoard.toString()).isEqualTo(expected);
    }

    @Test
    public void fromString() {
        String initialBoardStr = initialBoard.toString();
        Board board = new Board(initialBoardStr);
        assertThat(board.toString()).isEqualTo(initialBoardStr);
    }

    @Test
    public void testCopy() {
        Board clone = initialBoard.copy();
        assertThat(clone.toString()).isEqualTo(initialBoard.toString());
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Optional<Piece> pieceOpt = clone.getPiece(i, j);
                Optional<Piece> initialPieceOpt = initialBoard.getPiece(i, j);
                if (pieceOpt.isPresent()) {
                    assertThat(pieceOpt.get()).isInstanceOf(
                            initialPieceOpt.orElseThrow(() -> new RuntimeException("Expected piece here")).getClass());
                    assertThat(pieceOpt.get().getColor()).isEqualTo(
                            initialPieceOpt.orElseThrow(() -> new RuntimeException("Expected piece here")).getColor());
                } else {
                    assertThat(initialPieceOpt).isEmpty();
                }

            }
        }
    }

    @Test
    public void testDoMove() {
        Piece knight = initialBoard.getPiece(1, 0).orElseThrow(() -> new RuntimeException("Expected piece here"));
        Move move = new Move(knight, 1, 0, 2, 2);
        initialBoard.doMove(move);
        assertThat(initialBoard.getPiece(1, 0)).isEmpty();
        assertThat(initialBoard.getPiece(2, 2)).isPresent().get().isEqualTo(knight);
    }

    @Test
    public void testUndoMove() {
        Piece knight = initialBoard.getPiece(1, 0).orElseThrow(() -> new RuntimeException("Expected piece here"));
        Move move = new Move(knight, 1, 0, 2, 2);
        initialBoard.doMove(move);
        assertThat(initialBoard.getPiece(1, 0)).isEmpty();
        assertThat(initialBoard.getPiece(2, 2)).isPresent().get().isEqualTo(knight);
        initialBoard.undoMove(move);
        assertThat(initialBoard.getPiece(1, 0)).isPresent().get().isEqualTo(knight);
        assertThat(initialBoard.getPiece(2, 2)).isEmpty();
    }

    @Test
    public void testDoMoveCastling() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝   ♜ \n" +
                "♟ ♟   ♟ ♞ ♟ ♟ ♟ \n" +
                "    ♟           \n" +
                "                \n" +
                "      ♛         \n" +
                "  ♕ ♘           \n" +
                "♙ ♙   ♗ ♙ ♙ ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n" +
                "");
        Piece king = board.getPiece(4, 0).orElseThrow(() -> new RuntimeException("Piece expected here"));
        Piece rook = board.getPiece(0, 0).orElseThrow(() -> new RuntimeException("Piece expected here"));
        Move castlingMove = new CastlingMove(king, 4, 0, 2, 0, rook, 0, 0, 3, 0);
        board.doMove(castlingMove);
        assertThat(board.getPiece(2, 0)).isPresent().get().isEqualTo(king);
        assertThat(board.getPiece(3, 0)).isPresent().get().isEqualTo(rook);
        assertThat(board.getPiece(4, 0)).isEmpty();
        assertThat(board.getPiece(0, 0)).isEmpty();
    }

    @Test
    public void testUndoMoveCastling() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝   ♜ \n" +
                "♟ ♟   ♟ ♞ ♟ ♟ ♟ \n" +
                "    ♟           \n" +
                "                \n" +
                "      ♛         \n" +
                "  ♕ ♘           \n" +
                "♙ ♙   ♗ ♙ ♙ ♙ ♙ \n" +
                "    ♔ ♖   ♗ ♘ ♖ \n" +
                "");
        Piece king = board.getPiece(2, 0).orElseThrow(() -> new RuntimeException("Piece expected here"));
        Piece rook = board.getPiece(3, 0).orElseThrow(() -> new RuntimeException("Piece expected here"));
        Move castlingMove = new CastlingMove(king, 4, 0, 2, 0, rook, 0, 0, 3, 0);
        board.undoMove(castlingMove);
        assertThat(board.getPiece(2, 0)).isEmpty();
        assertThat(board.getPiece(3, 0)).isEmpty();
        assertThat(board.getPiece(4, 0)).isPresent().get().isEqualTo(king);
        assertThat(board.getPiece(0, 0)).isPresent().get().isEqualTo(rook);
    }

    @Test
    public void testDoMoveWithPromotion() {
        final int fromX = 3;
        final int fromY = 6;
        final int toX = 3;
        final int toY = 7;
        Piece[][] positions = new Piece[8][8];
        Piece pawn = new Pawn(Color.WHITE);
        Piece queen = new Queen(Color.WHITE);

        positions[fromY][fromX] = pawn;
        Board board = new Board(positions);
        assertThat(board.getPiece(fromX, fromY)).isPresent().get().isEqualTo(pawn);
        assertThat(board.getPiece(toX, toY)).isEmpty();

        Move move = new Move(pawn, fromX, fromY, toX, toY);
        PromotionMove promotionMove = new PromotionMove(move, queen);
        board.doMove(promotionMove);
        assertThat(board.getPiece(fromX, fromY)).isEmpty();
        assertThat(board.getPiece(toX, toY)).isPresent().get().isEqualTo(queen);
    }

    @Test
    public void testDoMoveEnPassant() {
        final int fromX = 0;
        final int fromY = 4;
        final int toX = 1;
        final int toY = 5;
        Piece[][] positions = new Piece[8][8];
        Piece whitePawn = new Pawn(Color.WHITE);
        Piece blackPawn = new Pawn(Color.BLACK);

        positions[fromY][fromX] = whitePawn;
        positions[fromY][toX] = blackPawn;
        Board board = new Board(positions);
        assertThat(board.getPiece(fromX, fromY)).isPresent().get().isEqualTo(whitePawn);
        assertThat(board.getPiece(toX, toY)).isEmpty();
        assertThat(board.getPiece(toX, fromY)).isPresent().get().isEqualTo(blackPawn);

        Move move = new Move(whitePawn, fromX, fromY, toX, toY);
        EnPassantMove enPassantMove = new EnPassantMove(move, toX, fromY);
        enPassantMove.setTookPiece(blackPawn);
        board.doMove(enPassantMove);
        assertThat(board.getPiece(fromX, fromY)).isEmpty();
        assertThat(board.getPiece(toX, fromY)).isEmpty();
        assertThat(board.getPiece(toX, toY)).isPresent().get().isEqualTo(whitePawn);
    }

    @Test
    public void testUndoMoveEnPassant() {
        final int fromX = 0;
        final int fromY = 4;
        final int toX = 1;
        final int toY = 5;
        Piece[][] positions = new Piece[8][8];
        Piece whitePawn = new Pawn(Color.WHITE);
        Piece blackPawn = new Pawn(Color.BLACK);

        positions[toY][toX] = whitePawn;
        Board board = new Board(positions);
        assertThat(board.getPiece(fromX, fromY)).isEmpty();
        assertThat(board.getPiece(toX, fromY)).isEmpty();
        assertThat(board.getPiece(toX, toY)).isPresent().get().isEqualTo(whitePawn);

        Move move = new Move(whitePawn, fromX, fromY, toX, toY);
        EnPassantMove enPassantMove = new EnPassantMove(move, toX, fromY);
        enPassantMove.setTookPiece(blackPawn);
        board.undoMove(enPassantMove);

        assertThat(board.getPiece(fromX, fromY)).isPresent().get().isEqualTo(whitePawn);
        assertThat(board.getPiece(toX, toY)).isEmpty();
        assertThat(board.getPiece(toX, fromY)).isPresent().get().isEqualTo(blackPawn);
    }
}
