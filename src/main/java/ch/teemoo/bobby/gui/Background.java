package ch.teemoo.bobby.gui;

import java.awt.*;

public enum Background {
    DARK(new Color(139, 69, 19)), LIGHT(new Color(222, 184, 135));

    private final Color color;

    Background(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}