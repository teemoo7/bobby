package ch.teemoo.bobby.models.players;

import java.time.LocalDateTime;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.services.MoveService;

public class TraditionalBot extends Bot {
    protected final int level;
    protected final Integer timeout;

    public TraditionalBot(int level, Integer timeout, MoveService moveService) {
        super(moveService);
        this.level = level;
        this.timeout = timeout;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " (level " + level + ")";
    }

    public Move selectMove(Game game) {
        LocalDateTime computationTimeout = null;
        if (timeout != null) {
            computationTimeout = LocalDateTime.now().plusSeconds(timeout);
        }
        return moveService.selectMove(game, level, computationTimeout);
    }

    public boolean isDrawAcceptable(Game game) {
        return moveService.isDrawAcceptable(game);
    }
}
