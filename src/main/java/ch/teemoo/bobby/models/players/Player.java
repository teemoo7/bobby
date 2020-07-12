package ch.teemoo.bobby.models.players;

public abstract class Player {
    private final String name;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return getClass().getSimpleName() + " " + getName();
    }

    public boolean isBot() {
        return this instanceof Bot;
    }
}
