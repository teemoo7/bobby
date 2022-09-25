package ch.teemoo.bobby.models.tournaments;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.services.MoveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class TournamentTest {
	private Player player1;
	private Player player2;
	private Player player3;
	private List<Player> participants;
	private Tournament tournament;

	@Mock
	MoveService moveService;

	@BeforeEach
	public void setUp() {
		player1 = new RandomBot(moveService);
		player2 = new RandomBot(moveService);
		player3 = new RandomBot(moveService);
		participants = Arrays.asList(player1, player2, player3);
		tournament = new Tournament(participants);
	}

	@Test
	public void testInitWithMatchesGeneration() {
		List<Match> matches = tournament.getMatches();
		assertThat(matches).hasSize(3);
		assertThat(matches.stream().filter(m -> m.getPlayer1() == player1 || m.getPlayer2() == player1).collect(
			Collectors.toList())).hasSize(participants.size() - 1);
		assertThat(matches.stream().filter(m -> m.getPlayer1() == player2 || m.getPlayer2() == player2).collect(
			Collectors.toList())).hasSize(participants.size() - 1);
		assertThat(matches.stream().filter(m -> m.getPlayer1() == player3 || m.getPlayer2() == player3).collect(
			Collectors.toList())).hasSize(participants.size() - 1);
	}

	@Test
	public void testGetParticipantScores() {
		Map<Player, Float> participantScores = tournament.getParticipantScores();
		assertThat(participantScores).hasSize(participants.size());
		assertThat(new ArrayList<>(participantScores.keySet())).containsExactlyInAnyOrderElementsOf(participants);
		assertThat(participantScores.values().stream().allMatch(score -> score == 0f)).isTrue();
	}

	@Test
	public void testGetScoreboard() {
		assertThat(tournament.getScoreboard()).contains(player1.getDescription()).contains(player2.getDescription())
			.contains(player3.getDescription());
	}

	@Test
	public void testToString() {
		assertThat(tournament.toString()).isEqualTo(tournament.getScoreboard());
	}
}
