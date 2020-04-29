package ch.teemoo.bobby;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.helpers.GameFactory;
import ch.teemoo.bobby.models.GameSetup;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import ch.teemoo.bobby.services.PortableGameNotationService;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bobby implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(Bobby.class);

    private final MoveService moveService = new MoveService();
    private final FileService fileService = new FileService();
    private final PortableGameNotationService portableGameNotationService =
        new PortableGameNotationService(moveService);
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
        setLookAndFeel();
        SwingUtilities.invokeLater(new Bobby(defaultSetup));
    }

    public void run() {
        GameSetup gameSetup = null;
        if (useDefaultGameSetup) {
            gameSetup = new GameSetup(new Human("Player"), botFactory.getStrongestBot());
        }
        BoardView boardView = new BoardView("Bobby chess game");
        new GameController(boardView, gameSetup, gameFactory, botFactory, moveService, fileService,
            portableGameNotationService);
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());
        }
        catch (Exception e) {
            logger.warn("Unable to set system Look and Feel", e);
        }
    }
}
