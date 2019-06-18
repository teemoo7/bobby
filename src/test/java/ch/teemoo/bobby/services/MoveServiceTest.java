package ch.teemoo.bobby.services;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Position;
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

        // No check at all
        board = new Board("" +
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
    public void testIsInStraightCheck() {
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

        // Horizontally
        board = new Board("" +
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

        // No check at all
        board = new Board("" +
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
