package ch.teemoo.bobby.helpers;

import ch.teemoo.bobby.models.players.Bot;
import ch.teemoo.bobby.models.players.ExperiencedBot;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;

public class BotFactory {
	private static final int MAX_LEVEL = 2;
	private static final int MIN_LEVEL = 0;

	private final MoveService moveService;
	private final OpeningService openingService;

	public BotFactory(MoveService moveService, OpeningService openingService) {
		this.moveService = moveService;
		this.openingService = openingService;
	}

	public Bot getRandomBot() {
		return new RandomBot(moveService);
	}

	public Bot getTraditionalBot(int level) {
		checkLevel(level);
		return new TraditionalBot(level, moveService);
	}

	public Bot getExperiencedBot(int level) {
		checkLevel(level);
		return new ExperiencedBot(level, moveService, openingService);
	}

	public Bot getStrongestBot() {
		return getExperiencedBot(MAX_LEVEL);
	}

	private void checkLevel(int level) {
		assert level >= MIN_LEVEL;
		assert level <= MAX_LEVEL;
	}
}
