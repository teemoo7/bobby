package ch.teemoo.bobby.helpers;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.games.GameSetup;

public class GameFactory {
	public Game createGame(GameSetup gameSetup) {
		return new Game(gameSetup.getWhitePlayer(), gameSetup.getBlackPlayer());
	}

	public Game emptyGame() {
		return new Game(null, null);
	}
}
