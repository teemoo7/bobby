package ch.teemoo.bobby.lichess;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.players.Bot;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.services.UniversalChessInterfaceService;
import chariot.model.Challenge;
import chariot.model.ChallengeResult;
import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerTest {
	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

	@Mock
	private LichessClient client;

	@Mock
	private UniversalChessInterfaceService uciService;

	@Mock
	private BotFactory botFactory;

	@InjectMocks
	private EventHandler eventHandler;

	@Test
	public void testCleanEmpty() {
		// given
		var game = new OnlineGame("test", null, null);
		eventHandler.onlineGameMap.put(game.id(), game);

		// when
		eventHandler.clean();

		// then
		assertThat(systemOutRule.getLog()).contains("Cleaning before exit");
		verify(client).abort(game.id());
	}

	@Test
	public void testStartWithChallengeToBeAccepted() {
		// given
		var challenge = generateStandardChallengeWithDefaultPlayers();
		var eventChallenge = new Event.ChallengeEvent(Event.Type.challenge, challenge, new Event.Compat(true, false));
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));
		var bot = mock(Bot.class);
		when(botFactory.getStrongestBot()).thenReturn(bot);

		// when
		eventHandler.start();

		// then
		assertThat(eventHandler.onlineGameMap.containsKey(challenge.id())).isTrue();
		var onlineGame = eventHandler.onlineGameMap.get(challenge.id());
		assertThat(onlineGame).isNotNull();
		assertThat(onlineGame.id()).isEqualTo(challenge.id());
		assertThat(onlineGame.bot()).isEqualTo(bot);
		verify(client).acceptChallenge(challenge.id());
	}

	@Test
	public void testStartWithChallengeToBeDeclined() {
		// given
		Challenge challengeHordeVariant =
			new Challenge("hdasjkdh", "bullet", "http://localhost", "created", false, null, null, Enums.ColorPref.white,
				Enums.Color.white, new Challenge.Variant(Enums.GameVariant.horde, "Horde"), generatePlayer(),
				generateBotPlayer(), new ChallengeResult.Perf("", "Bullet"), Optional.empty(), Optional.empty(),
				List.of());
		var eventChallenge =
			new Event.ChallengeEvent(Event.Type.challenge, challengeHordeVariant, new Event.Compat(true, false));
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));

		// when
		eventHandler.start();

		// then
		verify(client).declineChallenge(challengeHordeVariant.id());
	}

	@Test
	public void testStartWithChallengeDeclined() {
		// given
		var eventChallenge =
			new Event.ChallengeEvent(Event.Type.challengeDeclined, generateStandardChallengeWithDefaultPlayers(),
				new Event.Compat(true, true));
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));

		// when
		eventHandler.start();

		// then
		assertThat(systemOutRule.getLog()).contains("Challenge cancelled / declined");
	}

	@Test
	public void testStartWithChallengeCanceled() {
		// given
		var eventChallenge =
			new Event.ChallengeEvent(Event.Type.challengeCanceled, generateStandardChallengeWithDefaultPlayers(),
				new Event.Compat(true, true));
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));

		// when
		eventHandler.start();

		// then
		assertThat(systemOutRule.getLog()).contains("Challenge cancelled / declined");
	}

	@Test
	public void testStartWithChallengeGameFinished() {
		// given
		var eventChallenge =
			new Event.ChallengeEvent(Event.Type.gameFinish, generateStandardChallengeWithDefaultPlayers(),
				new Event.Compat(true, true));
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));

		// when
		eventHandler.start();

		// then
		assertThat(systemOutRule.getLog()).contains("Game finished, cleaning memory");
		assertThat(eventHandler.onlineGameMap.containsKey(eventChallenge.id())).isFalse();
	}

	@Test
	public void testStartWithChallengeGameStartNotInMemory() {
		// given
		var eventChallenge = generateDefaultChallengeEventGameStart();
		when(client.streamEvents()).thenReturn(Stream.of(eventChallenge));
		var gameId = eventChallenge.challenge().id();

		// when
		eventHandler.start();

		// then
		assertThat(systemOutRule.getLog()).contains("Game to start");
		assertThat(systemOutRule.getLog()).contains("Game " + gameId + " is not in memory, resigning");
		verify(client).resign(gameId);
		verify(client, never()).streamGameEvents(anyString());
	}

	@Test
	public void testStartGameStreamGameEvents() {
		// given
		var eventChallenge = generateDefaultChallengeEventGameStart();
		var gameId = eventChallenge.challenge().id();
		var onlineGame = new OnlineGame(gameId, new Game(null, null), null);
		eventHandler.onlineGameMap.put(gameId, onlineGame);

		// when
		eventHandler.startGame(eventChallenge);

		// then
		verify(client).streamGameEvents(gameId);
	}

	@Test
	public void testStartGameChatLine() {
		// given
		var eventChallenge = generateDefaultChallengeEventGameStart();
		var gameId = eventChallenge.challenge().id();
		var onlineGame = new OnlineGame(gameId, new Game(null, null), null);
		eventHandler.onlineGameMap.put(gameId, onlineGame);
		var username = "spectator";
		var text = "hello chess world";
		var gameEvent = new GameEvent.Chat(GameEvent.Type.chatLine, username, text, null);
		when(client.streamGameEvents(gameId)).thenReturn(Stream.of(gameEvent));

		// when
		eventHandler.startGame(eventChallenge);

		// then
		assertThat(systemOutRule.getLog()).contains("Chat: [" + username + "] -> " + text);
	}

	@Test
	public void testStartGameFull() {
		// given
		var eventChallenge = generateDefaultChallengeEventGameStart();
		var gameId = eventChallenge.challenge().id();
		var onlineGame = new OnlineGame(gameId, new Game(new Human("opponent"), mock(Bot.class)), null);
		eventHandler.onlineGameMap.put(gameId, onlineGame);
		var gameEvent =
			new GameEvent.Full(GameEvent.Type.gameFull, gameId, eventChallenge.challenge().rated(), null, null,
				"bullet", null, null, null, null, null, generateGameState("", chariot.model.Game.Status.created));
		when(client.streamGameEvents(gameId)).thenReturn(Stream.of(gameEvent));

		// when
		eventHandler.startGame(eventChallenge);

		// then
		assertThat(systemOutRule.getLog()).contains("Game starting");
	}

	@Test
	public void testStartGameState() {
		// given
		var eventChallenge = generateDefaultChallengeEventGameStart();
		var gameId = eventChallenge.challenge().id();
		var onlineGame = new OnlineGame(gameId, new Game(new Human("opponent"), mock(Bot.class)), null);
		eventHandler.onlineGameMap.put(gameId, onlineGame);
		var gameEvent = generateGameState("", chariot.model.Game.Status.aborted);
		when(client.streamGameEvents(gameId)).thenReturn(Stream.of(gameEvent));

		// when
		eventHandler.startGame(eventChallenge);

		// then
		assertThat(systemOutRule.getLog()).contains("Game state");
	}

	@Test
	public void testUpdateGameStateWithMoveHistory() {
		// given
		var uciMoves = "e2e4";
		var bot = mock(Bot.class);
		var game = new Game(new Human("player"), bot);
		var onlineGame = new OnlineGame("1234", game, bot);
		var move = new Move(new Pawn(Color.WHITE), 4, 1, 4, 3);
		when(uciService.getMoveFromUciNotation(uciMoves, game)).thenReturn(move);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState(uciMoves, chariot.model.Game.Status.aborted));

		// then
		assertThat(game.getHistory()).containsExactly(move);
		assertThat(game.getBoard().getPiece(move.getToX(), move.getToY())).isPresent().get().isEqualTo(move.getPiece());
		assertThat(game.getToPlay()).isEqualTo(Color.BLACK);
	}

	@Test
	public void testUpdateGameStateWithBotPlayNextMove() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(bot, new Human("player"));
		var onlineGame = new OnlineGame("1234", game, bot);
		var move = new Move(new Pawn(Color.WHITE), 4, 1, 4, 3);
		when(bot.isBot()).thenReturn(true);
		when(bot.selectMove(game)).thenReturn(move);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.created));

		// then
		verify(client).move(onlineGame.id(), "e2e4");
		assertThat(game.getHistory()).containsExactly(move);
		assertThat(game.getBoard().getPiece(move.getToX(), move.getToY())).isPresent().get().isEqualTo(move.getPiece());
		assertThat(game.getToPlay()).isEqualTo(Color.BLACK);
	}

	@Test
	public void testUpdateGameStateWithHumanPlayNextMove() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(new Human("player"), bot);
		var onlineGame = new OnlineGame("1234", game, bot);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.started));

		// then
		verify(client, never()).move(anyString(), anyString());
		assertThat(game.getHistory()).isEmpty();
		assertThat(game.getToPlay()).isEqualTo(Color.WHITE);
	}

	@Test
	public void testUpdateGameStateStalemate() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(new Human("player"), bot);
		var onlineGame = new OnlineGame("1234", game, bot);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.stalemate));

		// then
		verify(client).chat(eq(onlineGame.id()), anyString());
	}

	@Test
	public void testUpdateGameStateDraw() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(new Human("player"), bot);
		var onlineGame = new OnlineGame("1234", game, bot);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.draw));

		// then
		verify(client).chat(eq(onlineGame.id()), anyString());
	}

	@Test
	public void testUpdateGameStateMate() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(new Human("player"), bot);
		var onlineGame = new OnlineGame("1234", game, bot);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.mate));

		// then
		verify(client).chat(eq(onlineGame.id()), anyString());
	}

	@Test
	public void testUpdateGameStateResign() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(bot, new Human("player"));
		var onlineGame = new OnlineGame("1234", game, bot);
		when(bot.isBot()).thenReturn(true);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.resign));

		// then
		verify(client).chat(eq(onlineGame.id()), anyString());
	}

	@Test
	public void testUpdateGameStateOutOfTime() {
		// given
		var bot = mock(Bot.class);
		var game = new Game(bot, new Human("player"));
		var onlineGame = new OnlineGame("1234", game, bot);
		when(bot.isBot()).thenReturn(true);

		// when
		eventHandler.updateGameState(onlineGame, generateGameState("", chariot.model.Game.Status.outoftime));

		// then
		verify(client).chat(eq(onlineGame.id()), anyString());
	}

	private GameEvent.State generateGameState(String uciMoves, chariot.model.Game.Status status) {
		return new GameEvent.State(GameEvent.Type.gameState, uciMoves, 0L, 0L, 0L, 0L, false, false, false, false,
			status, null);
	}

	private Event.ChallengeEvent generateDefaultChallengeEventGameStart() {
		return new Event.ChallengeEvent(Event.Type.gameStart, generateStandardChallengeWithDefaultPlayers(),
			new Event.Compat(true, true));
	}

	private Challenge generateStandardChallengeWithDefaultPlayers() {
		return generateStandardChallenge(generatePlayer(), generateBotPlayer());
	}

	private Challenge generateStandardChallenge(ChallengeResult.Player challengerPlayer,
		ChallengeResult.Player botPlayer) {
		return new Challenge("hdasjkdh", "bullet", "http://localhost", "created", false, null, null,
			Enums.ColorPref.white, Enums.Color.white, new Challenge.Variant(Enums.GameVariant.standard, "Standard"),
			challengerPlayer, botPlayer, new ChallengeResult.Perf("", "Bullet"), Optional.empty(), Optional.empty(),
			List.of());
	}

	private ChallengeResult.Player generatePlayer() {
		return new ChallengeResult.Player("teemoo7", "teemoo7", null, true, true, 1500, false, null);
	}

	private ChallengeResult.Player generateBotPlayer() {
		return new ChallengeResult.Player("bobby-bot", "bobby-bot", "BOT", true, true, 1500, false, null);
	}

}
