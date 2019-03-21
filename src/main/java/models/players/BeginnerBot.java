package models.players;

import static helpers.ColorHelper.swap;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import models.Board;
import models.Color;
import models.Game;
import models.GameState;
import models.Move;
import models.pieces.Piece;
import services.MoveService;

public class BeginnerBot extends Bot {
    public BeginnerBot() {
        super("Beginner Bot");
    }

    public Move selectMove(Game game, MoveService moveService) {
        // Evaluate each move given the points of the pieces and the checkmate possibility, then select highest
        Board board = game.getBoard();
        Color color = game.getToPlay();

        List<Move> moves = moveService.computeAllMoves(board, color);

        int highestScore = Integer.MIN_VALUE;
        Move bestMove = null;
        for(Move move: moves) {
            Board boardAfter = board.clone();
            boardAfter.doMove(move);
            List<Move> historyCopy = new ArrayList<>(game.getHistory());
            historyCopy.add(move);
            final Color opponentColor = swap(move.getPiece().getColor());
            int myScore = getPiecesScore(boardAfter, move.getPiece().getColor());
            int opponentScore = getPiecesScore(boardAfter, opponentColor);

            // Checking is a good direction, add a bonus
            if (move.isChecking()) {
                myScore += 2;
            }

            GameState gameState = moveService.getGameState(boardAfter, opponentColor, historyCopy);
            if (gameState == GameState.LOSS) {
                // Opponent is checkmate, that the best move to do!
                myScore = Integer.MAX_VALUE;
            }

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
