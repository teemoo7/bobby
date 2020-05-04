package ch.teemoo.bobby.models.pieces;

import ch.teemoo.bobby.models.Color;

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

    public int getValue() {
        return value;
    }

	public String toString() {
        return this.color + " " + this.getClass().getSimpleName();
    }

    public abstract Piece copy();

    public static Piece fromUnicodeChar(char c) {
        Piece piece;
        switch (c) {
            case '♜':
                piece = new Rook(Color.BLACK);
                break;
            case '♞':
                piece = new Knight(Color.BLACK);
                break;
            case '♝':
                piece = new Bishop(Color.BLACK);
                break;
            case '♛':
                piece = new Queen(Color.BLACK);
                break;
            case '♚':
                piece = new King(Color.BLACK);
                break;
            case '♟':
                piece = new Pawn(Color.BLACK);
                break;
            case '♖':
                piece = new Rook(Color.WHITE);
                break;
            case '♘':
                piece = new Knight(Color.WHITE);
                break;
            case '♗':
                piece = new Bishop(Color.WHITE);
                break;
            case '♕':
                piece = new Queen(Color.WHITE);
                break;
            case '♔':
                piece = new King(Color.WHITE);
                break;
            case '♙':
                piece = new Pawn(Color.WHITE);
                break;
            default:
                throw new IllegalArgumentException("Unexpected piece unicode: " + c);
        }
        return piece;
    }
}
