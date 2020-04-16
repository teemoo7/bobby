package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.services.MoveService;

public class TraditionalBot extends Bot {
    private final int level;

    public TraditionalBot(int level, MoveService moveService) {
        super("Traditional Bot (level " + level + ")", moveService);
        this.level = level;
    }

    public Move selectMove(Game game) {
        return moveService.selectMove(game, level);
    }
}
