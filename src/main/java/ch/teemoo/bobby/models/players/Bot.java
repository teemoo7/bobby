package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.services.MoveService;

public abstract class Bot extends Player {
    protected final MoveService moveService;

    public Bot(String name, MoveService moveService) {
        super("Bobby (" + name + ")");
        this.moveService = moveService;
    }

    public abstract Move selectMove(Game game);
}
