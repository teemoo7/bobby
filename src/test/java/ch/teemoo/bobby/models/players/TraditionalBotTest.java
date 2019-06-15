package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TraditionalBotTest {
    @Mock
    MoveService moveService;

    @Mock
    Game game;

    @Test
    public void testTraditionalBotProps() {
        Player bot = new TraditionalBot(0);
        assertThat(bot.getName()).isEqualTo("Traditional Bot (level 0)");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    public void testTraditionalBotWrongLevel() {
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> new TraditionalBot(-1));
        assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> new TraditionalBot(3));
    }

    @Test
    public void testSelectMove() {
        int level = 2;
        Bot bot = new TraditionalBot(level);
        bot.selectMove(game, moveService);
        verify(moveService).selectMove(any(), eq(level));
    }

}
