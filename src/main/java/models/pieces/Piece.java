package models.pieces;

import models.Color;

public abstract class Piece {
    final protected Color color;
    final protected int value;

    public Piece(Color color, int value) {
        this.color = color;
        this.value = value;
    }

    abstract public String getUnicode();

    public Color getColor() {
        return color;
    }

    public String toString() {
        return this.color + " " + this.getClass().getSimpleName();
    }

    public abstract Piece clone();
}
