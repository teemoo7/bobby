package gui;

import static models.Board.SIZE;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.*;

import models.Position;
import models.pieces.Piece;


public class BoardView extends JFrame {
    private final Container contentPane;
    private Square[][] squares = new Square[SIZE][SIZE];

    private JMenuItem itemSave;
    private JMenuItem itemLoad;

    public BoardView(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.contentPane = getContentPane();
        int sizeWithLabels = 10;
        GridLayout gridLayout = new GridLayout(sizeWithLabels, sizeWithLabels);
        contentPane.setLayout(gridLayout);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setMenu();
    }

    public Square[][] getSquares() {
        return squares;
    }

    public void setItemSaveActionListener(ActionListener actionListener) {
        itemSave.addActionListener(actionListener);
    }

    public void setItemLoadActionListener(ActionListener actionListener) {
        itemLoad.addActionListener(actionListener);
    }

    public void display(Piece[][] positions) {
        addFilesLabels();
        Background background;
        for (int i = positions.length - 1; i >= 0 ; i--) {
            contentPane.add(getRankLabel(i));
            background = getFirstSquareBackground(i);
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                // Inverse coordinates (positions is a 2D array, reversed)
                Square square = new Square(piece, new Position(j, i), background);
                background = swapBackground(background);
                contentPane.add(square);
                squares[i][j] = square;
            }
            contentPane.add(getRankLabel(i));
        }
        addFilesLabels();
        setVisible(true);
    }

    public void refresh(Piece[][] positions) {
        for (int i = positions.length - 1; i >= 0 ; i--) {
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                // get current square, so that only its label is updated
                squares[i][j].setPiece(piece);
            }
        }
    }

    public void popupInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void popupError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void setMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem itemExit = new JMenuItem("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(actionEvent -> System.exit(0));

        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);

        this.itemSave = new JMenuItem("Save");
        gameMenu.add(itemSave);

        this.itemLoad = new JMenuItem("Load");
        gameMenu.add(itemLoad);
    }

    private void addFilesLabels() {
        contentPane.add(new SideLabel(""));
        contentPane.add(new SideLabel("a"));
        contentPane.add(new SideLabel("b"));
        contentPane.add(new SideLabel("c"));
        contentPane.add(new SideLabel("d"));
        contentPane.add(new SideLabel("e"));
        contentPane.add(new SideLabel("f"));
        contentPane.add(new SideLabel("g"));
        contentPane.add(new SideLabel("h"));
        contentPane.add(new SideLabel(""));
    }

    private SideLabel getRankLabel(int i) {
        return new SideLabel(String.valueOf(i+1));
    }

    private Background swapBackground(Background background) {
        if (background == Background.DARK) {
            return Background.LIGHT;
        } else {
            return Background.DARK;
        }
    }

    private Background getFirstSquareBackground(int i) {
        Background background;
        if (i % 2 == 1) {
            background = Background.DARK;
        } else {
            background = Background.LIGHT;
        }
        return background;
    }
}
