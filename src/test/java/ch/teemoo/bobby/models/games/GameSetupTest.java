package ch.teemoo.bobby.models.games;

import static org.assertj.core.api.Assertions.assertThat;

import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import org.junit.jupiter.api.Test;

public class GameSetupTest {

	@Test
	public void testGameSetup() {
		// given
		Player player1 = new Human("Test");
		Player player2 = new Human("Test2");

		// when
		GameSetup gameSetup = new GameSetup(player1, player2);

		// then
		assertThat(gameSetup.getWhitePlayer()).isEqualTo(player1);
		assertThat(gameSetup.getBlackPlayer()).isEqualTo(player2);
	}
}
