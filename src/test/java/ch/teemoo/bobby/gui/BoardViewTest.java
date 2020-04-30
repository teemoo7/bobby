package ch.teemoo.bobby.gui;

import ch.teemoo.bobby.helpers.GuiHelper;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.junit.Before;
import org.junit.Test;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.assertj.core.api.Assertions.assertThat;

public class BoardViewTest {
    private GuiHelper guiHelper = new GuiHelper();

    private BoardView view;

    @Before
    public void setUp() {
        view = new BoardView("Test board", guiHelper, false);
    }

    @Test
    public void testBoardViewConstructor() {
        assertThat(view.getTitle()).isEqualTo("Test board");
        assertThat(view.getContentPane().getLayout()).isInstanceOf(GridLayout.class);
        GridLayout gridLayout = (GridLayout) view.getContentPane().getLayout();
        assertThat(gridLayout.getColumns()).isEqualTo(gridLayout.getRows()).isEqualTo(10);
    }

    @Test
    public void testDisplay() {
        Piece[][] pieces = new Piece[8][8];
        Piece rook = new Rook(Color.WHITE);
        pieces[2][4] = rook;
        view.display(pieces, false);
        Square rookSquare = view.getSquares()[2][4];
        Square emptySquare = view.getSquares()[2][3];
        assertThat(rookSquare.getPiece()).isEqualTo(rook);
        assertThat(emptySquare.getPiece()).isNull();
        assertThat(rookSquare.getBackground()).isNotEqualTo(emptySquare.getBackground());
        assertThat(((SideLabel) view.getContentPane().getComponent(1)).getText()).isEqualTo("a");
    }

    @Test
    public void testDisplayReversed() {
        Piece[][] pieces = new Piece[8][8];
        Piece rook = new Rook(Color.WHITE);
        pieces[2][4] = rook;
        view.display(pieces, true);
        Square rookSquare = view.getSquares()[2][4];
        Square emptySquare = view.getSquares()[2][3];
        assertThat(rookSquare.getPiece()).isEqualTo(rook);
        assertThat(emptySquare.getPiece()).isNull();
        assertThat(rookSquare.getBackground()).isNotEqualTo(emptySquare.getBackground());
        assertThat(view.getContentPane().getComponent(1)).isInstanceOf(SideLabel.class);
        assertThat(((SideLabel) view.getContentPane().getComponent(1)).getText()).isEqualTo("h");
    }

    @Test
    public void testRefresh() {
        Piece[][] pieces = new Piece[8][8];
        Piece rook = new Rook(Color.WHITE);
        pieces[2][4] = rook;
        view.display(pieces, false);
        assertThat(view.getSquares()[2][4].getPiece()).isEqualTo(rook);
        Piece[][] noPieces = new Piece[8][8];
        view.refresh(noPieces);
        assertThat(view.getSquares()[2][4].getPiece()).isNull();
    }

    @Test
    public void testResetAllClickables() {
        Piece[][] noPieces = new Piece[8][8];
        view.display(noPieces, false);
        view.getSquares()[2][4].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
            }
        });
        assertThat(view.getSquares()[2][4].getMouseListeners()).hasSize(1);
        view.resetAllClickables();
        assertThat(view.getSquares()[2][4].getMouseListeners()).hasSize(0);
    }

    @Test
    public void testCleanSquareBorder() {
        Piece[][] noPieces = new Piece[8][8];
        view.display(noPieces, false);
        view.getSquares()[2][4].setBorder(new LineBorder(java.awt.Color.BLUE));
        assertThat(view.getSquares()[2][4].getBorder()).isInstanceOf(LineBorder.class);
        view.cleanSquaresBorder();
        assertThat(view.getSquares()[2][4].getBorder()).isInstanceOf(EmptyBorder.class);
    }

    @Test
    public void testAddBorderToLastMoveSquares() {
        Piece[][] noPieces = new Piece[8][8];
        view.display(noPieces, false);
        assertThat(view.getSquares()[2][4].getBorder()).isNull();
        assertThat(view.getSquares()[3][4].getBorder()).isNull();
        view.addBorderToLastMoveSquares(new Move(new Queen(Color.WHITE), 4, 2, 4, 3));
        assertThat(view.getSquares()[2][4].getBorder()).isInstanceOf(LineBorder.class);
        assertThat(view.getSquares()[3][4].getBorder()).isInstanceOf(LineBorder.class);
    }
}
