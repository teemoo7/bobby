package ch.teemoo.bobby.lichess;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.players.Bot;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.services.UniversalChessInterfaceService;
import chariot.model.Challenge;
import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHandler {
	private final static Logger logger = LoggerFactory.getLogger(EventHandler.class);

	private final LichessClient client;
	private final UniversalChessInterfaceService uciService;

	private final BotFactory botFactory;

	protected final Map<String, OnlineGame> onlineGameMap = new HashMap<>();

	public EventHandler(LichessClient client, UniversalChessInterfaceService uciService, BotFactory botFactory) {
		this.client = client;
		this.uciService = uciService;
		this.botFactory = botFactory;
	}

	public void clean() {
		logger.info("Cleaning before exit");
		if (!onlineGameMap.isEmpty()) {
			logger.info("Aborting all games in memory");
			onlineGameMap.keySet().forEach(client::abort);
			onlineGameMap.clear();
		}
	}

	public void start() {
		logger.info("Start playing as a bot");

		Stream<Event> events = client.streamEvents();
		logger.info("Connection successful, waiting for challenges...");

		events.forEach(event -> {
			switch (event.type()) {
			case challenge -> newChallenge(event);
			case gameStart -> startGame(event);
			case challengeCanceled, challengeDeclined -> logger.info("Challenge cancelled / declined: {}", event);
			case gameFinish -> cleanFinishedGame(event);
			}
		});
	}

	void newChallenge(Event event) {
		var challengeEvent = (Event.ChallengeEvent) event;
		logger.info("New challenge received. Details: {}", challengeEvent.challenge());
		if (!isChallengeAcceptable(challengeEvent)) {
			declineChallenge(challengeEvent);
		} else {
			acceptChallenge(challengeEvent);
		}
	}

	boolean isChallengeAcceptable(Event.ChallengeEvent challengeEvent) {
		Predicate<Challenge> challengerName = challenge -> challenge.challenger().name().equalsIgnoreCase("teemoo7");
		Predicate<Challenge> variantStandard = challenge -> challenge.variant().key() == Enums.GameVariant.standard;

		return challengerName.and(variantStandard).test(challengeEvent.challenge());
	}

	void acceptChallenge(Event.ChallengeEvent challengeEvent) {
		logger.info("Accepting challenge!");

		var bot = botFactory.getStrongestBot();
		var challenger = new Human(challengeEvent.challenge().challenger().name());

		Game game;
		if (challengeEvent.challenge().finalColor() == Enums.Color.white) {
			game = new Game(challenger, bot);
		} else {
			game = new Game(bot, challenger);
		}
		var onlineGame = new OnlineGame(challengeEvent.id(), game, bot);
		onlineGameMap.put(onlineGame.id(), onlineGame);

		client.acceptChallenge(challengeEvent.id());
	}

	void declineChallenge(Event.ChallengeEvent challengeEvent) {
		logger.info("Challenge is not acceptable, declining...");
		client.declineChallenge(challengeEvent.id());
	}

	void cleanFinishedGame(Event event) {
		logger.info("Game finished, cleaning memory: {}", event);
		onlineGameMap.remove(event.id());
	}

	void startGame(Event event) {
		logger.info("Game to start: {}", event);

		if (!onlineGameMap.containsKey(event.id())) {
			logger.warn("Game {} is not in memory, resigning", event.id());
			client.resign(event.id());
			return;
		}

		var onlineGame = onlineGameMap.get(event.id());

		logger.info("Ready to play game with id {}, waiting for events...", event.id());
		Stream<GameEvent> gameEvents = client.streamGameEvents(event.id());

		gameEvents.forEach(gameEvent -> {
			switch (gameEvent.type()) {
			case gameFull -> startPlayingGame(onlineGame, (GameEvent.Full) gameEvent);
			case chatLine -> incomingChatLine((GameEvent.Chat) gameEvent);
			case gameState -> updateGameState(onlineGame, (GameEvent.State) gameEvent);
			}
		});
	}

	void startPlayingGame(OnlineGame onlineGame, GameEvent.Full full) {
		logger.info("Game starting: {}", full);
		processMovesHistory(onlineGame.game(), full.state().moves());
		playNextMove(onlineGame);
	}

	void incomingChatLine(GameEvent.Chat chat) {
		logger.info("Chat: [{}] -> {}", chat.username(), chat.text());
	}

	void updateGameState(OnlineGame onlineGame, GameEvent.State state) {
		logger.info("Game state: {}", state);

		var gameId = onlineGame.id();
		var game = onlineGame.game();

		processMovesHistory(game, state.moves());

		var status = state.status();
		switch (status) {
		case mate, resign, outoftime -> {
			if (game.getPlayerToPlay().isBot()) {
				sendChatMessage(gameId,
					"That's what chess is all about. One day you give your opponent a lesson, the next day he gives you one.");
			} else {
				sendChatMessage(gameId, "I like the moment when I break a man's ego.");
			}
		}
		case stalemate, draw -> sendChatMessage(gameId, "Draws make for dull chess, wins make for fighting chess.");
		case started, created -> playNextMove(onlineGame);
		default -> {
		}
		}
	}

	void playNextMove(OnlineGame onlineGame) {
		var game = onlineGame.game();
		if (game.getPlayerToPlay().isBot()) {
			var move = ((Bot) game.getPlayerToPlay()).selectMove(game);
			sendMove(onlineGame.id(), move.getUciNotation());
			applyMove(game, move);
		}
	}

	void processMovesHistory(Game game, String uciMovesString) {
		if (!uciMovesString.isBlank()) {
			List<String> uciMoves = Arrays.stream(uciMovesString.split("\\s")).toList();
			if (game.getHistory().size() < uciMoves.size()) {
				String lastUciMove = uciMoves.get(uciMoves.size() - 1);
				Move move = uciService.getMoveFromUciNotation(lastUciMove, game);
				applyMove(game, move);
			}
		}
	}

	void applyMove(Game game, Move move) {
		game.getBoard().doMove(move);
		game.setToPlay(swap(move.getPiece().getColor()));
		game.addMoveToHistory(move);
	}

	void sendMove(String gameId, String moveUci) {
		logger.info("Sending move: {}", moveUci);
		client.move(gameId, moveUci);
	}

	void sendChatMessage(String gameId, String message) {
		logger.info("Chat: [{}] -> {}", "bobby-bot", message);
		client.chat(gameId, message);
	}
}
