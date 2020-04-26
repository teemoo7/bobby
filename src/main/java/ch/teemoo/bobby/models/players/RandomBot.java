package ch.teemoo.bobby.models.players;

import java.util.List;
import java.util.Random;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.services.MoveService;

public class RandomBot extends Bot {
    private final static Random RANDOM = new Random();

    public RandomBot(MoveService moveService) {
        super(moveService);
    }

    public Move selectMove(Game game) {
        List<Move> moves = moveService.computeAllMoves(game.getBoard(), game.getToPlay(), game.getHistory(), true);
        return moves.get(RANDOM.nextInt(moves.size()));
    }

    public boolean isDrawAcceptable(Game game) {
        return RANDOM.nextBoolean();
    }
}
