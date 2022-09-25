package ch.teemoo.bobby.gui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class BackgroundTest {

    @Test
    public void testBackgroundColor() {
        assertThat(Background.LIGHT.getColor()).isEqualTo(new java.awt.Color(222, 184, 135));
        assertThat(Background.DARK.getColor()).isEqualTo(new java.awt.Color(139, 69, 19));
    }
}
