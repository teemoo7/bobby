package ch.teemoo.bobby.models.players;

import java.util.List;
import java.util.Random;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;

public class ExperiencedBot extends Bot {
    private final int level;
    private final OpeningService openingService;

    public ExperiencedBot(int level, MoveService moveService, OpeningService openingService) {
        super("Experienced Bot (level " + level + ")", moveService);
        this.level = level;
        this.openingService = openingService;
    }

    public Move selectMove(Game game) {
        List<Move> openingMoves = openingService.findPossibleMovesForHistory(game.getHistory());
        if (openingMoves.isEmpty()) {
            return moveService.selectMove(game, level);
        } else {
            return openingMoves.get(new Random().nextInt(openingMoves.size()));
        }
    }
}
