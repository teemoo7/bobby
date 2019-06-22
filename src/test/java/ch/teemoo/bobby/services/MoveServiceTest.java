package ch.teemoo.bobby.services;

import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.players.RandomBot;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

}
