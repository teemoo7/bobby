package models;

import java.util.List;

public abstract class Bot extends Player {
    public Bot(String name) {
        super(name);
    }

    public abstract Move selectMove(List<Move> moves, Board board);
}
