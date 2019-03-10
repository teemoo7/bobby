import gui.BoardView;
import models.Board;
import models.Color;
import models.Game;
import models.HumanPlayer;
import models.pieces.Bishop;
import models.pieces.King;
import models.pieces.Knight;
import models.pieces.Pawn;
import models.pieces.Piece;
import models.pieces.Queen;
import models.pieces.Rook;

public class Bobby {
    public static void main(String args[]) {
        Game game = new Game(new HumanPlayer("Player 1"), new HumanPlayer("Player 2"));
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, game);
    }
}
