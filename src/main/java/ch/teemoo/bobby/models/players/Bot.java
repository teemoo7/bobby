package ch.teemoo.bobby.models.players;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.services.MoveService;

public abstract class Bot extends Player {
    public Bot(String name) {
        super(name);
    }

    public abstract Move selectMove(Game game, MoveService moveService);
}
