package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.helpers.GameFactory;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.GameSetup;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.services.FileService;
import ch.teemoo.bobby.services.MoveService;

import javax.swing.*;

public class Bobby implements Runnable {
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Bobby());
    }

    public void run() {
//        GameSetup gameSetup = new GameSetup(new Human("Player 1"), new TraditionalBot(2));
        GameSetup gameSetup = null;
        BoardView boardView = new BoardView("Bobby chess game");
        new GameController(boardView, gameSetup, new GameFactory(), new MoveService(), new FileService());
    }
}
