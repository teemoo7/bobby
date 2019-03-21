package models.players;

import java.util.List;
import java.util.Random;

import models.Game;
import models.Move;
import services.MoveService;

public class RandomBot extends Bot {
    public RandomBot() {
        super("Random Bot");
    }

    public Move selectMove(Game game, MoveService moveService) {
        List<Move> moves = moveService.computeAllMoves(game.getBoard(), game.getToPlay());
        return moves.get(new Random().nextInt(moves.size()));
    }
}
