package models;

import java.util.Optional;

import models.pieces.Piece;

public class Board {
    public final static int RANKS = 8;
    public final static int FILES = 8;

    private final Piece[][] board = new Piece[RANKS][FILES];

    public Optional<Piece> getPiece(int x, int y) {
        return Optional.ofNullable(board[x][y]);
    }

    public String toString() {
        //for (Piece[] )
        return "";
    }

    public Board clone() {
        Board clone = new Board();
        for (int i = 0; i < RANKS; i++) {
            for (int j = 0; j < FILES; j++) {
                clone.setPiece(i, j, getPiece(i, j).clone());
            }
        }
        return clone;
    }

    public Board withMove(Move move) {

        setPiece(move.get);
    }

    private void setPiece(int x, int y, Piece piece) {
        board[x][y] = piece;
    }

    private Optional<Piece> removePiece(int x, int y) {
        Optional<Piece> toRemove = getPiece(x, y);
        board[x][y] = null;
        return toRemove;
    }

}
