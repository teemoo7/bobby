package ch.teemoo.bobby.models.players;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;

import java.util.*;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.GameState;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.services.MoveService;

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

        List<Move> moves = moveService.computeAllMoves(board, color, true);
        Map<Move, Integer> moveScores = new HashMap<>(moves.size());

        for(Move move: moves) {
            Board boardAfter = board.clone();
            boardAfter.doMove(move);
            List<Move> historyCopy = new ArrayList<>(history);
            historyCopy.add(move);
            final Color opponentColor = swap(move.getPiece().getColor());
            final GameState gameState = moveService.getGameState(boardAfter, opponentColor, historyCopy);

            int gameStateScore = NEUTRAL;
            if (gameState.isLost()) {
                // Opponent is checkmate, that the best move to do!
                gameStateScore = BEST;
            } else if (gameState.isDraw()) {
                // Let us be aggressive, a draw is not a good move, we want to win
                gameStateScore -= 20;
            }

            // Compute the probable next move for the opponent and see if our current move is a real benefit in the end
            if (depth > 0 && gameState.isInProgress()) {
                Move opponentMove = selectMove(boardAfter, opponentColor, historyCopy, moveService, depth-1);
                boardAfter.doMove(opponentMove);
                historyCopy.add(opponentMove);
                final GameState gameStateAfterOpponent = moveService.getGameState(boardAfter, color, historyCopy);
                if (gameStateAfterOpponent.isLost()) {
                    // I am checkmate, that the worst move to do!
                    gameStateScore = WORST;
                } else if (gameStateAfterOpponent.isDraw()) {
                    // Let us be aggressive, a draw is not a good move, we want to win
                    gameStateScore -= 20;
                }
            }

            // Basically, taking a piece improves your situation
            int piecesValue = getPiecesValueSum(boardAfter, move.getPiece().getColor());
            int opponentPiecesValue = getPiecesValueSum(boardAfter, opponentColor);
            int piecesScore = piecesValue-opponentPiecesValue;

            /*
            // Checking is a good direction, add a bonus
            if (move.isChecking()) {
                score += 2;

                //todo: following heuristic is not bad but has a major drawback: it is not based on current situation
                //todo: but on history too

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
            */

            /*
            if (move.getPiece() instanceof King) {
                // In many cases, moving the king is a poor choice, except when rooking (not implemented yet), add malus
                score -= 2;
            }
            */

            //todo: compute a the fire heat map to see which squares are controlled (under fire) and which squares are
            //todo: important to control (centered).
            //fixme: we should compute moves for pawns in case of taking, not straight moves
            List<Move> allMoves = moveService.computeAllMoves(boardAfter, color, false);
            int[][] heatmapCenter = getHeatmapForCenter();
            Position opponentKing = moveService.findKingPosition(boardAfter, opponentColor)
                .orElseThrow(() -> new RuntimeException("King expected here"));
            int[][] heatmapOpponentKing = getHeatmapAroundLocation(opponentKing.getX(), opponentKing.getY());
            int heatScore = allMoves.stream().mapToInt(
                m -> heatmapCenter[m.getToX()][m.getToY()] + heatmapOpponentKing[m.getToX()][m.getToY()]).sum();

            int score = 1 * gameStateScore + 4 * piecesScore + 1 * heatScore;
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
        return getMaxScoreWithRandomChoice(moveScores)
                .orElseThrow(() -> new RuntimeException("At least one move must be done"));
    }

    private Optional<Move> getMaxScoreWithRandomChoice(Map<Move, Integer> moveScores) {
        // Instead of just search for the max score, we search for all moves that have the max score, and if there are
        // more than one move, then we randomly choose one. It shall give a bit of variation in games.
        if (moveScores.isEmpty()) {
            return Optional.empty();
        }
        List<Move> bestMoves = new ArrayList<>();
        Integer highestScore = null;
        for (Map.Entry<Move, Integer> entry: moveScores.entrySet()) {
            if (highestScore == null || entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestMoves.clear();
                bestMoves.add(entry.getKey());
            } else if (entry.getValue() == highestScore) {
                bestMoves.add(entry.getKey());
            }
        }
        return Optional.of(bestMoves.get(new Random().nextInt(bestMoves.size())));
    }

    private <K, V extends Comparable<V>> Optional<Map.Entry<K,V>> getMaxValue(Map<K, V> map) {
        return map.entrySet()
            .stream()
            .max(Comparator.comparing(Map.Entry::getValue));
    }

    private int[][] getHeatmapForCenter() {
        int[][] heatmap = new int[Board.SIZE][Board.SIZE];
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int heat = 0;
                if ((i == 3 || i == 4) && (j == 3 | j == 4)) {
                    heat = 2;
                } else if ((i == 2 || i == 5) && (j == 2 | j == 5)) {
                    heat = 1;
                }
                heatmap[i][j] = heat;
            }
        }
        return heatmap;
    }

    private int[][] getHeatmapAroundLocation(int x, int y) {
        int[][] heatmap = new int[Board.SIZE][Board.SIZE];
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                int distanceToHeat = Math.max(Math.abs(x-i), Math.abs(y-j));
                int heat;
                switch (distanceToHeat) {
                case 0:
                    heat = 3;
                    break;
                case 1:
                    heat = 2;
                    break;
                case 2:
                    heat = 1;
                    break;
                default:
                    heat = 0;
                }
                heatmap[i][j] = heat;
            }
        }
        return heatmap;
    }
}
