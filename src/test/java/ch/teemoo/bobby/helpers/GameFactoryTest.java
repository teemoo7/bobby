package ch.teemoo.bobby.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.games.GameSetup;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import org.junit.jupiter.api.Test;

public class GameFactoryTest {

	private GameFactory gameFactory = new GameFactory();

	@Test
	public void testCreateGame() {
		// given
		Player player1 = new Human("Test");
		Player player2 = new Human("Test2");
		GameSetup gameSetup = new GameSetup(player1, player2);

		// when
		Game game = gameFactory.createGame(gameSetup);

		// then
		assertThat(game.getWhitePlayer()).isEqualTo(player1);
		assertThat(game.getBlackPlayer()).isEqualTo(player2);
	}

	@Test
	public void testEmptyGame() {
		// given

		// when
		Game game = gameFactory.emptyGame();

		// then
		assertThat(game.getWhitePlayer()).isNull();
		assertThat(game.getBlackPlayer()).isNull();
	}
}
