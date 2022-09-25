package ch.teemoo.bobby.models.players;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.verify;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.services.MoveService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TraditionalBotTest {
    @Mock
    MoveService moveService;

    @Mock
    Game game;

    @Test
    public void testTraditionalBotProps() {
        Player bot = new TraditionalBot(0, null, moveService);
        assertThat(bot.getName()).isEqualTo("Bobby");
        assertThat(bot.getDescription()).isEqualTo("TraditionalBot Bobby (level 0)");
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

    @Test
    public void testIsDrawAcceptable() {
        Bot bot = new TraditionalBot(1, null, moveService);
        bot.isDrawAcceptable(null);
        verify(moveService).isDrawAcceptable(any());
    }
}
