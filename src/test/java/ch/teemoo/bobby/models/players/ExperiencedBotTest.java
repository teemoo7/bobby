package ch.teemoo.bobby.models.players;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import ch.teemoo.bobby.models.moves.CastlingMove;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExperiencedBotTest {
    @Mock
    MoveService moveService;

    @Mock
    OpeningService openingService;

    @Mock
    Game game;

    @Test
    public void testExperiencedBotProps() {
        Player bot = new ExperiencedBot(0, null, moveService, openingService);
        assertThat(bot.getName()).isEqualTo("Bobby");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    public void testSelectMoveNoOpening() {
        when(openingService.findPossibleMovesForHistory(any())).thenReturn(Collections.emptyList());
        int level = 2;
        Integer timeout = 3;
        Bot bot = new ExperiencedBot(level, timeout, moveService, openingService);
        bot.selectMove(game);
        verify(moveService).selectMove(any(), eq(level), notNull());
    }

    @Test
    public void testSelectMoveWithOpening() {
        Move openingMove = new CastlingMove(new King(Color.WHITE), 4, 0, 2, 0, new Rook(Color.WHITE), 0, 0, 3, 0);
        when(openingService.findPossibleMovesForHistory(any())).thenReturn(Collections.singletonList(openingMove));
        int level = 2;
        Integer timeout = 3;
        Bot bot = new ExperiencedBot(level, timeout, moveService, openingService);
        Move move = bot.selectMove(game);
        verify(moveService, never()).selectMove(any(), anyInt(), any());
        assertThat(move).isEqualTo(openingMove);
    }

}
