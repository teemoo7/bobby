package ch.teemoo.bobby.services;

import static ch.teemoo.bobby.helpers.ColorHelper.swap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.MoveAnalysis;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.games.GameState;
import ch.teemoo.bobby.models.moves.CastlingMove;
import ch.teemoo.bobby.models.moves.EnPassantMove;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.RandomBot;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MoveServiceTest {

    private MoveService moveService;

    @BeforeEach
    public void setUp() {
        moveService = new MoveService();
    }

    @Test
    public void testSelectMoveCanCheckMate() {
        // Scenario: with a minimal depth, if a checkmate is possible, then it must be the selected move
        List<String> movesNotation = Arrays.asList(
                "e2-e3",
                "f7-f6",
                "f2-f4",
                "g7-g5"
        );
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Color colorToPlay = Color.WHITE;
        for (String notation: movesNotation) {
            Move move = Move.fromBasicNotation(notation, colorToPlay);
            Piece piece = game.getBoard().getPiece(move.getFromX(), move.getFromY())
                    .orElseThrow(() -> new RuntimeException("Unexpected move, no piece at this location"));
            move = new Move(piece, move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
            colorToPlay = swap(colorToPlay);
            game.getBoard().doMove(move);
            game.setToPlay(colorToPlay);
            game.addMoveToHistory(move);
        }
        Move bestMove = moveService.selectMove(game, 0, null);
        assertThat(bestMove.getBasicNotation()).isEqualTo("d1-h5+");

        game.getBoard().doMove(bestMove);
        game.addMoveToHistory(bestMove);
        assertThat(moveService.getGameState(game.getBoard(), swap(colorToPlay), game.getHistory())).isEqualTo(GameState.LOSS);
    }

    @Test
    public void testSelectMoveAvoidCheckMate() {
        // Scenario: with a depth of 1+, if the opponent can checkmate at next turn, then the selected move must avoid this, although the best move at first depth would be this one
        List<String> movesNotation = Arrays.asList(
                "e2-e3",
                "f7-f6",
                "f2-f4",
                "g7-g6",
                "d1-f3",
                "b8-c6",
                "f1-e2",
                "b7-b6",
                "f3-h5"
        );
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Color colorToPlay = Color.WHITE;
        for (String notation: movesNotation) {
            Move move = Move.fromBasicNotation(notation, colorToPlay);
            Piece piece = game.getBoard().getPiece(move.getFromX(), move.getFromY())
                    .orElseThrow(() -> new RuntimeException("Unexpected move, no piece at this location"));
            move = new Move(piece, move.getFromX(), move.getFromY(), move.getToX(), move.getToY());
            colorToPlay = swap(colorToPlay);
            game.getBoard().doMove(move);
            game.addMoveToHistory(move);
            game.setToPlay(colorToPlay);
        }
        // At this stage, the best move with the minimal depth (0) is to take the queen, but doing this will lead to
        // being checkmated at next turn, so the best move for a larger depth (at least 1) is not to take the queen
        Move naiveBestMove = moveService.selectMove(game, 0, null);
        assertThat(naiveBestMove.getBasicNotation()).isEqualTo("g6xh5");

        Move bestMove = moveService.selectMove(game, 1, null);
        assertThat(bestMove.getBasicNotation()).isNotEqualTo("g6xh5");
        game.getBoard().doMove(bestMove);
        game.addMoveToHistory(bestMove);
        assertThat(moveService.getGameState(game.getBoard(), swap(colorToPlay), game.getHistory())).isNotEqualTo(GameState.LOSS);
    }

    @Test
    public void testComputeAllMoves() {
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board initialBoard = game.getBoard();
        // Each player has 20 possible moves in initial position
        assertThat(moveService.computeAllMoves(initialBoard, Color.WHITE, game.getHistory(), false)).hasSize(20);
        assertThat(moveService.computeAllMoves(initialBoard, Color.BLACK, game.getHistory(),false)).hasSize(20);
    }

    @Test
    public void testComputeMoves() {
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board initialBoard = game.getBoard();
        // White rook cannot move in initial position
        assertThat(moveService.computeMoves(initialBoard, initialBoard.getPiece(0, 0).get(), 0, 0, game.getHistory(),false, false)).isEmpty();
        // White knight has two possible moves
        assertThat(moveService.computeMoves(initialBoard, initialBoard.getPiece(1, 0).get(), 1, 0, game.getHistory(),false, false)).hasSize(2);
        // Any pawn has two possible moves
        assertThat(moveService.computeMoves(initialBoard, initialBoard.getPiece(5, 1).get(), 5, 1, game.getHistory(),false, false)).hasSize(2);
    }

    @Test
    public void testGetGameStateInProgress() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝   ♜ \n" +
                "♟   ♟ ♟     ♕ ♟ \n" +
                "  ♟         ♟   \n" +
                "        ♟   ♘   \n" +
                "                \n" +
                "        ♙       \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖   ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getGameState(board, Color.BLACK, Lists.emptyList())).isEqualTo(GameState.IN_PROGRESS);
    }

    @Test
    public void testGetGameStateLossCheckmate() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝   ♜ \n" +
                "♟   ♟ ♟   ♕   ♟ \n" +
                "  ♟         ♟   \n" +
                "        ♟   ♘   \n" +
                "                \n" +
                "        ♙       \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖   ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getGameState(board, Color.BLACK, Lists.emptyList())).isEqualTo(GameState.LOSS);
    }

    @Test
    public void testGetGameStateDrawStalemate() {
        Board board = new Board("" +
                "        ♚       \n" +
                "            ♕   \n" +
                "                \n" +
                "♗           ♟   \n" +
                "        ♘   ♙ ♟ \n" +
                "        ♙     ♘ \n" +
                "♙ ♙ ♙         ♙ \n" +
                "♖       ♔ ♗   ♖ \n"
        );
        assertThat(moveService.getGameState(board, Color.BLACK, Lists.emptyList())).isEqualTo(GameState.DRAW_STALEMATE);
    }

    @Test
    public void testGetGameStateDraw50MovesNoCaptureNoPawn() {
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board board = game.getBoard();
        List<Move> history = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            Move moveWhite;
            Move moveBlack;
            if (i % 4 == 0) {
                moveWhite = new Move(board.getPiece(1, 0).get(), 1, 0, 2, 2);
                moveBlack = new Move(board.getPiece(1, 7).get(), 1, 7, 2, 5);
            } else if (i % 4 == 1) {
                moveWhite = new Move(board.getPiece(2, 2).get(), 2, 2, 1, 0);
                moveBlack = new Move(board.getPiece(2, 5).get(), 2, 5, 1, 7);
            } else if (i % 4 == 2) {
                moveWhite = new Move(board.getPiece(1, 0).get(), 1, 0, 0, 2);
                moveBlack = new Move(board.getPiece(1, 7).get(), 1, 7, 0, 5);
            } else {
                moveWhite = new Move(board.getPiece(0, 2).get(), 0, 2, 1, 0);
                moveBlack = new Move(board.getPiece(0, 5).get(), 0, 5, 1, 7);
            }
            history.add(moveWhite);
            board.doMove(moveWhite);
            history.add(moveBlack);
            board.doMove(moveBlack);
        }
        assertThat(moveService.getGameState(board, Color.WHITE, history)).isEqualTo(GameState.DRAW_50_MOVES);
    }

    @Test
    public void testGetGameStateDrawThreefold() {
        Board board = new Board("" +
                "      ♚     ♕   \n" +
                "                \n" +
                "                \n" +
                "            ♟   \n" +
                "        ♘   ♙ ♟ \n" +
                "        ♙     ♘ \n" +
                "♙ ♙ ♙ ♗       ♙ \n" +
                "♖       ♔ ♗   ♖ \n"
        );
        List<String> movesBasicNotation = Arrays.asList(
                "e2-e3", "f7-f6", "f2-f4", "e7-e5", "f4xe5", "f6xe5", "d1-f3", "g8-f6", "b1-c3", "f6-e4", "c3xe4",
                "g7-g6", "e4-g5", "d7-d5", "f3xd5", "c7-c6", "d5xc6+", "c8-d7", "c6xb7", "f8-b4", "b7xa8", "e5-e4",
                "g5xe4", "b8-c6", "a8xc6", "b4-c5", "c6xc5", "d7-e6", "c5xa7", "d8xd2+", "c1xd2", "e6-h3", "g1xh3",
                "h7-h5", "a7-h7", "g6-g5", "h7xh8+", "e8-d7", "g2-g4", "h5-h4", "h8-g7+", "d7-d8", "g7-g8+", "d8-d7",
                "g8-g7+", "d7-d8", "g7-g8+", "d8-d7", "g8-g7+", "d7-d8", "g7-g8+");
        List<Move> history = new ArrayList<>(movesBasicNotation.size());
        Color color = Color.WHITE;
        for (String notation: movesBasicNotation) {
            history.add(Move.fromBasicNotation(notation, color));
            color = swap(color);
        }
        assertThat(moveService.getGameState(board, Color.BLACK, history)).isEqualTo(GameState.DRAW_THREEFOLD);
    }

    @Test
    public void testFindKingPosition() {
        // Initial positions board
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        assertThat(moveService.findKingPosition(game.getBoard(), Color.WHITE)).isPresent().get().hasFieldOrPropertyWithValue("x", 4).hasFieldOrPropertyWithValue("y", 0);
        assertThat(moveService.findKingPosition(game.getBoard(), Color.BLACK)).isPresent().get().hasFieldOrPropertyWithValue("x", 4).hasFieldOrPropertyWithValue("y", 7);

        // Empty board
        Board emptyBoard = new Board(new Piece[8][8]);
        assertThat(moveService.findKingPosition(emptyBoard, Color.WHITE)).isEmpty();
        assertThat(moveService.findKingPosition(emptyBoard, Color.BLACK)).isEmpty();
    }

    @Test
    public void testEvaluateBoardLoss() {
        assertThat(moveService.evaluateBoard(null, Color.BLACK, Color.WHITE, GameState.LOSS, null, null, Collections.emptyList())).isEqualTo(MoveService.WORST);
    }

    @Test
    public void testEvaluateBoardWin() {
        assertThat(moveService.evaluateBoard(null, Color.BLACK, Color.BLACK, GameState.LOSS, null, null, Collections.emptyList())).isEqualTo(MoveService.BEST);
    }

    @Test
    public void testEvaluateBoardDraw() {
        assertThat(moveService.evaluateBoard(null, Color.BLACK, Color.BLACK, GameState.DRAW_STALEMATE, null, null, Collections.emptyList())).isEqualTo(-20);
    }

    @Test
    public void testEvaluateBoardInitialPosition() {
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        assertThat(moveService.evaluateBoard(game.getBoard(), Color.WHITE, Color.BLACK, GameState.IN_PROGRESS, new Position(4, 7), new Position(4, 0), game.getHistory())).isEqualTo(0);
    }

    @Test
    public void testGetPiecesValueSum() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "        ♚       \n" +
                "            ♟ ♕ \n" +
                "    ♗     ♙     \n" +
                "          ♘     \n" +
                "♛           ♙ ♙ \n" +
                "    ♖   ♔       \n"
        );
        assertThat(moveService.getPiecesValueSum(board, Color.BLACK)).isEqualTo(111);
        assertThat(moveService.getPiecesValueSum(board, Color.WHITE)).isEqualTo(124);
    }

    @Test
    public void testGetPiecesValueSumInitialPosition() {
        // Initial positions board
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board board = game.getBoard();
        assertThat(moveService.getPiecesValueSum(board, Color.WHITE)).isEqualTo(
                8 * (new Pawn(Color.WHITE)).getValue()
                + 2 * (new Knight(Color.WHITE)).getValue()
                + 2 * (new Bishop(Color.WHITE)).getValue()
                + 2 * (new Rook(Color.WHITE)).getValue()
                + (new Queen(Color.WHITE)).getValue()
                + (new King(Color.WHITE)).getValue()
                );

        assertThat(moveService.getPiecesValueSum(board, Color.WHITE)).isEqualTo(moveService.getPiecesValueSum(board, Color.BLACK));
    }

    @Test
    public void testGetDevelopmentBonusNormal() {
        assertThat(moveService.getDevelopmentBonus(Collections.emptyList())).isEqualTo(0);
    }

    @Test
    public void testGetDevelopmentBonusQueenMoveInOpening() {
        List<Move> moves = Arrays.asList(
            new Move(new Pawn(Color.WHITE), 3, 1, 3, 3),
            new Move(new Queen(Color.WHITE), 3, 0, 3, 2)
        );
        assertThat(moveService.getDevelopmentBonus(moves)).isEqualTo(-5);
    }

    @Test
    public void testGetDevelopmentBonusTwicePieceMovedInOpening() {
        Piece knight = new Knight(Color.WHITE);
        List<Move> moves = Arrays.asList(
            new Move(knight, 1, 0, 2, 2),
            new Move(knight, 2, 2, 3, 4)
        );
        assertThat(moveService.getDevelopmentBonus(moves)).isEqualTo(-5);
    }

    @Test
    public void testGetDevelopmentBonusCastling() {
        Piece king = new King(Color.WHITE);
        List<Move> moves = Arrays.asList(
            new CastlingMove(king, 4, 0, 2, 0, new Rook(Color.WHITE), 0, 0, 3, 0)
        );
        assertThat(moveService.getDevelopmentBonus(moves)).isEqualTo(15);
    }

    @Test
    public void testGetDevelopmentBonusKingShouldNotMoveBeforeCastling() {
        List<Move> moves = Arrays.asList(
            new Move(new Pawn(Color.WHITE), 3, 1, 3, 2),
            new Move(new Pawn(Color.WHITE), 3, 2, 3, 3),
            new Move(new Pawn(Color.WHITE), 3, 3, 3, 4),
            new Move(new Pawn(Color.WHITE), 3, 4, 3, 5),
            new Move(new Pawn(Color.WHITE), 3, 5, 3, 6),
            new Move(new King(Color.WHITE), 4, 0, 4, 1)
            );
        assertThat(moveService.getDevelopmentBonus(moves)).isEqualTo(-10);
    }

    @Test
    public void testGetBestMove() {
        Map<MoveAnalysis, Integer> map = new HashMap<>();
        map.put(new MoveAnalysis(new Move(new Bishop(Color.BLACK), 4, 5, 5, 6)), 60);
        map.put(new MoveAnalysis(new Move(new Bishop(Color.BLACK), 4, 5, 3, 4)), 23);
        map.put(new MoveAnalysis(new Move(new Bishop(Color.BLACK), 4, 5, 6, 7)), 45);
        map.put(new MoveAnalysis(new Move(new Knight(Color.BLACK), 2, 3, 4, 4)), -56);
        map.put(new MoveAnalysis(new Move(new Knight(Color.BLACK), 2, 3, 1, 5)), 20);
        map.put(new MoveAnalysis(new Move(new Knight(Color.BLACK), 2, 3, 0, 4)), 59);
        MoveAnalysis bestMove = new MoveAnalysis(new Move(new Queen(Color.BLACK), 6, 6, 6, 7));
        map.put(bestMove, 65);
        assertThat(moveService.getBestMove(map)).isEqualTo(bestMove);
    }

    @Test
    public void testGetBestMoveEmpty() {
        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> moveService.getBestMove(new HashMap<>()));
    }

    @Test
    public void testGetMaxScoreWithRandomChoice() {
        Map<MoveAnalysis, Integer> map = new HashMap<>();
        map.put(new MoveAnalysis(new Move(new Bishop(Color.WHITE), 4, 5, 5, 6)), -2);
        map.put(new MoveAnalysis(new Move(new Bishop(Color.WHITE), 4, 5, 3, 4)), 5);
        map.put(new MoveAnalysis(new Move(new Bishop(Color.WHITE), 4, 5, 6, 7)), 8);
        map.put(new MoveAnalysis(new Move(new Knight(Color.WHITE), 2, 3, 4, 4)), 8);
        map.put(new MoveAnalysis(new Move(new Knight(Color.WHITE), 2, 3, 1, 5)), 7);
        map.put(new MoveAnalysis(new Move(new Knight(Color.WHITE), 2, 3, 0, 4)), -2);
        Optional<MoveAnalysis> bestmove = moveService.getMaxScoreWithRandomChoice(map);
        assertThat(bestmove).isPresent();
        assertThat(map.get(bestmove.get())).isEqualTo(8);

    }

    @Test
    public void testGetMaxScoreWithRandomChoiceSingleElement() {
        Map<MoveAnalysis, Integer> map = new HashMap<>(1);
        MoveAnalysis moveAnalysis = new MoveAnalysis(new Move(new Bishop(Color.WHITE), 4, 5, 5, 6));
        map.put(moveAnalysis, 0);
        assertThat(moveService.getMaxScoreWithRandomChoice(map)).isPresent().get().isEqualTo(moveAnalysis);
    }

    @Test
    public void testGetMaxScoreWithRandomChoiceEmpty() {
        assertThat(moveService.getMaxScoreWithRandomChoice(new HashMap<>())).isEmpty();
    }

    @Test
    public void testCanMoveInitialPosition() {
        // Initial positions board
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board board = game.getBoard();
        assertThat(moveService.canMove(board, Color.WHITE, game.getHistory())).isTrue();
        assertThat(moveService.canMove(board, Color.BLACK, game.getHistory())).isTrue();
    }

    @Test
    public void testCanMoveCheckMate() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟ ♟ ♟     ♟ \n" +
                "          ♟     \n" +
                "            ♟ ♕ \n" +
                "          ♙     \n" +
                "        ♙       \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.canMove(board, Color.BLACK, Collections.emptyList())).isFalse();
    }

    @Test
    public void testComputeMovesBoardInitialPositions() {
        // Initial positions board
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board board = game.getBoard();
        List<Move> whiteMovesStart = moveService.computeBoardMoves(board, Color.WHITE, game.getHistory(),false, false, false);
        assertThat(whiteMovesStart).containsExactlyInAnyOrder(
                // Pawns
                new Move(board.getPiece(0, 1).get(), 0, 1, 0, 2),
                new Move(board.getPiece(0, 1).get(), 0, 1, 0, 3),
                new Move(board.getPiece(1, 1).get(), 1, 1, 1, 2),
                new Move(board.getPiece(1, 1).get(), 1, 1, 1, 3),
                new Move(board.getPiece(2, 1).get(), 2, 1, 2, 2),
                new Move(board.getPiece(2, 1).get(), 2, 1, 2, 3),
                new Move(board.getPiece(3, 1).get(), 3, 1, 3, 2),
                new Move(board.getPiece(3, 1).get(), 3, 1, 3, 3),
                new Move(board.getPiece(4, 1).get(), 4, 1, 4, 2),
                new Move(board.getPiece(4, 1).get(), 4, 1, 4, 3),
                new Move(board.getPiece(5, 1).get(), 5, 1, 5, 2),
                new Move(board.getPiece(5, 1).get(), 5, 1, 5, 3),
                new Move(board.getPiece(6, 1).get(), 6, 1, 6, 2),
                new Move(board.getPiece(6, 1).get(), 6, 1, 6, 3),
                new Move(board.getPiece(7, 1).get(), 7, 1, 7, 2),
                new Move(board.getPiece(7, 1).get(), 7, 1, 7, 3),
                // Knights
                new Move(board.getPiece(1, 0).get(), 1, 0, 0, 2),
                new Move(board.getPiece(1, 0).get(), 1, 0, 2, 2),
                new Move(board.getPiece(6, 0).get(), 6, 0, 5, 2),
                new Move(board.getPiece(6, 0).get(), 6, 0, 7, 2)
        );
    }

    @Test
    public void testComputeMovesBoardInitialPositionsReturnAtFirstPieceHavingMoves() {
        // Initial positions board
        Game game = new Game(new RandomBot(moveService), new RandomBot(moveService));
        Board board = game.getBoard();
        List<Move> whiteMovesStart = moveService.computeBoardMoves(board, Color.WHITE, game.getHistory(),false, true, false);
        assertThat(whiteMovesStart).containsExactlyInAnyOrder(
                new Move(board.getPiece(1, 0).get(), 1, 0, 0, 2),
                new Move(board.getPiece(1, 0).get(), 1, 0, 2, 2)
        );
    }

    @Test
    public void testIsInCheckNoKing() {
        // Empty board
        Board emptyBoard = new Board(new Piece[8][8]);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> moveService.isInCheck(emptyBoard, Color.WHITE)).withMessage("King not found");
    }

    @Test
    public void testIsInCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟   ♟ \n" +
                "          ♘ ♟   \n" +
                "        ♘       \n" +
                "  ♝     ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInCheck(board, Color.BLACK)).isTrue();
        assertThat(moveService.isInCheck(board, Color.WHITE)).isFalse();
    }

    @Test
    public void testComputePawnMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "    ♟   ♟       \n" +
                "      ♙         \n" +
                "                \n" +
                "  ♟             \n" +
                "♙               \n" +
                "                \n"
        );
        Piece pawn = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computePawnMoves(pawn, 3, 4, board, Collections.emptyList(), false);
        assertThat(moves).containsExactlyInAnyOrder(
                new Move(pawn, 3, 4, 3, 5),
                getMoveWithTookPiece(pawn, 3, 4, 2, 5, board.getPiece(2, 5).get()),
                getMoveWithTookPiece(pawn, 3, 4, 4, 5, board.getPiece(4, 5).get())
        );

        Piece pawnAtStart = board.getPiece(0, 1).get();
        moves = moveService.computePawnMoves(pawnAtStart, 0, 1, board, Collections.emptyList(), false);
        assertThat(moves).containsExactlyInAnyOrder(
                new Move(pawnAtStart, 0, 1, 0, 2),
                new Move(pawnAtStart, 0, 1, 0, 3),
                getMoveWithTookPiece(pawnAtStart, 0, 1, 1, 2, board.getPiece(1, 2).get())
        );
    }

    @Test
    public void testComputePawnMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "    ♙ ♟ ♟       \n" +
                "      ♙         \n" +
                "                \n" +
                "♙               \n" +
                "♙               \n" +
                "                \n"
        );
        Piece pawn = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computePawnMoves(pawn, 3, 4, board, Collections.emptyList(), false);
        assertThat(moves).containsExactlyInAnyOrder(getMoveWithTookPiece(pawn, 3, 4, 4, 5, board.getPiece(4, 5).get()));

        Piece pawnAtStart = board.getPiece(0, 1).get();
        moves = moveService.computePawnMoves(pawnAtStart, 0, 1, board, Collections.emptyList(), false);
        assertThat(moves).isEmpty();

        Piece blackPawn = board.getPiece(4, 5).get();
        moves = moveService.computePawnMoves(blackPawn, 4, 5, board, Collections.emptyList(), false);
        assertThat(moves).containsExactlyInAnyOrder(new Move(blackPawn, 4, 5, 4, 4), getMoveWithTookPiece(blackPawn, 4, 5, 3, 4, board.getPiece(3, 4).get()));
    }

    @Test
    public void testComputePawnMovesEnPassant() {
        Board board = new Board("" +
            "                \n" +
            "                \n" +
            "                \n" +
            "      ♙ ♟       \n" +
            "                \n" +
            "                \n" +
            "                \n" +
            "                \n"
        );
        Piece blackPawn = board.getPiece(4, 4).get();
        Move lastMove = new Move(blackPawn, 4, 6, 4, 4);
        List<Move> history = Collections.singletonList(lastMove);
        Piece pawn = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computePawnMoves(pawn, 3, 4, board, history, false);
        EnPassantMove enPassantMove = new EnPassantMove(new Move(pawn, 3, 4, 4, 5), 4, 4);
        enPassantMove.setTookPiece(blackPawn);
        assertThat(moves).containsExactlyInAnyOrder(new Move(pawn, 3, 4, 3, 5), enPassantMove);
    }

    @Test
    public void testComputeLShapeMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "                \n" +
                "      ♘         \n" +
                "                \n" +
                "                \n" +
                "                \n" +
                "                \n"
        );
        Piece knight = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeLShapeMoves(knight, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                new Move(knight, 3, 4, 4, 6),
                new Move(knight, 3, 4, 5, 5),
                new Move(knight, 3, 4, 4, 2),
                new Move(knight, 3, 4, 1, 5),
                new Move(knight, 3, 4, 2, 6),
                new Move(knight, 3, 4, 5, 3),
                new Move(knight, 3, 4, 2, 2),
                new Move(knight, 3, 4, 1, 3)
        );
    }

    @Test
    public void testComputeLShapeMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "    ♟   ♟       \n" +
                "  ♙       ♟     \n" +
                "      ♘         \n" +
                "  ♙       ♟     \n" +
                "    ♙   ♟       \n" +
                "                \n" +
                "                \n"
        );
        Piece knight = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeLShapeMoves(knight, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                getMoveWithTookPiece(knight, 3, 4, 4, 6, board.getPiece(4, 6).get()),
                getMoveWithTookPiece(knight, 3, 4, 5, 5, board.getPiece(5, 5).get()),
                getMoveWithTookPiece(knight, 3, 4, 4, 2, board.getPiece(4, 2).get()),
                getMoveWithTookPiece(knight, 3, 4, 2, 6, board.getPiece(2, 6).get()),
                getMoveWithTookPiece(knight, 3, 4, 5, 3, board.getPiece(5, 3).get())
        );
    }

    @Test
    public void testComputeDiagonalMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "                \n" +
                "      ♗         \n" +
                "                \n" +
                "                \n" +
                "                \n" +
                "                \n"
        );
        Piece bishop = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeDiagonalMoves(bishop, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up-right
                new Move(bishop, 3, 4, 4, 5),
                new Move(bishop, 3, 4, 5, 6),
                new Move(bishop, 3, 4, 6, 7),
                // up-left
                new Move(bishop, 3, 4, 2, 5),
                new Move(bishop, 3, 4, 1, 6),
                new Move(bishop, 3, 4, 0, 7),
                // down-right
                new Move(bishop, 3, 4, 4, 3),
                new Move(bishop, 3, 4, 5, 2),
                new Move(bishop, 3, 4, 6, 1),
                new Move(bishop, 3, 4, 7, 0),
                // down-left
                new Move(bishop, 3, 4, 2, 3),
                new Move(bishop, 3, 4, 1, 2),
                new Move(bishop, 3, 4, 0, 1)
        );
    }

    @Test
    public void testComputeDiagonalMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "  ♙             \n" +
                "        ♟       \n" +
                "      ♗         \n" +
                "    ♙           \n" +
                "          ♟     \n" +
                "                \n" +
                "                \n"
        );
        Piece bishop = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeDiagonalMoves(bishop, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up-right
                getMoveWithTookPiece(bishop, 3, 4, 4, 5, board.getPiece(4, 5).get()),
                // up-left
                new Move(bishop, 3, 4, 2, 5),
                // down-right
                new Move(bishop, 3, 4, 4, 3),
                getMoveWithTookPiece(bishop, 3, 4, 5, 2, board.getPiece(5, 2).get())
                // down-left
        );
    }

    @Test
    public void testComputeStraightMovesFreeSpace() {
        Board board = new Board("" +
                "                \n" +
                "                \n" +
                "                \n" +
                "      ♖         \n" +
                "                \n" +
                "                \n" +
                "                \n" +
                "                \n"
        );
        Piece rook = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeStraightMoves(rook, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up
                new Move(rook, 3, 4, 3, 5),
                new Move(rook, 3, 4, 3, 6),
                new Move(rook, 3, 4, 3, 7),
                //down
                new Move(rook, 3, 4, 3, 3),
                new Move(rook, 3, 4, 3, 2),
                new Move(rook, 3, 4, 3, 1),
                new Move(rook, 3, 4, 3, 0),
                // left
                new Move(rook, 3, 4, 2, 4),
                new Move(rook, 3, 4, 1, 4),
                new Move(rook, 3, 4, 0, 4),
                // right
                new Move(rook, 3, 4, 4, 4),
                new Move(rook, 3, 4, 5, 4),
                new Move(rook, 3, 4, 6, 4),
                new Move(rook, 3, 4, 7, 4)
        );
    }

    @Test
    public void testComputeStraightMovesWithPieces() {
        Board board = new Board("" +
                "                \n" +
                "      ♙         \n" +
                "                \n" +
                "    ♟ ♖ ♙       \n" +
                "                \n" +
                "                \n" +
                "      ♟         \n" +
                "                \n"
        );
        Piece rook = board.getPiece(3, 4).get();
        List<Move> moves = moveService.computeStraightMoves(rook, 3, 4, board);
        assertThat(moves).containsExactlyInAnyOrder(
                // up
                new Move(rook, 3, 4, 3, 5),
                //down
                new Move(rook, 3, 4, 3, 3),
                new Move(rook, 3, 4, 3, 2),
                getMoveWithTookPiece(rook, 3, 4, 3, 1, board.getPiece(3, 1).get()),
                // left
                getMoveWithTookPiece(rook, 3, 4, 2, 4, board.getPiece(2, 4).get())
                // right
        );
    }

    @Test
    public void testComputeCastlingMovesBothSides() {
        Board board = new Board("" +
                "♜       ♚     ♜ \n" +
                "♟ ♟ ♟ ♛   ♟ ♟ ♟ \n" +
                "    ♞ ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.computeCastlingMoves(blackKing, 4, 7, board, Collections.emptyList())).hasSize(2);
        Piece whiteKing = board.getPiece(3, 0).get();
        assertThat(moveService.computeCastlingMoves(whiteKing, 4, 0, board, Collections.emptyList())).hasSize(0);
    }

    @Test
    public void testComputeCastlingMovesDeniedByHistory() {
        Board board = new Board("" +
                "♜       ♚     ♜ \n" +
                "♟ ♟ ♟ ♛   ♟ ♟ ♟ \n" +
                "    ♞ ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece whiteKing = board.getPiece(4, 0).get();
        List<Move> history = Collections.singletonList(new Move(whiteKing, 4, 0, 3, 0));
        assertThat(moveService.computeCastlingMoves(whiteKing, 4, 0, board, history)).hasSize(0);
    }

    @Test
    public void testGetCastlingMoveCorrect() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece whiteKing = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, whiteKing, 4, 0, 2, 0, 3, Collections.emptyList())).isPresent().get().isInstanceOf(
            CastlingMove.class);
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.getCastlingMove(board, blackKing, 4, 7, 6, 7, 5, Collections.emptyList())).isPresent().get().isInstanceOf(CastlingMove.class);
    }

    @Test
    public void testGetCastlingMoveRookNotPresent() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "        ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3, Collections.emptyList())).isEmpty();

        board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♗       ♔   ♘ ♖ \n"
        );
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3, Collections.emptyList())).isEmpty();

        board = new Board("" +
                "  ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♜       ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3, Collections.emptyList())).isEmpty();
    }

    @Test
    public void testGetCastlingMovePieceBetweenRookAndKing() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♗ ♔   ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3, Collections.emptyList())).isEmpty();
    }

    @Test
    public void testGetCastlingMoveKingCrossFire() {
        Board board = new Board("" +
                "♜ ♞     ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "    ♛   ♙       \n" +
                "          ♙     \n" +
                "♙ ♙   ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.getCastlingMove(board, king, 4, 0, 2, 0, 3, Collections.emptyList())).isEmpty();
    }

    @Test
    public void testIsValidKingPositionForCastlingKingHasMoved() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 3, 0, board)).isFalse();

        board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙ ♔   ♙ ♙ \n" +
                "♖         ♗ ♘ ♖ \n"
        );
        king = board.getPiece(4, 1).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 1, board)).isFalse();

        board = new Board("" +
                "♜ ♞ ♝ ♛       ♜ \n" +
                "♟ ♟ ♟   ♚ ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙ ♔   ♙ ♙ \n" +
                "♖         ♗ ♘ ♖ \n"
        );
        king = board.getPiece(4, 6).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 6, board)).isFalse();
    }


    @Test
    public void testIsValidKingPositionForCastlingWrongPiece() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔ ♕ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingEmptyLocation() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖     ♔   ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(3, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingInCheck() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙     ♝ \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece king = board.getPiece(4, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(king, 4, 0, board)).isFalse();
    }

    @Test
    public void testIsValidKingPositionForCastlingCorrect() {
        Board board = new Board("" +
                "♜ ♞   ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙       \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖       ♔ ♗ ♘ ♖ \n"
        );
        Piece whiteKing = board.getPiece(4, 0).get();
        assertThat(moveService.isValidKingPositionForCastling(whiteKing, 4, 0, board)).isTrue();
        Piece blackKing = board.getPiece(4, 7).get();
        assertThat(moveService.isValidKingPositionForCastling(blackKing, 4, 7, board)).isTrue();
    }

    @Test
    public void testGetAllowedMoveOutOfBounds() {
        assertThat(moveService.getAllowedMove(null, 0, 7, 0, 1, null)).isEmpty();
    }

    @Test
    public void testGetAllowedBishopCapturingQueen() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Bishop(Color.BLACK), 2, 7, 4, -4, board)).isPresent()
                .get().hasFieldOrPropertyWithValue("tookPiece", board.getPiece(6, 3).get());
    }

    @Test
    public void testGetAllowedMovePawnMovingWithoutCapture() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟ ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 0, -1, board)).isPresent();
    }

    @Test
    public void testGetAllowedMovePawnMovingWithoutCaptureBlockedByOwnPawn() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "      ♟ ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 0, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedMovePawnMovingAndCapturing() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟ ♟       \n" +
                "        ♙   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isPresent()
                .get().hasFieldOrPropertyWithValue("tookPiece", board.getPiece(4, 3).get());
    }

    @Test
    public void testGetAllowedMovePawnMovingCapturingBlockedByOwnPawn() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "        ♟   ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedMovePawnMovingCapturingNothingToCapture() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟     ♟ ♟ ♟ \n" +
                "                \n" +
                "      ♟         \n" +
                "            ♕   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Pawn(Color.BLACK), 3, 4, 1, -1, board)).isEmpty();
    }

    @Test
    public void testGetAllowedBishopCannotCaptureOwnPiece() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝ ♞ ♜ \n" +
                "♟ ♟ ♟       ♟ ♟ \n" +
                "      ♟         \n" +
                "        ♟       \n" +
                "        ♙   ♟   \n" +
                "          ♙     \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ \n"
        );
        assertThat(moveService.getAllowedMove(new Bishop(Color.BLACK), 2, 7, 4, -4, board)).isEmpty();
    }

    @Test
    public void testIsOutOfBounds() {
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, -1, 0))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, -1))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 8, 0))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, 8))).isTrue();
        assertThat(moveService.isOutOfBounds(new Move(null, 0, 0, 0, 7))).isFalse();
    }

    @Test
    public void testIsValidSituationMissingKing() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕   ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isFalse();
    }

    @Test
    public void testIsValidSituationKingsDistanceTooSmall() {
        Board board = new Board("" +
                "♜ ♞     ♔ ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕   ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isFalse();
    }

    @Test
    public void testIsValidSituationCannotBeInCheckAfterMyMove() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♖               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "    ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.WHITE)).isFalse();
    }

    @Test
    public void testIsValidSituationCorrect() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♖               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "    ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isValidSituation(board, Color.BLACK)).isTrue();
    }

    @Test
    public void testIsBlackKingInPawnCheck() {
        Board board = new Board("" +
                "♜ ♞     ♚     ♜ \n" +
                "♟ ♟ ♟ ♝   ♙   ♟ \n" +
                "                \n" +
                "        ♘       \n" +
                "  ♝     ♙ ♛     \n" +
                "                \n" +
                "♙ ♙ ♙ ♙     ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInPawnCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInPawnCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsWhiteKingInPawnCheck() {
        Board board = new Board("" +
                "♜ ♞       ♚   ♜ \n" +
                "♟ ♟   ♝   ♙   ♟ \n" +
                "    ♙           \n" +
                "        ♘       \n" +
                "        ♙ ♛     \n" +
                "♝               \n" +
                "  ♙   ♟     ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInPawnCheck(board, new Position(5, 7), Color.BLACK)).isFalse();
        assertThat(moveService.isInPawnCheck(board, new Position(4, 0), Color.WHITE)).isTrue();
    }

    @Test
    public void testIsInLCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♜ \n" +
                "♟ ♟ ♟     ♟   ♟ \n" +
                "          ♘ ♟   \n" +
                "        ♘       \n" +
                "  ♝     ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInLCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInLCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInLCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚     ♜ \n" +
                "♟ ♟ ♟     ♟   ♟ \n" +
                "          ♛ ♟   \n" +
                "        ♘       \n" +
                "  ♝     ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖   ♗ ♕ ♔ ♗   ♖ \n"
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testIsInDiagonalCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟   ♛ ♟   ♟ \n" +
                "      ♟         \n" +
                "  ♗     ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "♙   ♙           \n" +
                "  ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔   ♘ ♖ "
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 0), Color.WHITE)).isFalse();

        board = new Board("" + "" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟       ♟   ♟ \n" +
                "    ♟           \n" +
                "  ♗   ♟   ♕ ♟   \n" +
                "  ♛ ♙ ♙ ♙   ♞   \n" +
                "♙               \n" +
                "  ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔   ♘ ♖ \n"
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 0), Color.WHITE)).isTrue();

    }

    @Test
    public void testIsInDiagonalCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟ ♟ ♛ ♟   ♟ \n" +
                "                \n" +
                "        ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInDiagonalCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testIsInStraightCheckVertically() {
        // Vertically
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚ ♝   ♜ \n" +
                "♟ ♟ ♟ ♟   ♟ ♟ ♟ \n" +
                "          ♞     \n" +
                "        ♕       \n" +
                "        ♙       \n" +
                "                \n" +
                "♙ ♙ ♙ ♙   ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInStraightCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInStraightCheckHorizontally() {
        Board board = new Board("" +
                "♜ ♞ ♝ ♛ ♚     ♕ \n" +
                "♟ ♟ ♟ ♟ ♝ ♟   ♟ \n" +
                "                \n" +
                "            ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isTrue();
        assertThat(moveService.isInStraightCheck(board, new Position(4, 0), Color.WHITE)).isFalse();
    }

    @Test
    public void testIsInStraightCheckNoCheck() {
        Board board = new Board("" +
                "♜ ♞ ♝   ♚ ♝     \n" +
                "♟ ♟ ♟ ♟ ♛ ♟   ♟ \n" +
                "                \n" +
                "        ♕   ♟   \n" +
                "      ♙ ♙   ♞   \n" +
                "    ♙           \n" +
                "♙ ♙       ♙ ♙ ♙ \n" +
                "♖ ♘ ♗   ♔ ♗ ♘ ♖ "
        );
        assertThat(moveService.isInStraightCheck(board, new Position(4, 7), Color.BLACK)).isFalse();
    }

    @Test
    public void testCenteredHeatmap() {
        int[][] heatmap = MoveService.generateCenteredHeatmap();
        assertThat(heatmap).hasNumberOfRows(8);
        assertThat(heatmap[0]).hasSize(8);
        int[][] expected = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 0, 0},
                {0, 0, 1, 2, 2, 1, 0, 0},
                {0, 0, 1, 2, 2, 1, 0, 0},
                {0, 0, 1, 1, 1, 1, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
        };
        assertThat(heatmap).isEqualTo(expected);
    }

    @Test
    public void testGetHeatmapAroundLocation() {
        int[][] heatmap = moveService.getHeatmapAroundLocation(7, 0);
        assertThat(heatmap).hasNumberOfRows(8);
        assertThat(heatmap[0]).hasSize(8);
        int[][] expected = new int[][] {
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 0, 0, 0, 0, 0},
                {2, 2, 1, 0, 0, 0, 0, 0},
                {3, 2, 1, 0, 0, 0, 0, 0},
        };
        assertThat(heatmap).isEqualTo(expected);
    }

    @Test
    public void testIsDrawAcceptableWithInitialPositionsDeclinedForPenalty() {
        // given
        Game game = new Game(new Human("test"), new RandomBot(moveService));

        // when
        boolean isAcceptable = moveService.isDrawAcceptable(game);

        // then
        assertThat(isAcceptable).isFalse();
    }

    private Move getMoveWithTookPiece(Piece piece, int fromX, int fromY, int toX, int toY, Piece tookPiece) {
        Move move = new Move(piece, fromX, fromY, toX, toY);
        move.setTookPiece(tookPiece);
        return move;
    }
}
