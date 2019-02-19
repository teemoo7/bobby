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
                Optional<Piece> piece = getPiece(i, j);
                if (piece.isPresent()) {
                    clone.setPiece(i, j, piece.get().clone());
                }
            }
        }
        return clone;
    }

    //todo: builder
    public void withMove(Move move) {
        removePiece(move.getFromX(), move.getFromY());
        setPiece(move.getToX(), move.getToY(), move.getPiece());
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
