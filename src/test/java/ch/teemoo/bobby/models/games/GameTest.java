package ch.teemoo.bobby.models.games;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.*;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class GameTest {

    @Test
    public void testNewGameSettings() {
        Player whitePlayer = new Human("Player 1");
        Game game = new Game(whitePlayer, new Human("Player 2"));
        assertThat(game.getToPlay()).isEqualTo(Color.WHITE);
        assertThat(game.getPlayerToPlay()).isEqualTo(whitePlayer);
        assertThat(game.getHistory()).isEmpty();
        assertThat(game.getState()).isEqualTo(GameState.IN_PROGRESS);
    }

    @Test
    public void testNewGameDefaultBoard() {
        Game game = new Game(new Human("A"), new Human("B"));
        Board board = game.getBoard();
        Piece[][] pieces = board.getBoard();

        // White pieces
        assertThat(pieces[0][0]).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][1]).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][2]).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][3]).isInstanceOf(Queen.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][4]).isInstanceOf(King.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][5]).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][6]).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        assertThat(pieces[0][7]).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        for (int i = 0; i < 8; i++) {
            assertThat(pieces[1][i]).isInstanceOf(Pawn.class).hasFieldOrPropertyWithValue("color", Color.WHITE);
        }

        // Black pieces
        assertThat(pieces[7][0]).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][1]).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][2]).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][3]).isInstanceOf(Queen.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][4]).isInstanceOf(King.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][5]).isInstanceOf(Bishop.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][6]).isInstanceOf(Knight.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        assertThat(pieces[7][7]).isInstanceOf(Rook.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        for (int i = 0; i < 8; i++) {
            assertThat(pieces[6][i]).isInstanceOf(Pawn.class).hasFieldOrPropertyWithValue("color", Color.BLACK);
        }

        // Empty places
        for (int j = 2; j < 6; j++) {
            for (int i = 0; i < 8; i++) {
                assertThat(pieces[j][i]).isNull();
            }
        }
    }

    @Test
    public void testAddMoveToHistory() {
        Game game = new Game(new Human("Player 1"), new Human("Player 2"));
        assertThat(game.getHistory()).isEmpty();
        Move move = new Move(new Pawn(Color.WHITE), 4, 1, 4, 2);
        game.addMoveToHistory(move);
        assertThat(game.getHistory()).containsExactly(move);
    }

    @Test
    public void testRemoveLastMoveFromHistory() {
        Game game = new Game(new Human("Player 1"), new Human("Player 2"));
        assertThat(game.getHistory()).isEmpty();
        Move move = new Move(new Pawn(Color.WHITE), 4, 1, 4, 2);
        game.addMoveToHistory(move);
        assertThat(game.getHistory()).containsExactly(move);
        game.removeLastMoveFromHistory();
        assertThat(game.getHistory()).isEmpty();
    }

    @Test
    public void testGetPlayerByColor() {
        Player whitePlayer = new Human("Player 1");
        Player blackPlayer = new Human("Player 2");
        Game game = new Game(whitePlayer, blackPlayer);
        assertThat(game.getPlayerByColor(Color.WHITE)).isEqualTo(whitePlayer);
        assertThat(game.getPlayerByColor(Color.BLACK)).isEqualTo(blackPlayer);
    }

    @Test
    public void testCanBePlayed() {
        Game game = new Game(new RandomBot(null), new Human("test"));
        assertThat(game.canBePlayed()).isTrue();
        game = new Game(new RandomBot(null), new RandomBot(null));
        assertThat(game.canBePlayed()).isTrue();
        game = new Game(new RandomBot(null), new Human("test"));
        game.setState(GameState.LOSS);
        assertThat(game.canBePlayed()).isFalse();
        game = new Game(null, null);
        assertThat(game.canBePlayed()).isFalse();
    }

    @Test
    public void testGetPlayerToPlayWaiting() {
        Player whitePlayer = new Human("Player 1");
        Player blackPlayer = new Human("Player 2");
        Game game = new Game(whitePlayer, blackPlayer);
        assertThat(game.getPlayerToPlay()).isEqualTo(whitePlayer);
        assertThat(game.getPlayerWaiting()).isEqualTo(blackPlayer);
    }

    @Test
    public void testGetPlayerToPlayWaitingReversed() {
        Player whitePlayer = new Human("Player 1");
        Player blackPlayer = new Human("Player 2");
        Game game = new Game(whitePlayer, blackPlayer);
        game.setToPlay(Color.BLACK);
        assertThat(game.getPlayerToPlay()).isEqualTo(blackPlayer);
        assertThat(game.getPlayerWaiting()).isEqualTo(whitePlayer);
    }
}
