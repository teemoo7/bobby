package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        assertThat(bot.getName()).isEqualTo("Bobby (Random Bot)");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    public void testSelectMove() {
        Bot bot = new RandomBot(moveService);
        Move move = new Move(new Rook(Color.WHITE), 3, 4, 4, 4);
        when(moveService.computeAllMoves(any(), any(), eq(true))).thenReturn(Collections.singletonList(move));
        assertThat(bot.selectMove(game)).isEqualTo(move);
    }
}
