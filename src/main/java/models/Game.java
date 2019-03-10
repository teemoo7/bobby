package models;

import java.util.ArrayList;
import java.util.List;

public class Game {
    final Player whitePlayer;
    final Player blackPlayer;

    Color toPlay;
    GameState state;
    List<Move> history;

    public Game(Player whitePlayer, Player blackPlayer) {
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.toPlay = Color.WHITE;
        this.history = new ArrayList<>();
        this.state = GameState.IN_PROGRESS;
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
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

    public List<Move> getHistory() {
        return history;
    }

    public void addMoveToHistory(Move move) {
        this.history.add(move);
    }
}
