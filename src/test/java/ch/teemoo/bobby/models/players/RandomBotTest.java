package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RandomBotTest {
    @Mock
    Game game;

    @Mock
    MoveService moveService;

    @Test
    public void testRandomBotProps() {
        Player bot = new RandomBot(moveService);
        assertThat(bot.getName()).isEqualTo("Bobby");
        assertThat(bot.getDescription()).isEqualTo("RandomBot Bobby");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    public void testSelectMove() {
        Bot bot = new RandomBot(moveService);
        Move move = new Move(new Rook(Color.WHITE), 3, 4, 4, 4);
        when(moveService.computeAllMoves(any(), any(), anyList(), eq(true))).thenReturn(Collections.singletonList(move));
        assertThat(bot.selectMove(game)).isEqualTo(move);
    }

    @Test
    public void testIsDrawAcceptable() {
        Bot bot = new RandomBot(null);
        assertThat(bot.isDrawAcceptable(null)).isInstanceOf(Boolean.class);
    }
}
