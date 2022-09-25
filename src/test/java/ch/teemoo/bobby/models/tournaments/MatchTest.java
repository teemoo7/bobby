package ch.teemoo.bobby.models.tournaments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.services.MoveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class MatchTest {
	private Player player1;
	private Player player2;
	private Match match;

	@Mock
	MoveService moveService;

	@BeforeEach
	public void setUp() {
		this.player1 = new RandomBot(moveService);
		this.player2 = new RandomBot(moveService);
		this.match = new Match(player1, player2);
	}

	@Test
	public void testGetPlayers() {
		assertThat(match.getPlayer1()).isEqualTo(player1);
		assertThat(match.getPlayer2()).isEqualTo(player2);
	}

	@Test
	public void testGetScoreByPlayer() {
		assertThat(match.getScoreByPlayer(player1)).isEqualTo(0);
		assertThat(match.getScoreByPlayer(player2)).isEqualTo(0);
	}

	@Test
	public void testGetScoreByPlayerException() {
		assertThatExceptionOfType(RuntimeException.class)
			.isThrownBy(() -> match.getScoreByPlayer(new RandomBot(moveService)))
			.withMessage("Given player does not take part to this match");
	}

	@Test
	public void testIsPlayerTakingPartToTheMatch() {
		assertThat(match.isPlayerTakingPartToTheMatch(player1)).isTrue();
		assertThat(match.isPlayerTakingPartToTheMatch(player2)).isTrue();
		assertThat(match.isPlayerTakingPartToTheMatch(new RandomBot(moveService))).isFalse();
	}

	@Test
	public void testAddDraw() {
		// given
		final float score1 = match.getScoreByPlayer(player1);
		final float score2 = match.getScoreByPlayer(player2);

		// when
		match.addDraw(34);

		// then
		assertThat(match.getScoreByPlayer(player1)).isEqualTo(score1 + 0.5f);
		assertThat(match.getScoreByPlayer(player2)).isEqualTo(score2 + 0.5f);
	}

	@Test
	public void testAddWin() {
		// given
		final float score1 = match.getScoreByPlayer(player1);
		final float score2 = match.getScoreByPlayer(player2);

		// when
		match.addWin(player2, 65);

		// then
		assertThat(match.getScoreByPlayer(player1)).isEqualTo(score1);
		assertThat(match.getScoreByPlayer(player2)).isEqualTo(score2 + 1f);
	}

	@Test
	public void testAddWinException() {
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> match.addWin(new RandomBot(moveService), 21))
			.withMessage("Player not found");
	}

	@Test
	public void testToString() {
		assertThat(match.toString()).contains("Players:").contains("Score:").contains("Games:").contains("Moves:")
			.contains("Avg m/g:");
	}
}
