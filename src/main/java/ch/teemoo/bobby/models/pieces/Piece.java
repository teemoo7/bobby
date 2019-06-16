package ch.teemoo.bobby.models.pieces;

import java.util.UUID;

import ch.teemoo.bobby.models.Color;

public abstract class Piece {
    final protected Color color;
    final protected int value;
    protected UUID id;

    public Piece(Color color, int value) {
        this.color = color;
        this.value = value;
        this.id = UUID.randomUUID();
    }

    abstract public String getUnicode();

    public Color getColor() {
        return color;
    }

    public int getValue() {
        return value;
    }

	public UUID getId() {
		return id;
	}

	protected void setId(UUID id) {
    	// for cloning with same ID
    	this.id = id;
	}

	public String toString() {
        return this.color + " " + this.getClass().getSimpleName();
    }

    public abstract Piece copy();
}
