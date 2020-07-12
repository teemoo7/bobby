package ch.teemoo.bobby;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import ch.teemoo.bobby.gui.IBoardView;
import ch.teemoo.bobby.gui.NoBoardView;
import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.helpers.GameFactory;
import ch.teemoo.bobby.models.games.GameResult;
import ch.teemoo.bobby.models.games.GameSetup;
import ch.teemoo.bobby.models.tournaments.Match;
import ch.teemoo.bobby.models.tournaments.Tournament;
import ch.teemoo.bobby.models.players.ExperiencedBot;
import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import ch.teemoo.bobby.services.PortableGameNotationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TournamentOrganizer implements Runnable {
	private final static Logger logger = LoggerFactory.getLogger(TournamentOrganizer.class);

	private static final int ROUNDS_PER_MATCH = 4;

	private final MoveService moveService = new MoveService();
	private final FileService fileService = new FileService();
	private final PortableGameNotationService portableGameNotationService =
		new PortableGameNotationService(moveService);
	private final OpeningService openingService = new OpeningService(portableGameNotationService, fileService);
	private final GameFactory gameFactory = new GameFactory();
	private final BotFactory botFactory = new BotFactory(moveService, openingService);

	private final boolean fastTournament;

	public static void main(String args[]) {
		Runnable tournament = new TournamentOrganizer(false);
		tournament.run();
	}

	public TournamentOrganizer(boolean fastTournament) {
		this.fastTournament = fastTournament;
	}

	public void run() {
		IBoardView boardView = new NoBoardView();
		GameController controller = new GameController(boardView, gameFactory, botFactory, moveService, fileService,
			portableGameNotationService);

		List<Player> players;
		int rounds;
		if (fastTournament) {
			players = getOnlyTwoFastPlayers();
			rounds = 2;
		} else {
			players = getAllPlayers();
			rounds = ROUNDS_PER_MATCH;
		}
		logger.info("{} players has registered", players.size());
		logger.info("There will be {} rounds per match, participants play against each other", players.size());

		logger.info("Tournament is open!");
		Tournament tournament = new Tournament(players);
		for (Match match: tournament.getMatches()) {
			for (int i = 0; i < rounds; i++) {
				playRound(controller, match, i % 2 == 0);
			}
		}

		logger.info("Tournament is over!");
		for (Match match: tournament.getMatches()) {
			logger.info("Match result:\n{}", match.toString());
		}
		logger.info("Here is the scoreboard:\n{}", tournament.getScoreboard());
	}

	List<Player> getAllPlayers() {
		return Arrays.asList(
			new RandomBot(moveService),
			new TraditionalBot(0, null, moveService),
			new TraditionalBot(1, null, moveService),
			new TraditionalBot(2, null, moveService),
			new ExperiencedBot(2, null, moveService, openingService)
		);
	}

	List<Player> getOnlyTwoFastPlayers() {
		return Arrays.asList(
			new RandomBot(moveService),
			new TraditionalBot(0, null, moveService)
		);
	}

	void playRound(GameController controller, Match match, boolean swapPlayersInitialPosition) {
		CompletableFuture<GameResult> completableFutureGameOver = new CompletableFuture<>();
		GameSetup gameSetup;
		if (swapPlayersInitialPosition) {
			gameSetup = new GameSetup(match.getPlayer2(), match.getPlayer1());
		} else {
			gameSetup = new GameSetup(match.getPlayer1(), match.getPlayer2());
		}
		controller.newGame(gameSetup, true, completableFutureGameOver::complete);
		try {
			GameResult gameResult = completableFutureGameOver.get();
			int nbMoves = gameResult.getNbMoves();
			switch (gameResult.getResult()) {
			case DRAW:
				match.addDraw(nbMoves);
				break;
			case WHITE_WINS:
				match.addWin(gameSetup.getWhitePlayer(), nbMoves);
				break;
			case BLACK_WINS:
				match.addWin(gameSetup.getBlackPlayer(), nbMoves);
				break;
			}
			logger.info("Game over: {}", gameResult.getNbMoves());
		} catch (InterruptedException | ExecutionException e) {
			logger.error("Game simulation failed, skipping...", e);
		}
	}
}
