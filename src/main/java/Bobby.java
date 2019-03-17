import gui.BoardView;
import models.Game;
import models.HumanPlayer;

public class Bobby {
    public static void main(String args[]) {
        Game game = new Game(new HumanPlayer("Player 1"), new HumanPlayer("Player 2"));
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
        gameController.play();
    }
}
