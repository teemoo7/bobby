package ch.teemoo.bobby;

import ch.teemoo.bobby.gui.BoardView;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.players.BeginnerBot;
import ch.teemoo.bobby.models.players.Human;

public class Bobby {
    public static void main(String args[]) {
        //Game game = new Game(new Human("Player 1"), new Human("Player 2"));
        Game game = new Game(new Human("Player 1"), new BeginnerBot());
        //Game game = new Game(new BeginnerBot(), new BeginnerBot());
        //Game game = new Game(new BeginnerBot(), new RandomBot());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
