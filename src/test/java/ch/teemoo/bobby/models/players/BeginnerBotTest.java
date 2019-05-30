package ch.teemoo.bobby.models.players;

import org.junit.Ignore;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeginnerBotTest {
    @Test
    public void testBeginnerBotProps() {
        Player bot = new BeginnerBot();
        assertThat(bot.getName()).isEqualTo("Beginner Bot");
        assertThat(bot.isBot()).isTrue();
    }

    @Test
    @Ignore
    public void testSelectMove() {
        //todo: implement test when logic is finalized (and remove the @Ignore above)
    }

}
