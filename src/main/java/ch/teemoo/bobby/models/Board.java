package ch.teemoo.bobby.models;

import java.util.Optional;

import ch.teemoo.bobby.models.pieces.*;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class Board {
    public final static int SIZE = 8;

    private final Piece[][] board;

    //fixme: do not store pieces array (mutable internal representation)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Board(Piece[][] board) {
        this.board = board;
    }

    public Board(String representation) {
        this.board = fromString(representation);
    }

    //fixme: do not expose pieces array (mutable internal representation)
    @SuppressFBWarnings("EI_EXPOSE_REP")
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
        } else if (move instanceof EnPassantMove) {
            EnPassantMove enPassantMove = (EnPassantMove) move;
            removePiece(enPassantMove.getTookPiecePosX(), enPassantMove.getTookPiecePosY());
        }
    }

    public void undoMove(Move move) {
        removePiece(move.getToX(), move.getToY());
        Piece piece = move.getPiece();
        setPiece(move.getFromX(), move.getFromY(), piece);
        if (move.isTaking()) {
            if (move instanceof EnPassantMove) {
                EnPassantMove enPassantMove = (EnPassantMove) move;
                setPiece(enPassantMove.getTookPiecePosX(), enPassantMove.getTookPiecePosY(),
                    enPassantMove.getTookPiece());
            } else {
                setPiece(move.getToX(), move.getToY(), move.getTookPiece());
            }
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

    private Piece[][] fromString(String value) {
        Piece[][] pieces = new Piece[SIZE][SIZE];
        String[] lines = value.split("\n");
        assert lines.length == SIZE;
        for (int i = 0; i < SIZE; i++) {
            String line = lines[i];
            // Every piece is followed by a space, just ignore the spaces
            for (int j = 0; j < SIZE; j++) {
                char c = line.charAt(2 * j);
                if (c != ' ') {
                    pieces[SIZE-1-i][j] = Piece.fromUnicodeChar(c);
                }
            }
        }
        return pieces;
    }
}
