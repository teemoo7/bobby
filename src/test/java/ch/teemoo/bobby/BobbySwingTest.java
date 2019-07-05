package ch.teemoo.bobby;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import javax.swing.*;
import java.awt.*;

import static org.assertj.swing.launcher.ApplicationLauncher.application;
import static org.assertj.swing.finder.WindowFinder.findFrame;

public class BobbySwingTest extends AssertJSwingJUnitTestCase {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private FrameFixture frame;

    @Override
    protected void onSetUp() {
        application(Bobby.class).start();
        frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return "Bobby chess game".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
    }

    @Test
    public void testFileExitMenu() {
        exit.expectSystemExitWithStatus(0);
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "Exit".equals(menuItem.getText());
            }
        }).click();
    }
}
