package ch.teemoo.bobby.models.players;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class HumanTest {

    @Test
    public void testHumanProps() {
        final String name = "Micael";
        Player human = new Human(name);
        assertThat(human.getName()).isEqualTo(name);
        assertThat(human.getDescription()).isEqualTo("Human Micael");
        assertThat(human.isBot()).isFalse();
    }
}
