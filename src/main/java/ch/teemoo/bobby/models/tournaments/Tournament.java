package ch.teemoo.bobby.models.tournaments;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.players.Player;

public class Tournament {
	private final List<Player> participants;
	private final List<Match> matches;

	public Tournament(List<Player> participants) {
		this.participants = participants;
		this.matches = generateMatches(participants);
	}

	public List<Match> getMatches() {
		return matches;
	}

	public Map<Player, Float> getParticipantScores() {
		Map<Player, Float> scoresMap = new HashMap<>(participants.size());
		for (Player player: participants) {
			scoresMap.put(player, (float) (matches.stream().filter(m -> m.isPlayerTakingPartToTheMatch(player))
				.mapToDouble(m -> m.getScoreByPlayer(player)).sum()));
		}
		return scoresMap;
	}

	public String getScoreboard() {
		List<Map.Entry<Player, Float>> entries =
			getParticipantScores().entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
				.collect(Collectors.toList());
		int currentPos = 1;
		StringBuilder result = new StringBuilder();
		for (Map.Entry<Player, Float> entry: entries) {
			result.append(currentPos++).append(".\t");
			result.append(entry.getValue()).append("\t");
			result.append(entry.getKey().getDescription()).append("\n");
		}
		return result.toString();
	}

	public String toString() {
		return getScoreboard();
	}

	static List<Match> generateMatches(List<Player> participants) {
		List<Match> matches = new ArrayList<>();
		for (int i = 0; i < participants.size()-1; i++) {
			for (int j = i + 1; j < participants.size(); j++) {
				matches.add(new Match(participants.get(i), participants.get(j)));
			}
		}
		return matches;
	}


}
