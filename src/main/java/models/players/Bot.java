package models.players;

import models.Game;
import models.Move;
import services.MoveService;

public abstract class Bot extends Player {
    public Bot(String name) {
        super(name);
    }

    public abstract Move selectMove(Game game, MoveService moveService);
}
