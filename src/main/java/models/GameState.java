package models;

public enum GameState {
    IN_PROGRESS, LOSS, DRAW_STALEMATE, DRAW_THREEFOLD, DRAW_50_MOVES;

    public boolean isDraw() {
        return this == DRAW_50_MOVES || this == DRAW_THREEFOLD || this == DRAW_STALEMATE;
    }

    public boolean isLost() {
        return this == LOSS;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }
}
