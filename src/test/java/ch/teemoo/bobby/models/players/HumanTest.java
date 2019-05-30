package ch.teemoo.bobby.models.players;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HumanTest {

    @Test
    public void testHumanProps() {
        final String name = "Bobby";
        Player human = new Human(name);
        assertThat(human.getName()).isEqualTo(name);
        assertThat(human.isBot()).isFalse();
    }
}
