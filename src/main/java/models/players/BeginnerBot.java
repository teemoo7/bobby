package models.players;

import static helpers.ColorHelper.swap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import models.Board;
import models.Color;
import models.Game;
import models.GameState;
import models.Move;
import models.pieces.Piece;
import services.MoveService;

public class BeginnerBot extends Bot {
    private final static int WORST = -1000;
    private final static int BEST = 1000;
    private final static int NEUTRAL = 0;

    public BeginnerBot() {
        super("Beginner Bot");
    }

    public Move selectMove(Game game, MoveService moveService) {
        Board board = game.getBoard();
        Color color = game.getToPlay();
        List<Move> history = game.getHistory();
        return selectMove(board, color, history, moveService, 1);
    }

    public Move selectMove(Board board, Color color, List<Move> history, MoveService moveService, int depth) {
        // Evaluate each move given the points of the pieces and the checkmate possibility, then select highest

        List<Move> moves = moveService.computeAllMoves(board, color);
        Map<Move, Integer> moveScores = new HashMap<>(moves.size());

        for(Move move: moves) {
            int score = NEUTRAL;

            Board boardAfter = board.clone();
            boardAfter.doMove(move);
            List<Move> historyCopy = new ArrayList<>(history);
            historyCopy.add(move);
            final Color opponentColor = swap(move.getPiece().getColor());
            final GameState gameState = moveService.getGameState(boardAfter, opponentColor, historyCopy);

            if (gameState.isLost()) {
                // Opponent is checkmate, that the best move to do!
                score = BEST;
            } else if (gameState.isDraw()) {
                // Let us be aggressive, a draw is not a good move, we want to win
                score -= 20;
            }

            // Compute the probable next move for the opponent and see if our current move is a real benefit in the end
            if (depth > 0 && gameState.isInProgress()) {
                Move opponentMove = selectMove(boardAfter, opponentColor, historyCopy, moveService, depth-1);
                boardAfter.doMove(opponentMove);
                historyCopy.add(opponentMove);
                final GameState gameStateAfterOpponent = moveService.getGameState(boardAfter, color, historyCopy);
                if (gameStateAfterOpponent.isLost()) {
                    // I am checkmate, that the worst move to do!
                    score = WORST;
                } else if (gameStateAfterOpponent.isDraw()) {
                    // Let us be aggressive, a draw is not a good move, we want to win
                    score -= 20;
                }
            }

            // Basically, taking a piece improves your situation
            int piecesValue = getPiecesValueSum(boardAfter, move.getPiece().getColor());
            int opponentPiecesValue = getPiecesValueSum(boardAfter, opponentColor);
            int deltaPiecesValue = piecesValue-opponentPiecesValue;

            score += deltaPiecesValue;

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

            moveScores.put(move, score);
        }
        return getBestMove(moveScores);
    }

    private int getPiecesValueSum(Board board, Color color) {
        int sum = 0;
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                Optional<Piece> pieceOpt = board.getPiece(i, j);
                if (pieceOpt.isPresent() && pieceOpt.get().getColor() == color) {
                    sum += pieceOpt.get().getValue();
                }
            }
        }
        return sum;
    }

    private Move getBestMove(Map<Move, Integer> moveScores) {
        //System.out.println(moveScores);
        return getMaxValue(moveScores).orElseThrow(() -> new RuntimeException("At least one move must be done"))
            .getKey();
    }

    private <K, V extends Comparable<V>> Optional<Map.Entry<K,V>> getMaxValue(Map<K, V> map) {
        return map.entrySet()
            .stream()
            .max(Comparator.comparing(Map.Entry::getValue));
    }
}
