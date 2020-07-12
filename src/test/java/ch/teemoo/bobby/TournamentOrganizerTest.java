package ch.teemoo.bobby;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

public class TournamentOrganizerTest {
	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

	@Test
	public void testTournamentOrganizer() {
		TournamentOrganizer tournamentOrganizer = new TournamentOrganizer(true);
		tournamentOrganizer.run();
		assertThat(systemOutRule.getLog()).contains("2 players has registered")
			.contains("There will be 2 rounds per match, participants play against each other")
			.contains("Tournament is open!")
			.contains("Tournament is over!")
			.contains("Here is the scoreboard");
	}
}
