package gui;

import javax.swing.*;
import java.awt.*;


class Square extends JLabel {

    Square(String pieceUnicode, Background background) {
        super(pieceUnicode);
        setFont(new Font("DejaVu Sans", Font.PLAIN, 48));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setBackground(background.getColor());
    }

    Square(Background background) {
        this("", background);
    }
}
