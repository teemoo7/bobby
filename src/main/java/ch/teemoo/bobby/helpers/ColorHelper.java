package ch.teemoo.bobby.helpers;

import ch.teemoo.bobby.models.Color;

public class ColorHelper {
    private ColorHelper() {
    }

    public static Color swap(Color color) {
        if (color == Color.WHITE) {
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }
}
