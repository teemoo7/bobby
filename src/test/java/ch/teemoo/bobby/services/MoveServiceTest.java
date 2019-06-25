package ch.teemoo.bobby.services;

import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.players.RandomBot;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class MoveServiceTest {

    private MoveService moveService;

    @Before
    public void setUp() {
        moveService = new MoveService();
    }

    @Test
    public void testFindKingPosition() {
        // Initial positions board
        Game game = new Game(new RandomBot(), new RandomBot());
        assertThat(moveService.findKingPosition(game.getBoard(), Color.WHITE)).isPresent().get().hasFieldOrPropertyWithValue("x", 4).hasFieldOrPropertyWithValue("y", 0);
        assertThat(moveService.findKingPosition(game.getBoard(), Color.BLACK)).isPresent().get().hasFieldOrPropertyWithValue("x", 4).hasFieldOrPropertyWithValue("y", 7);

        // Empty board
        Board emptyBoard = new Board(new Piece[8][8]);
        assertThat(moveService.findKingPosition(emptyBoard, Color.WHITE)).isEmpty();
        assertThat(moveService.findKingPosition(emptyBoard, Color.BLACK)).isEmpty();
    }


    @Test
    public void testComputeDiagonalMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "                \n" +
                "      ♗         \n" +
                "                \n" +
                "                \n" +
                "                \n" +
                "                \n"
        );
        Piece bishop = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeDiagonalMoves(bishop, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up-right
                new Move(bishop, 3, 4, 4, 5),
                new Move(bishop, 3, 4, 5, 6),
                new Move(bishop, 3, 4, 6, 7),
                // up-left
                new Move(bishop, 3, 4, 2, 5),
                new Move(bishop, 3, 4, 1, 6),
                new Move(bishop, 3, 4, 0, 7),
                // down-right
                new Move(bishop, 3, 4, 4, 3),
                new Move(bishop, 3, 4, 5, 2),
                new Move(bishop, 3, 4, 6, 1),
                new Move(bishop, 3, 4, 7, 0),
                // down-left
                new Move(bishop, 3, 4, 2, 3),
                new Move(bishop, 3, 4, 1, 2),
                new Move(bishop, 3, 4, 0, 1)
        );
    }

    @Test
    public void testComputeDiagonalMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "  ♙             \n" +
                "        ♟       \n" +
                "      ♗         \n" +
                "    ♙           \n" +
                "          ♟     \n" +
                "                \n" +
                "                \n"
        );
        Piece bishop = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeDiagonalMoves(bishop, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up-right
                getMoveWithTookPiece(bishop, 3, 4, 4, 5, board.getPiece(4, 5).get()),
                // up-left
                new Move(bishop, 3, 4, 2, 5),
                // down-right
                new Move(bishop, 3, 4, 4, 3),
                getMoveWithTookPiece(bishop, 3, 4, 5, 2, board.getPiece(5, 2).get())
                // down-left
        );
    }

    @Test
    public void testComputeStraightMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "                \n" +
                "      ♖         \n" +
                "                \n" +
                "                \n" +
                "                \n" +
                "                \n"
        );
        Piece rook = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeStraightMoves(rook, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up
                new Move(rook, 3, 4, 3, 5),
                new Move(rook, 3, 4, 3, 6),
                new Move(rook, 3, 4, 3, 7),
                //down
                new Move(rook, 3, 4, 3, 3),
                new Move(rook, 3, 4, 3, 2),
                new Move(rook, 3, 4, 3, 1),
                new Move(rook, 3, 4, 3, 0),
                // left
                new Move(rook, 3, 4, 2, 4),
                new Move(rook, 3, 4, 1, 4),
                new Move(rook, 3, 4, 0, 4),
                // right
                new Move(rook, 3, 4, 4, 4),
                new Move(rook, 3, 4, 5, 4),
                new Move(rook, 3, 4, 6, 4),
                new Move(rook, 3, 4, 7, 4)
        );
    }

    @Test
    public void testComputeStraightMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "      ♙         \n" +
                "                \n" +
                "    ♟ ♖ ♙       \n" +
                "                \n" +
                "                \n" +
                "      ♟         \n" +
                "                \n"
        );
        Piece rook = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeStraightMoves(rook, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up
                new Move(rook, 3, 4, 3, 5),
                //down
                new Move(rook, 3, 4, 3, 3),
                new Move(rook, 3, 4, 3, 2),
                getMoveWithTookPiece(rook, 3, 4, 3, 1, board.getPiece(3, 1).get()),
                // left
                getMoveWithTookPiece(rook, 3, 4, 2, 4, board.getPiece(2, 4).get())
                // right
        );
    }

    @Test
    public void testComputeCastlingMovesBothSides() {
        Board board = new Board("" +
                "♜       ♚     ♜ \n" +
                "♟ ♟ ♟ ♛   ♟ ♟ ♟ \n" +
                "    ♞ ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.computeCastlingMoves(blackKing, 4, 7, board)).hasSize(2);
        Piece whiteKing = board.getPiece(3, 0).get();
        assertThat(moveService.computeCastlingMoves(whiteKing, 4, 0, board)).hasSize(0);
    }

    @Test
    public void testGetCastlingMoveCorrect() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece whiteKing = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, whiteKing, 4, 0, 2, 0, 3)).isPresent().get().isInstanceOf(CastlingMove.class);
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.getCastlingMove(board, blackKing, 4, 7, 6, 7, 5)).isPresent().get().isInstanceOf(CastlingMove.class);
    }

    @Test
    public void testGetCastlingMoveRookNotPresent() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "        ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3)).isEmpty();

        board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♗       ♔   ♘ ♖ \n"
        );
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3)).isEmpty();

        board = new Board("" +
                "  ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♜       ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3)).isEmpty();
    }

    @Test
    public void testGetCastlingMovePieceBetweenRookAndKing() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♗ ♔   ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3)).isEmpty();
    }

    @Test
    public void testGetCastlingMoveKingCrossFire() {
        Board board = new Board("" +
                "♜ ♞     ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "    ♛   ♙       \n" +
                "          ♙     \n" +
                "♙ ♙   ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3)).isEmpty();
    }

    @Test
    public void testIsValidKingPositionForCastlingKingHasMoved() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 3, 0, board)).isFalse();

        board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙ ♔   ♙ ♙ \n" +
                "♖         ♗ ♘ ♖ \n"
        );
        king = board.getPiece(4, 1).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 1, board)).isFalse();

        board = new Board("" +
                "♜ ♞ ♝ ♛       ♜ \n" +
                "♟ ♟ ♟   ♚ ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙ ♔   ♙ ♙ \n" +
                "♖         ♗ ♘ ♖ \n"
        );
        king = board.getPiece(4, 6).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 6, board)).isFalse();
    }


    @Test
    public void testIsValidKingPositionForCastlingWrongPiece() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔ ♕ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingEmptyLocation() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingInCheck() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙     ♝ \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingCorrect() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece whiteKing = board.getPiece(4, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(whiteKing, 4, 0, board)).isTrue();
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.isValidKingPositionForCastling(blackKing, 4, 7, board)).isTrue();
    }

    @Test
    public void testGetAllowedMoveOutOfBounds() {
        assertThat(moveService.getAllowedMove(null, 0, 7, 0, 1, null)).isEmpty();
    }

    @Test
    public void testGetAllowedBishopCapturingQueen() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Bishop(Color.BLACK), 2, 7, 4, -4, board)).isPresent()
                .get().hasFieldOrPropertyWithValue("tookPiece", board.getPiece(6, 3).get());
    }

    @Test
    public void testGetAllowedMovePawnMovingWithoutCapture() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟ ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 0, -1, board)).isPresent();
    }

    @Test
    public void testGetAllowedMovePawnMovingWithoutCaptureBlockedByOwnPawn() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "      ♟ ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 0, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedMovePawnMovingAndCapturing() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟ ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isPresent()
                .get().hasFieldOrPropertyWithValue("tookPiece", board.getPiece(4, 3).get());
    }

    @Test
    public void testGetAllowedMovePawnMovingCapturingBlockedByOwnPawn() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "        ♟   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedMovePawnMovingCapturingNothingToCapture() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "            ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedBishopCannotCaptureOwnPiece() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟       ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♟   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Bishop(Color.BLACK), 2, 7, 4, -4, board)).isEmpty();
    }

    @Test
    public void testIsOutOfBounds() {
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, -1, 0))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, -1))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 8, 0))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, 8))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, 7))).isFalse();
    }

    @Test
    public void testIsValidSituationMissingKing() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕   ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isFalse();
    }

    @Test
    public void testIsValidSituationKingsDistanceTooSmall() {
        Board board = new Board("" +
                "♜ ♞     ♔ ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕   ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isFalse();
    }

    @Test
    public void testIsValidSituationCannotBeInCheckAfterMyMove() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♖               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "    ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
    }

    @Test
    public void testIsValidSituationCorrect() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♖               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "    ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isTrue();
    }

    @Test
    public void testIsBlackKingInPawnCheck() {
        Board board = new Board("" +
                "♜ ♞     ♚     ♜ \n" +
                "♟ ♟ ♟ ♝   ♙   ♟ \n" +
                "                \n" +
                "        ♘       \n" +
                "  ♝     ♙ ♛     \n" +
                "                \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInPawnCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInPawnCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsWhiteKingInPawnCheck() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInPawnCheck(board, new Position(5, 7), Color.BLACK)).isFalse();
        assertThat(moveService.isInPawnCheck(board, new Position(4, 0), Color.WHITE)).isTrue();
    }

    @Test
    public void testIsInLCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟   ♟ \n" +
                "          ♘ ♟   \n" +
                "        ♘       \n" +
                "  ♝     ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInLCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInLCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInLCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚     ♜ \n" +
                "♟ ♟ ♟     ♟   ♟ \n" +
                "          ♛ ♟   \n" +
                "        ♘       \n" +
                "  ♝     ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testIsInDiagonalCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟   ♛ ♟   ♟ \n" +
                "      ♟         \n" +
                "  ♗     ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "♙   ♙           \n" +
                "  ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔   ♘ ♖ "
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 0), Color.WHITE)).isFalse();

        board = new Board("" + "" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟       ♟   ♟ \n" +
                "    ♟           \n" +
                "  ♗   ♟   ♕ ♟   \n" +
                "  ♛ ♙ ♙ ♙   ♞   \n" +
                "♙               \n" +
                "  ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔   ♘ ♖ \n"
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 0), Color.WHITE)).isTrue();

    }

    @Test
    public void testIsInDiagonalCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟ ♟ ♛ ♟   ♟ \n" +
                "                \n" +
                "        ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testIsInStraightCheckVertically() {
        // Vertically
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝   ♜ \n" +
                "♟ ♟ ♟ ♟   ♟ ♟ ♟ \n" +
                "          ♞     \n" +
                "        ♕       \n" +
                "        ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInStraightCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInStraightCheckHorizontally() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♕ \n" +
                "♟ ♟ ♟ ♟ ♝ ♟   ♟ \n" +
                "                \n" +
                "            ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInStraightCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInStraightCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟ ♟ ♛ ♟   ♟ \n" +
                "                \n" +
                "        ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testCenteredHeatmap() {
        int[][] heatmap = MoveService.generateCenteredHeatmap();
        assertThat(heatmap).hasSize(8);
        assertThat(heatmap[0]).hasSize(8);
        int[][] expected = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0},
                {0, 0, 1, 2, 2, 1, 0, 0},
                {0, 0, 1, 2, 2, 1, 0, 0},
                {0, 0, 0, 1, 1, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
        };
        assertThat(heatmap).isEqualTo(expected);
    }

    @Test
    public void testgetHeatmapAroundLocation() {
        int[][] heatmap = moveService.getHeatmapAroundLocation(7, 0);
        assertThat(heatmap).hasSize(8);
        assertThat(heatmap[0]).hasSize(8);
        int[][] expected = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 0, 0, 0, 0, 0},
                {2, 2, 1, 0, 0, 0, 0, 0},
                {3, 2, 1, 0, 0, 0, 0, 0},
        };
        assertThat(heatmap).isEqualTo(expected);
    }

    private Move getMoveWithTookPiece(Piece piece, int fromX, int fromY, int toX, int toY, Piece tookPiece) {
        Move move = new Move(piece, fromX, fromY, toX, toY);
        move.setTookPiece(tookPiece);
        return move;
    }
}
