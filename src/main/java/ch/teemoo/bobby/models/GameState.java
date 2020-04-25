package ch.teemoo.bobby.models;

public enum GameState {
    IN_PROGRESS, LOSS, DRAW_STALEMATE, DRAW_THREEFOLD, DRAW_50_MOVES, DRAW_AGREEMENT;

    public boolean isDraw() {
        return this == DRAW_50_MOVES || this == DRAW_THREEFOLD || this == DRAW_STALEMATE || this == DRAW_AGREEMENT;
    }

    public boolean isLost() {
        return this == LOSS;
    }

    public boolean isInProgress() {
        return this == IN_PROGRESS;
    }
}
