package ch.teemoo.bobby.models.games;

import static org.assertj.core.api.Assertions.assertThat;

import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import org.junit.Test;

public class GameResultTest {

	@Test
	public void testGameResult() {
		// given
		int moves = 34;
		GameResult.Result result = GameResult.Result.BLACK_WINS;

		// when
		GameResult gameResult = new GameResult(moves, result);

		// then
		assertThat(gameResult.getNbMoves()).isEqualTo(moves);
		assertThat(gameResult.getResult()).isEqualTo(result);
	}
}
