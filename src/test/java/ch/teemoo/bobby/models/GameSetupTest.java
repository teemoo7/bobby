package ch.teemoo.bobby.models;

import static org.assertj.core.api.Assertions.assertThat;

import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import org.junit.Test;

public class GameSetupTest {

	@Test
	public void testGameSetup() {
		// given
		Player player1 = new Human("Test");
		Player player2 = new RandomBot();

		// when
		GameSetup gameSetup = new GameSetup(player1, player2);

		// then
		assertThat(gameSetup.getWhitePlayer()).isEqualTo(player1);
		assertThat(gameSetup.getBlackPlayer()).isEqualTo(player2);
	}
}
