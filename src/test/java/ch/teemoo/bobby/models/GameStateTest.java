package ch.teemoo.bobby.models;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GameStateTest {

    @Test
    public void testIsDraw() {
        assertThat(GameState.DRAW_50_MOVES.isDraw()).isTrue();
        assertThat(GameState.DRAW_STALEMATE.isDraw()).isTrue();
        assertThat(GameState.DRAW_THREEFOLD.isDraw()).isTrue();
        assertThat(GameState.DRAW_AGREEMENT.isDraw()).isTrue();
        assertThat(GameState.IN_PROGRESS.isDraw()).isFalse();
        assertThat(GameState.LOSS.isDraw()).isFalse();
    }

    @Test
    public void testIsLost() {
        assertThat(GameState.DRAW_50_MOVES.isLost()).isFalse();
        assertThat(GameState.DRAW_STALEMATE.isLost()).isFalse();
        assertThat(GameState.DRAW_THREEFOLD.isLost()).isFalse();
        assertThat(GameState.DRAW_AGREEMENT.isLost()).isFalse();
        assertThat(GameState.IN_PROGRESS.isLost()).isFalse();
        assertThat(GameState.LOSS.isLost()).isTrue();
    }

    @Test
    public void testIsInProgress() {
        assertThat(GameState.DRAW_50_MOVES.isInProgress()).isFalse();
        assertThat(GameState.DRAW_STALEMATE.isInProgress()).isFalse();
        assertThat(GameState.DRAW_THREEFOLD.isInProgress()).isFalse();
        assertThat(GameState.DRAW_AGREEMENT.isLost()).isFalse();
        assertThat(GameState.IN_PROGRESS.isInProgress()).isTrue();
        assertThat(GameState.LOSS.isInProgress()).isFalse();
    }
}
