package ch.teemoo.bobby.models.players;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.*;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.services.MoveService;

public class BeginnerBot extends Bot {
    private final static int WORST = -1000;
    private final static int BEST = 1000;
    private final static int NEUTRAL = 0;
    private final static int[][] heatmapCenter = generateCenteredHeatmap();

    public BeginnerBot() {
        super("Beginner Bot");
    }

    public Move selectMove(Game game, MoveService moveService) {
        Board board = game.getBoard();
        Color color = game.getToPlay();
        List<Move> history = game.getHistory();
        return selectMove(board, color, history, moveService, 2);
    }

    private Move selectMove(Board board, Color color, List<Move> history, MoveService moveService, int depth) {
        // Evaluate each move given the points of the pieces and the checkmate possibility, then select highest

        List<Move> moves = moveService.computeAllMoves(board, color, true);
        Map<Move, Integer> moveScores = new ConcurrentHashMap<>(moves.size());

        final Color opponentColor = swap(color);
        final Position opponentKingOriginalPosition = moveService.findKingPosition(board, opponentColor)
            .orElseThrow(() -> new RuntimeException("King expected here"));

        for(Move move: moves) {
            Position opponentKingPosition = opponentKingOriginalPosition;
            Board boardAfter = board.clone();
            boardAfter.doMove(move);
            List<Move> historyCopy = new ArrayList<>(history);
            historyCopy.add(move);
            final GameState gameState = moveService.getGameState(boardAfter, opponentColor, historyCopy);

            int score = evaluateBoard(boardAfter, color, color, gameState, opponentKingPosition, moveService);
            if (score >= BEST) {
                return move;
            }

            // Compute the probable next move for the opponent and see if our current move is a real benefit in the end
            if (depth >= 1 && gameState.isInProgress()) {
                Move opponentMove = selectMove(boardAfter, opponentColor, historyCopy, moveService, depth-1);
                boardAfter.doMove(opponentMove);
                historyCopy.add(opponentMove);
                if (opponentMove.getPiece() instanceof King) {
                    // We must consider the current king position
                    opponentKingPosition = new Position(opponentMove.getToX(), opponentMove.getToY());
                }
                final GameState gameStateAfterOpponent = moveService.getGameState(boardAfter, color, historyCopy);
                score = evaluateBoard(boardAfter, color, opponentColor, gameStateAfterOpponent, opponentKingPosition, moveService);

                if (depth >= 2 && gameStateAfterOpponent.isInProgress()) {
                    //todo: determine which pieces move must be evaluated to reduce computation time
                    Move nextMove = selectMove(boardAfter, color, historyCopy, moveService, depth - 2);
                    boardAfter.doMove(nextMove);
                    historyCopy.add(nextMove);
                    final GameState gameStateAfterOpponentAfterMove = moveService.getGameState(boardAfter, opponentColor, historyCopy);
                    int scoreAfterOpponentAfterMove = evaluateBoard(boardAfter, color, color, gameStateAfterOpponentAfterMove, opponentKingPosition, moveService);
                    score += 0.5 * scoreAfterOpponentAfterMove;
                }
            }
            moveScores.put(move, score);
        }
        if (depth == 2) {
            //todo: for debugging
            System.out.println(moveScores.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).map(e -> e.getKey().toString() + "=" + e.getValue().toString()).collect(
                    Collectors.joining(", ")));
        }
        return getBestMove(moveScores);
    }

    private int evaluateBoard(Board board, Color colorToEvaluate, Color lastPlayer, GameState gameState, Position opponentKingPosition, MoveService moveService) {
        int gameStateScore = NEUTRAL;
        if (!gameState.isInProgress()) {
            // Game is over
            if (gameState.isLost()) {
                if (lastPlayer == colorToEvaluate) {
                    // Opponent is checkmate, that the best move to do!
                    gameStateScore = BEST;
                } else {
                    // I am checkmate, that the worst move to do!
                    gameStateScore = WORST;
                }
            } else if (gameState.isDraw()) {
                // Let us be aggressive, a draw is not a good move, we want to win
                gameStateScore -= 20;
            }
            return gameStateScore;
        }

        // Basically, taking a piece improves your situation
        int piecesValue = getPiecesValueSum(board, colorToEvaluate);
        int opponentPiecesValue = getPiecesValueSum(board, swap(colorToEvaluate));
        int piecesScore = piecesValue-opponentPiecesValue;

        //fixme: we should compute moves for pawns in case of taking, not straight moves
        List<Move> allMoves = moveService.computeAllMoves(board, colorToEvaluate, false);
        int[][] heatmapOpponentKing = getHeatmapAroundLocation(opponentKingPosition.getX(), opponentKingPosition.getY());
        int heatScore = allMoves.stream().mapToInt(
            m -> heatmapCenter[m.getToX()][m.getToY()] + heatmapOpponentKing[m.getToX()][m.getToY()]).sum();

        int score = gameStateScore + 10 * piecesScore + heatScore;
        return score;
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

    private static int[][] generateCenteredHeatmap() {
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
}
