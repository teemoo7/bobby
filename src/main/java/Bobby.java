import gui.BoardView;
import models.Game;
import models.HumanPlayer;
import models.RandomBot;

public class Bobby {
    public static void main(String args[]) {
        //Game game = new Game(new HumanPlayer("Player 1"), new HumanPlayer("Player 2"));
        Game game = new Game(new HumanPlayer("Player 1"), new RandomBot());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
