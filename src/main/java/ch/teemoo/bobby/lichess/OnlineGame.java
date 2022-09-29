package ch.teemoo.bobby.lichess;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.players.Bot;

public record OnlineGame(String id, Game game, Bot bot) {

}
