package ch.teemoo.bobby.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import ch.teemoo.bobby.models.Color;
import org.junit.jupiter.api.Test;

public class ColorHelperTest {

    @Test
    public void testSwap() {
        Color color = Color.BLACK;
        Color swapColor = ColorHelper.swap(color);
        assertThat(swapColor).isEqualTo(Color.WHITE);
        assertThat(ColorHelper.swap(swapColor)).isEqualTo(color);
    }

}
