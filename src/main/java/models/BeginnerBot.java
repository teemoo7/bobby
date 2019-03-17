package models;

import models.pieces.Piece;

import java.util.*;

import static helpers.ColorHelper.swap;

public class BeginnerBot extends Bot {
    public BeginnerBot() {
        super("Beginner Bot");
    }

    public Move selectMove(List<Move> moves, Board board) {
        // Evaluate each move given the points of the pieces and the checkmate possibility, then select highest

        int highestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move move: moves) {
            Board boardAfter = board.clone();
            boardAfter.doMove(move);
            int myScore = getPiecesScore(boardAfter, move.getPiece().getColor());
            int opponentScore = getPiecesScore(boardAfter, swap(move.getPiece().getColor()));
            //todo: add checkmate / check
            int score = myScore-opponentScore;
            if (score > highestScore) {
                bestMove = move;
                highestScore = score;
            }
        }
        return bestMove;
    }

    private int getPiecesScore(Board board, Color color) {
        int score = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Optional<Piece> pieceOpt = board.getPiece(i, j);
                if (pieceOpt.isPresent() && pieceOpt.get().getColor() == color) {
                    score += pieceOpt.get().getValue();
                }
            }
        }
        return score;
    }
}
