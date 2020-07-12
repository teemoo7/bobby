package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.services.MoveService;

public abstract class Bot extends Player {
    protected final MoveService moveService;

    public Bot(MoveService moveService) {
        super("Bobby");
        this.moveService = moveService;
    }

    public abstract Move selectMove(Game game);

    public abstract boolean isDrawAcceptable(Game game);
}
