import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import gui.BoardView;
import gui.Square;
import models.Board;
import models.Color;
import models.Move;
import models.pieces.Bishop;
import models.pieces.King;
import models.pieces.Knight;
import models.pieces.Pawn;
import models.pieces.Piece;
import models.pieces.Queen;
import models.pieces.Rook;

public class Bobby {
    public static void main(String args[]) {
        final MoveService moveService = new MoveService();

        //todo: use MVC Swing to display computed moves when piece is clicked
        Board board = new Board(getInitialPiecesPositions());

        BoardView boardView = new BoardView("Bobby chess game");
        boardView.display(board.getBoard());

        final Border redBorder = BorderFactory.createLineBorder(java.awt.Color.red);
        final Border blueBorder = BorderFactory.createLineBorder(java.awt.Color.blue);

        Square[][] squares = boardView.getSquares();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square square = squares[i][j];
                square.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        //todo: clear current selection
                        cleanSquaresBorder(squares);

                        //todo: if selection active, perform move, then clear

                        square.setBorder(redBorder);
                        List<Move> moves = moveService.computeMoves(board, square.getPiece(), square.getPosition().getX(), square.getPosition().getY(), false);
                        for (Move move: moves) {
                            squares[move.getToY()][move.getToX()].setBorder(blueBorder);
                        }
                    }
                });
            }
        }
    }

    private static void cleanSquaresBorder(Square[][] squares) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square square = squares[i][j];
                square.setBorder(BorderFactory.createEmptyBorder());
            }
        }
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
