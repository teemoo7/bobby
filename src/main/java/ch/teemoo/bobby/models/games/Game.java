package ch.teemoo.bobby.models.games;

import static ch.teemoo.bobby.models.Board.SIZE;

import java.util.ArrayList;
import java.util.List;

import ch.teemoo.bobby.models.Board;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.King;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.models.players.Player;

public class Game {
    final Player whitePlayer;
    final Player blackPlayer;
    final Board board;
    final List<Move> history;
    Color toPlay;
    GameState state;
    String opening;

    public Game(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.toPlay = Color.WHITE;
        this.history = new ArrayList<>();
        this.state = GameState.IN_PROGRESS;
        this.board = new Board(getInitialPiecesPositions());
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public Board getBoard() {
        return board;
    }

    public List<Move> getHistory() {
        return history;
    }

    public Color getToPlay() {
        return toPlay;
    }

    public void setToPlay(Color toPlay) {
        this.toPlay = toPlay;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public void addMoveToHistory(Move move) {
        this.history.add(move);
    }

    public Player getPlayerByColor(Color color) {
        switch (color) {
        case WHITE:
            return whitePlayer;
        case BLACK:
            return blackPlayer;
        }
        throw new RuntimeException("Unexpected color");
    }

    public Player getPlayerToPlay() {
        return getPlayerByColor(toPlay);
    }

    public Player getPlayerWaiting() {
        switch (toPlay) {
        case WHITE:
            return blackPlayer;
        case BLACK:
            return whitePlayer;
        }
        throw new RuntimeException("Unexpected color");
    }

    public void removeLastMoveFromHistory() {
        this.history.remove(this.history.size() - 1);
    }

    public boolean canBePlayed() {
        return whitePlayer != null && blackPlayer != null && state == GameState.IN_PROGRESS;
    }

    private Piece[][] getInitialPiecesPositions() {
        Piece[][] pos = new Piece[SIZE][SIZE];
        pos[0][0] = new Rook(Color.WHITE);
        pos[0][1] = new Knight(Color.WHITE);
        pos[0][2] = new Bishop(Color.WHITE);
        pos[0][3] = new Queen(Color.WHITE);
        pos[0][4] = new King(Color.WHITE);
        pos[0][5] = new Bishop(Color.WHITE);
        pos[0][6] = new Knight(Color.WHITE);
        pos[0][7] = new Rook(Color.WHITE);
        pos[1][0] = new Pawn(Color.WHITE);
        pos[1][1] = new Pawn(Color.WHITE);
        pos[1][2] = new Pawn(Color.WHITE);
        pos[1][3] = new Pawn(Color.WHITE);
        pos[1][4] = new Pawn(Color.WHITE);
        pos[1][5] = new Pawn(Color.WHITE);
        pos[1][6] = new Pawn(Color.WHITE);
        pos[1][7] = new Pawn(Color.WHITE);

        pos[7][0] = new Rook(Color.BLACK);
        pos[7][1] = new Knight(Color.BLACK);
        pos[7][2] = new Bishop(Color.BLACK);
        pos[7][3] = new Queen(Color.BLACK);
        pos[7][4] = new King(Color.BLACK);
        pos[7][5] = new Bishop(Color.BLACK);
        pos[7][6] = new Knight(Color.BLACK);
        pos[7][7] = new Rook(Color.BLACK);
        pos[6][0] = new Pawn(Color.BLACK);
        pos[6][1] = new Pawn(Color.BLACK);
        pos[6][2] = new Pawn(Color.BLACK);
        pos[6][3] = new Pawn(Color.BLACK);
        pos[6][4] = new Pawn(Color.BLACK);
        pos[6][5] = new Pawn(Color.BLACK);
        pos[6][6] = new Pawn(Color.BLACK);
        pos[6][7] = new Pawn(Color.BLACK);

        return pos;
    }
}
