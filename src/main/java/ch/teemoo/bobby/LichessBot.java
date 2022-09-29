package ch.teemoo.bobby;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.lichess.EventHandler;
import ch.teemoo.bobby.lichess.LichessClient;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import ch.teemoo.bobby.services.PortableGameNotationService;
import ch.teemoo.bobby.services.UniversalChessInterfaceService;
import chariot.Client;
import io.github.cdimascio.dotenv.Dotenv;

public class LichessBot implements Runnable {

	private final LichessClient client = new LichessClient(Client.auth(Dotenv.configure().load().get("LICHESS_TOKEN")));
	private final MoveService moveService = new MoveService();
	private final FileService fileService = new FileService();
	private final PortableGameNotationService pgnService = new PortableGameNotationService(moveService);
	private final UniversalChessInterfaceService uciService = new UniversalChessInterfaceService(moveService);
	final OpeningService openingService = new OpeningService(pgnService, fileService);
	final BotFactory botFactory = new BotFactory(moveService, openingService);

	public static void main(String[] args) {
		Runnable lichessBot = new LichessBot();
		lichessBot.run();
	}

	@Override
	public void run() {
		EventHandler eventHandler = new EventHandler(client, uciService, botFactory);

		Runtime.getRuntime().addShutdownHook(new Thread(eventHandler::clean));

		eventHandler.start();
	}

}
