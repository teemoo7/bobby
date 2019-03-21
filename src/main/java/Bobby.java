import gui.BoardView;
import models.players.BeginnerBot;
import models.Game;
import models.players.RandomBot;

public class Bobby {
    public static void main(String args[]) {
        //Game game = new Game(new HumanPlayer("Player 1"), new HumanPlayer("Player 2"));
        //Game game = new Game(new HumanPlayer("Player 1"), new RandomBot());
        Game game = new Game(new BeginnerBot(), new RandomBot());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
