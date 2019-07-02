package ch.teemoo.bobby.gui;

import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SideLabelTest {

    @Test
    public void testSideLabel() {
        SideLabel sideLabel = new SideLabel("A");
        assertThat(sideLabel.getText()).isEqualTo("A");
        assertThat(sideLabel.getFont()).isEqualTo(new Font("DejaVu Sans", Font.PLAIN, 16));
        assertThat(sideLabel.getHorizontalAlignment()).isEqualTo(SwingConstants.CENTER);
        assertThat(sideLabel.isOpaque()).isTrue();
    }
}
