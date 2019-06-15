package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.services.MoveService;

public class TraditionalBot extends Bot {
    private final int level;

    public TraditionalBot(int level) {
        super("Traditional Bot (level " + level + ")");
        assert level >= 0;
        assert level <= 2;
        this.level = level;
    }

    public Move selectMove(Game game, MoveService moveService) {
        return moveService.selectMove(game, level);
    }
}
