package ch.teemoo.bobby.models;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class PositionTest {
    @Test
    public void testPosition() {
        Position position = new Position(4, 5);
        assertThat(position.getX()).isEqualTo(4);
        assertThat(position.getY()).isEqualTo(5);
    }
}
