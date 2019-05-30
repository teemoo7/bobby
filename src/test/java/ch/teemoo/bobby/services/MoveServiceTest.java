package ch.teemoo.bobby.services;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
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


}
