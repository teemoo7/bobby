package ch.teemoo.bobby.gui;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.pieces.*;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BackgroundTest {

    @Test
    public void testBackgroundColor() {
        assertThat(Background.LIGHT.getColor()).isEqualTo(new java.awt.Color(222, 184, 135));
        assertThat(Background.DARK.getColor()).isEqualTo(new java.awt.Color(139, 69, 19));
    }
}
