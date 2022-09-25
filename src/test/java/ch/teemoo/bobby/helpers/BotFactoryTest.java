package ch.teemoo.bobby.helpers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import ch.teemoo.bobby.models.players.Bot;
import ch.teemoo.bobby.models.players.ExperiencedBot;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.models.players.TraditionalBot;
import ch.teemoo.bobby.services.MoveService;
import ch.teemoo.bobby.services.OpeningService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

public class BotFactoryTest {

	private BotFactory botFactory;

	@Mock
	MoveService moveService;

	@Mock
	OpeningService openingService;

	@BeforeEach
	public void setUp() {
		this.botFactory = new BotFactory(moveService, openingService);
	}

	@Test
	public void testGetRandomBot() {
		Bot bot = botFactory.getRandomBot();
		assertThat(bot).isInstanceOf(RandomBot.class);
	}

	@Test
	public void testGetTraditionalBot() {
		Bot bot = botFactory.getTraditionalBot(2, null);
		assertThat(bot).isInstanceOf(TraditionalBot.class);
	}

	@Test
	public void testGetTraditionalBotWrongLevel() {
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getTraditionalBot(-1, null));
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getTraditionalBot(3, null));
	}

	@Test
	public void testGetTraditionalBotWrongTimeout() {
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getTraditionalBot(1, 60));
	}

	@Test
	public void testGetExperiencedBot() {
		Bot bot = botFactory.getExperiencedBot(2, null);
		assertThat(bot).isInstanceOf(ExperiencedBot.class);
	}

	@Test
	public void testGetExperiencedBotWrongLevel() {
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getExperiencedBot(-1, null));
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getExperiencedBot(3, null));
	}

	@Test
	public void testGetExperiencedBotWrongTimeout() {
		assertThatExceptionOfType(AssertionError.class).isThrownBy(() -> botFactory.getExperiencedBot(1, 60));
	}

	@Test
	public void testGetStrongestBot() {
		Bot bot = botFactory.getStrongestBot();
		assertThat(bot).isInstanceOf(ExperiencedBot.class);
	}

}
