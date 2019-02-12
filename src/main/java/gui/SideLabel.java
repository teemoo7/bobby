package gui;

import javax.swing.*;
import java.awt.*;


class SideLabel extends JLabel {

    SideLabel(String label) {
        super(label);
        setFont(new Font("DejaVu Sans", Font.PLAIN, 16));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
    }
}
