package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.helpers.GameFactory;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.GameSetup;
import ch.teemoo.bobby.models.players.ExperiencedBot;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import ch.teemoo.bobby.services.PortableGameNotationService;

import javax.swing.*;

public class Bobby implements Runnable {
    private final MoveService moveService = new MoveService();
    private final FileService fileService = new FileService();
    private final PortableGameNotationService portableGameNotationService =
        new PortableGameNotationService(fileService, moveService);
    private final OpeningService openingService = new OpeningService(portableGameNotationService, fileService);
    private final GameFactory gameFactory = new GameFactory();
    private final BotFactory botFactory = new BotFactory(moveService, openingService);
    private final boolean useDefaultGameSetup;

    public Bobby(boolean useDefaultGameSetup) {
        this.useDefaultGameSetup = useDefaultGameSetup;
    }

    public static void main(String args[]) {
        boolean defaultSetup = false;
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("default")) {
                defaultSetup = true;
            }
        }
        SwingUtilities.invokeLater(new Bobby(defaultSetup));
    }

    public void run() {
        GameSetup gameSetup = null;
        if (useDefaultGameSetup) {
            gameSetup = new GameSetup(new Human("Player"), botFactory.getStrongestBot());
        }
        BoardView boardView = new BoardView("Bobby chess game");
        new GameController(boardView, gameSetup, gameFactory, botFactory, moveService, fileService);
    }
}
