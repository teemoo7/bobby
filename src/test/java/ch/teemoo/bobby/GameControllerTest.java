package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GameControllerTest {
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Mock
    BoardView view;

    @Mock
    Game game;

    @Mock
    Board board;

    @Mock
    MoveService moveService;

    @Mock
    FileService fileService;

    private GameController controller;

    @Before
    public void setUp() {
        when(game.getBoard()).thenReturn(board);
        controller = new GameController(view, game, moveService, fileService);
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
        verify(view).cleanSquaresBorder();
        verify(view).resetAllClickables();
        verify(board).doMove(eq(computedMove));
        verify(view).refresh(any());
        verify(view).addBorderToLastMoveSquares(eq(move));
        verify(game).addMoveToHistory(eq(computedMove));
        verify(game).setToPlay(eq(Color.BLACK));
    }

    @Test
    public void testUndoLastMove() {
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(game.getPlayerByColor(eq(Color.WHITE))).thenReturn(new Human("test"));
        controller.undoLastMove(move);
        verify(view).cleanSquaresBorder();
        verify(view).resetAllClickables();
        verify(board).undoMove(eq(move));
        verify(view).refresh(any());
        verify(game).removeLastMoveFromHistory();
        verify(game).setToPlay(eq(Color.WHITE));
    }

    @Test
    public void testDisplayGameInfoInProgressNoCheckNoOutput() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.IN_PROGRESS);
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).isEmpty();
    }

    @Test
    public void testDisplayGameInfoInProgressCheck() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        move.setChecking(true);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.IN_PROGRESS);
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("Check!");
    }

    @Test
    public void testDisplayGameInfoDrawThreefold() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.DRAW_THREEFOLD);
        when(game.getHistory()).thenReturn(Collections.emptyList());
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("½–½ (0 moves)").contains("Draw (threefold). The game is over.");
    }

    @Test
    public void testDisplayGameInfoDraw50Moves() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.DRAW_50_MOVES);
        when(game.getHistory()).thenReturn(Collections.emptyList());
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("½–½ (0 moves)").contains("Draw (50 moves). The game is over.");
    }

    @Test
    public void testDisplayGameInfoDrawStalemate() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.DRAW_STALEMATE);
        when(game.getHistory()).thenReturn(Collections.emptyList());
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("½–½ (0 moves)").contains("Draw (Stalemate). The game is over.");
    }

    @Test
    public void testDisplayGameInfoLoss() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.WHITE), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.LOSS);
        when(game.getHistory()).thenReturn(Collections.emptyList());
        when(game.getPlayerByColor(eq(Color.WHITE))).thenReturn(player);
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("1-0 (0 moves)").contains("Checkmate! test (WHITE) has won!");
    }

    @Test
    public void testDisplayGameInfoWin() {
        Player player = new Human("test");
        var move = new Move(new Queen(Color.BLACK), 3, 0, 3, 1);
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.LOSS);
        when(game.getHistory()).thenReturn(Collections.emptyList());
        when(game.getPlayerByColor(eq(Color.BLACK))).thenReturn(player);
        controller.displayGameInfo(player, move);
        assertThat(systemOutRule.getLog()).contains("0-1 (0 moves)").contains("Checkmate! test (BLACK) has won!");
    }

    @Test
    public void testSaveGameCancelled() throws Exception {
        when(view.saveGameDialog()).thenReturn(Optional.empty());
        controller.saveGame();
        verify(fileService, never()).writeGameToFileBasicNotation(any(), any());
    }

    @Test
    public void testSaveGame() throws Exception {
        File file = mock(File.class);
        when(view.saveGameDialog()).thenReturn(Optional.of(file));
        controller.saveGame();
        verify(fileService).writeGameToFileBasicNotation(any(), eq(file));
    }

    @Test
    public void testSaveGameException() throws Exception {
        File file = mock(File.class);
        when(view.saveGameDialog()).thenReturn(Optional.of(file));
        doThrow(new IOException("Test exception")).when(fileService).writeGameToFileBasicNotation(any(), any());
        controller.saveGame();
        assertThat(systemOutRule.getLog()).contains("An error happened: Test exception");
    }

    @Test
    public void testLoadGameCancelled() throws Exception {
        when(view.loadGameDialog()).thenReturn(Optional.empty());
        controller.loadGame();
        verify(fileService, never()).readGameFromFileBasicNotation(any());
    }

    @Test
    public void testLoadGame() throws Exception {
        File file = mock(File.class);
        when(view.loadGameDialog()).thenReturn(Optional.of(file));
        when(fileService.readGameFromFileBasicNotation(eq(file))).thenReturn(Collections.singletonList("e2-e4"));
        when(game.getWhitePlayer()).thenReturn(new Human("test"));
        when(game.getBlackPlayer()).thenReturn(new Human("test2"));
        when(moveService.computeMoves(any(), any(), anyInt(), anyInt(), anyBoolean())).thenReturn(Collections.singletonList(new Move(new Pawn(Color.WHITE), 4, 1, 4, 3)));
        when(moveService.getGameState(any(), any(), any())).thenReturn(GameState.IN_PROGRESS);
        controller.loadGame();
        verify(fileService).readGameFromFileBasicNotation(eq(file));
    }

    @Test
    public void testLoadGameException() throws Exception {
        File file = mock(File.class);
        when(view.loadGameDialog()).thenReturn(Optional.of(file));
        doThrow(new IOException("Test exception")).when(fileService).readGameFromFileBasicNotation(any());
        controller.loadGame();
        assertThat(systemOutRule.getLog()).contains("An error happened: Test exception");
    }

    @Test
    public void testPrintGameToConsole() {
        controller.printGameToConsole();
        assertThat(systemOutRule.getLog()).contains("Current board:");
    }

    @Test
    public void testSuggestMove() {
        Move move = new Move(new Knight(Color.BLACK), 3, 7, 4, 5);
        when(moveService.selectMove(any(), anyInt())).thenReturn(move);
        controller.suggestMove();
        assertThat(systemOutRule.getLog()).contains("Suggested move is : " + move.toString());
    }

    @Test
    public void testUndoLastMoveNoHistory() {
        when(game.getHistory()).thenReturn(Collections.emptyList());
        controller.undoLastMove();
        verify(board, never()).undoMove(any());
    }

    @Test
    public void testUndoLastMoveWithHistory() {
        List<Move> moves = Arrays.asList(
                new Move(new Knight(Color.BLACK), 3, 7, 4, 5),
                new Move(new Knight(Color.WHITE), 3, 0, 4, 2)
        );
        when(game.getHistory()).thenReturn(moves);
        when(game.getPlayerByColor(eq(Color.WHITE))).thenReturn(new Human("test"));
        controller.undoLastMove();
        verify(board, times(2)).undoMove(any());
    }
}
