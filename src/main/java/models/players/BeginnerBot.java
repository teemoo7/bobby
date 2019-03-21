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
            int score = 0;

            // Basically, taking a piece improves your situation
            final Color opponentColor = swap(move.getPiece().getColor());
            int myScore = getPiecesScore(boardAfter, move.getPiece().getColor());
            int opponentScore = getPiecesScore(boardAfter, opponentColor);
            score += myScore-opponentScore;

            // Checking is a good direction, add a bonus
            if (move.isChecking()) {
                score += 2;

                // To checkmate, the idea is to reduce the king's moves. One may see that if we had put the king under
                // check at the previous move, and that we can put him under check now with another piece than before,
                // we focus the fire on the king proximity (since the king moves by one square only)
                if (historyCopy.size() > 3) {
                    Move previousMove = historyCopy.get(historyCopy.size() - 3);
                    if (previousMove.isChecking() && !previousMove.getPiece().equals(move.getPiece())) {
                        score += 10;
                    }
                }
            }

            GameState gameState = moveService.getGameState(boardAfter, opponentColor, historyCopy);
            if (gameState == GameState.LOSS) {
                // Opponent is checkmate, that the best move to do!
                score = Integer.MAX_VALUE;
            } else if (gameState == GameState.DRAW_50_MOVES || gameState == GameState.DRAW_STALEMATE || gameState == GameState.DRAW_THREEFOLD) {
                // Let us be aggressive, a draw is not a good move, we want to win
                score -= 2;
            }

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
