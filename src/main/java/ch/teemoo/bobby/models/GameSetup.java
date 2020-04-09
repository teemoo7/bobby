package ch.teemoo.bobby.models;

import ch.teemoo.bobby.models.players.Player;

public class GameSetup {
	private final Player whitePlayer;
	private final Player blackPlayer;

	public GameSetup(Player whitePlayer, Player blackPlayer) {
		this.whitePlayer = whitePlayer;
		this.blackPlayer = blackPlayer;
	}

	public Player getWhitePlayer() {
		return whitePlayer;
	}

	public Player getBlackPlayer() {
		return blackPlayer;
	}
}
