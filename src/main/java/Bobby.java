import gui.BoardView;
import models.players.BeginnerBot;
import models.Game;
import models.players.Human;
import models.players.RandomBot;

public class Bobby {
    public static void main(String args[]) {
        //Game game = new Game(new Human("Player 1"), new Human("Player 2"));
        //Game game = new Game(new Human("Player 1"), new RandomBot());
        Game game = new Game(new BeginnerBot(), new RandomBot());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
