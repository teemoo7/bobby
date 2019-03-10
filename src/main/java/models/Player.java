package models;

public abstract class Player {
    String name;
    boolean isBot;

    public Player(String name, boolean isBot) {
        this.name = name;
        this.isBot = isBot;
    }
}
