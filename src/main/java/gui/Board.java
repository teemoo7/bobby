package gui;

import models.pieces.Piece;

import java.awt.*;
import javax.swing.JFrame;


public class Board extends JFrame {
    private final int SIZE = 8;
    private final Container contentPane;

    public Board(String title) {
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.contentPane = getContentPane();
        GridLayout gridLayout = new GridLayout(SIZE, SIZE);
        contentPane.setLayout(gridLayout);
        setSize(600, 600);
        setLocationRelativeTo(null);

    }

    public void display(Piece[][] positions) {
        Background background;
        for (int i = positions.length - 1; i >= 0 ; i--) {
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
        }
        setVisible(true);
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
