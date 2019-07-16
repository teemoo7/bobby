package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.gui.Square;
import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GameControllerTest {
    @Mock
    BoardView view;

    @Mock
    Game game;

    @Mock
    Board board;

    @Mock
    MoveService moveService;

    private GameController controller;

    @Before
    public void setUp() {
        when(game.getBoard()).thenReturn(board);
        Square[][] squares = new Square[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                var square = mock(Square.class);
                when(square.getMouseListeners()).thenReturn(new MouseListener[]{});
                squares[i][j] = square;
            }
        }
        when(view.getSquares()).thenReturn(squares);
        controller = new GameController(view, game, moveService);
    }

    @Test
    public void testGameControllerInit() {
        verify(view).display(any());
        verify(view).setItemLoadActionListener(any());
        verify(view).setItemPrintToConsoleActionListener(any());
        verify(view).setItemSaveActionListener(any());
        verify(view).setItemSuggestMoveActionListener(any());
        verify(view).setItemUndoMoveActionListener(any());
    }

    @Test
    public void testDoMoveUnauthorized() {
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);

        when(game.getPlayerByColor(eq(Color.WHITE))).thenReturn(new Human("test"));
        when(moveService.computeMoves(any(), any(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Collections.emptyList());

        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> controller.doMove(move))
                .withMessage("Unauthorized move");
    }

    @Test
    public void testDoMove() {
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        var computedMove = new Move(new Queen(Color.WHITE), move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
        when(game.getPlayerByColor(eq(Color.WHITE))).thenReturn(new Human("test"));
        when(moveService.getGameState(any(), any(), anyList())).thenReturn(GameState.IN_PROGRESS);
        when(moveService.computeMoves(any(), any(), anyInt(), anyInt(), anyBoolean()))
                .thenReturn(Collections.singletonList(computedMove));
        controller.doMove(move);
        verify(board).doMove(eq(computedMove));
        verify(view).refresh(any());
        verify(game).addMoveToHistory(eq(computedMove));
        verify(game).setToPlay(eq(Color.BLACK));
    }
}
