package models;

import java.util.List;
import java.util.Random;

public class RandomBot extends Bot {
    public RandomBot() {
        super("Random Bot");
    }

    public Move selectMove(List<Move> moves) {
        return moves.get(new Random().nextInt(moves.size()));
    }
}
