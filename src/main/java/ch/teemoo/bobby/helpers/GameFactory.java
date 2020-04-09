package ch.teemoo.bobby.helpers;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.GameSetup;

public class GameFactory {
	public Game createGame(GameSetup gameSetup) {
		return new Game(gameSetup.getWhitePlayer(), gameSetup.getBlackPlayer());
	}

	public Game emptyGame() {
		return new Game(null, null);
	}
}
