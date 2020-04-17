package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TraditionalBotTest {
    @Mock
    MoveService moveService;

    @Mock
    Game game;

    @Test
    public void testTraditionalBotProps() {
        Player bot = new TraditionalBot(0, null, moveService);
        assertThat(bot.getName()).isEqualTo("Bobby");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    public void testSelectMove() {
        int level = 2;
        Integer timeout = 3;
        Bot bot = new TraditionalBot(level, timeout, moveService);
        bot.selectMove(game);
        verify(moveService).selectMove(any(), eq(level), notNull());
    }

}
