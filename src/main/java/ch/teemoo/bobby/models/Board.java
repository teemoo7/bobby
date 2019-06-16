package ch.teemoo.bobby.models;

import java.util.Optional;

import ch.teemoo.bobby.models.pieces.Piece;

public class Board {
    public final static int SIZE = 8;

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
        StringBuilder builder = new StringBuilder();
        for (int i = SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < SIZE; j++) {
                builder.append(" ");
                Optional<Piece> piece = getPiece(j, i);
                if (piece.isPresent()) {
                    builder.append(piece.get().getUnicode());
                } else {
                    builder.append(" ");
                }
                builder.append(" ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    public Board copy() {
        Board clone = new Board(new Piece[SIZE][SIZE]);
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Optional<Piece> piece = getPiece(i, j);
                if (piece.isPresent()) {
                    clone.setPiece(i, j, piece.get().copy());
                }
            }
        }
        return clone;
    }

    public void doMove(Move move) {
        removePiece(move.getFromX(), move.getFromY());
        Piece piece = move.getPiece();
        if (move instanceof PromotionMove) {
            PromotionMove promotionMove = (PromotionMove) move;
            piece = promotionMove.getPromotedPiece();
        }
        setPiece(move.getToX(), move.getToY(), piece);
        if (move instanceof CastlingMove) {
            CastlingMove castlingMove = (CastlingMove) move;
            removePiece(castlingMove.getRookFromX(), castlingMove.getRookFromY());
            setPiece(castlingMove.getRookToX(), castlingMove.getRookToY(), castlingMove.getRook());
        }
    }

    public void undoMove(Move move) {
        removePiece(move.getToX(), move.getToY());
        Piece piece = move.getPiece();
        setPiece(move.getFromX(), move.getFromY(), piece);
        if (move.isTaking()) {
            setPiece(move.getToX(), move.getToY(), move.getTookPiece());
        }
        if (move instanceof CastlingMove) {
            CastlingMove castlingMove = (CastlingMove) move;
            removePiece(castlingMove.getRookToX(), castlingMove.getRookToY());
            setPiece(castlingMove.getRookFromX(), castlingMove.getRookFromY(), castlingMove.getRook());
        }
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
