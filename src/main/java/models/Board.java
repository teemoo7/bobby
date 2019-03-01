package models;

import java.util.Optional;

import models.pieces.Piece;

public class Board {
    public final static int RANKS = 8;
    public final static int FILES = 8;

    private final Piece[][] board;

    public Board(Piece[][] board) {
        this.board = board;
    }

    public Piece[][] getBoard() {
        return board;
    }

    public Optional<Piece> getPiece(int x, int y) {
        return Optional.ofNullable(board[y][x]);
    }

    public String toString() {
        //for (Piece[] )
        return "";
    }

    public Board clone() {
        Board clone = new Board(new Piece[FILES][RANKS]);
        for (int i = 0; i < FILES; i++) {
            for (int j = 0; j < RANKS; j++) {
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
        board[y][x] = piece;
    }

    private Optional<Piece> removePiece(int x, int y) {
        Optional<Piece> toRemove = getPiece(x, y);
        board[y][x] = null;
        return toRemove;
    }

}
