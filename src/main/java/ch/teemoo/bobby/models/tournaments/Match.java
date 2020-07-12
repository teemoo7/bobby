package ch.teemoo.bobby.models.tournaments;

import ch.teemoo.bobby.models.players.Player;

public class Match {
	private final Player player1;
	private final Player player2;
	private float scorePlayer1;
	private float scorePlayer2;
	private int playedGames;
	private int totalMoves;

	public Match(Player player1, Player player2) {
		this.player1 = player1;
		this.player2 = player2;
		this.scorePlayer1 = 0;
		this.scorePlayer2 = 0;
		this.playedGames = 0;
		this.totalMoves = 0;
	}

	public Player getPlayer1() {
		return player1;
	}

	public Player getPlayer2() {
		return player2;
	}

	public float getScoreByPlayer(Player player) {
		if (player == player1) {
			return scorePlayer1;
		} else if (player == player2) {
			return scorePlayer2;
		} else {
			throw new RuntimeException("Given player does not take part to this match");
		}
	}

	public boolean isPlayerTakingPartToTheMatch(Player player) {
		return player == player1 || player == player2;
	}

	public void addDraw(int nbMoves) {
		this.scorePlayer1 += 0.5;
		this.scorePlayer2 += 0.5;
		addGame(nbMoves);
	}

	public void addWin(Player player, int nbMoves) {
		if (player.equals(player1)) {
			this.scorePlayer1 += 1;
		} else if (player.equals(player2)) {
			this.scorePlayer2 += 1;
		} else {
			throw new RuntimeException("Player not found");
		}
		addGame(nbMoves);
	}

	public String toString() {
		return
			"Players: \t" + player1.getDescription() + " vs " + player2.getDescription() + "\n" +
			"Score:   \t" + scorePlayer1 + "-" + scorePlayer2 + "\n" +
			"Games:   \t" + playedGames + "\n" +
			"Moves:   \t" + totalMoves + "\n" +
			"Avg m/g: \t" + (float) totalMoves / (float) playedGames;
	}

	private void addGame(int nbMoves) {
		this.playedGames++;
		this.totalMoves += nbMoves;
	}
}
