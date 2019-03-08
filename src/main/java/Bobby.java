import gui.BoardView;
import models.Board;
import models.Color;
import models.pieces.Bishop;
import models.pieces.King;
import models.pieces.Knight;
import models.pieces.Pawn;
import models.pieces.Piece;
import models.pieces.Queen;
import models.pieces.Rook;

public class Bobby {
    public static void main(String args[]) {
        Board board = new Board(getInitialPiecesPositions());
        BoardView boardView = new BoardView("Bobby chess game");
        GameController gameController = new GameController(boardView, board);
    }

    private static Piece[][] getInitialPiecesPositions() {
        Piece[][] pos = new Piece[8][8];
        pos[0][0] = new Rook(Color.WHITE);
        pos[0][1] = new Knight(Color.WHITE);
        pos[0][2] = new Bishop(Color.WHITE);
        pos[0][3] = new Queen(Color.WHITE);
        pos[0][4] = new King(Color.WHITE);
        pos[0][5] = new Bishop(Color.WHITE);
        pos[0][6] = new Knight(Color.WHITE);
        pos[0][7] = new Rook(Color.WHITE);
        pos[1][0] = new Pawn(Color.WHITE);
        pos[1][1] = new Pawn(Color.WHITE);
        pos[1][2] = new Pawn(Color.WHITE);
        pos[1][3] = new Pawn(Color.WHITE);
        pos[1][4] = new Pawn(Color.WHITE);
        pos[1][5] = new Pawn(Color.WHITE);
        pos[1][6] = new Pawn(Color.WHITE);
        pos[1][7] = new Pawn(Color.WHITE);

        pos[7][0] = new Rook(Color.BLACK);
        pos[7][1] = new Knight(Color.BLACK);
        pos[7][2] = new Bishop(Color.BLACK);
        pos[7][3] = new Queen(Color.BLACK);
        pos[7][4] = new King(Color.BLACK);
        pos[7][5] = new Bishop(Color.BLACK);
        pos[7][6] = new Knight(Color.BLACK);
        pos[7][7] = new Rook(Color.BLACK);
        pos[6][0] = new Pawn(Color.BLACK);
        pos[6][1] = new Pawn(Color.BLACK);
        pos[6][2] = new Pawn(Color.BLACK);
        pos[6][3] = new Pawn(Color.BLACK);
        pos[6][4] = new Pawn(Color.BLACK);
        pos[6][5] = new Pawn(Color.BLACK);
        pos[6][6] = new Pawn(Color.BLACK);
        pos[6][7] = new Pawn(Color.BLACK);

        return pos;
    }
}
