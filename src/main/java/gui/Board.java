package gui;

import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.JFrame;

import models.pieces.Piece;


public class Board extends JFrame {
    private final Container contentPane;

    public Board(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.contentPane = getContentPane();
        int sizeWithLabels = 10;
        GridLayout gridLayout = new GridLayout(sizeWithLabels, sizeWithLabels);
        contentPane.setLayout(gridLayout);
        setSize(600, 600);
        setLocationRelativeTo(null);
    }

    public void display(Piece[][] positions) {
        addFilesLabels();
        Background background;
        for (int i = positions.length - 1; i >= 0 ; i--) {
            contentPane.add(getRankLabel(i));
            background = getFirstSquareBackground(i);
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                Square square;
                if (piece != null) {
                    square = new Square(piece.getUnicode(), background);
                } else {
                    square = new Square(background);
                }
                background = swapBackground(background);
                contentPane.add(square);
            }
            contentPane.add(getRankLabel(i));
        }
        addFilesLabels();
        setVisible(true);
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
