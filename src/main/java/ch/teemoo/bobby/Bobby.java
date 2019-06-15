package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.models.players.Human;

public class Bobby {
    public static void main(String args[]) {
        //Game game = new Game(new Human("Player 1"), new Human("Player 2"));
        Game game = new Game(new Human("Player 1"), new TraditionalBot(2));
        //Game game = new Game(new TraditionalBot(2), new TraditionalBot(1));
        //Game game = new Game(new TraditionalBot(1), new RandomBot());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
