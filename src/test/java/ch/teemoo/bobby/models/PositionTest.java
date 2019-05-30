package ch.teemoo.bobby.models;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class PositionTest {
    @Test
    public void testPosition() {
        Position position = new Position(4, 5);
        assertThat(position.getX()).isEqualTo(4);
        assertThat(position.getY()).isEqualTo(5);
    }
}
