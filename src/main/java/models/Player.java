package models;

public abstract class Player {
    private final String name;
    private final boolean isBot;

    public Player(String name, boolean isBot) {
        this.name = name;
        this.isBot = isBot;
    }

    public String getName() {
        return name;
    }

    public boolean isBot() {
        return isBot;
    }
}
