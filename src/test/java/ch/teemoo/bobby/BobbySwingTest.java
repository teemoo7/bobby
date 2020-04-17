package ch.teemoo.bobby;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.swing.finder.WindowFinder.findFrame;
import static org.assertj.swing.launcher.ApplicationLauncher.application;

import java.awt.Dialog;
import java.awt.Frame;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.fixture.DialogFixture;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JLabelFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

public class BobbySwingTest extends AssertJSwingJUnitTestCase {
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private FrameFixture frame;

    @Override
    protected void onSetUp() {
        application(Bobby.class).withArgs("default").start();
        frame = findFrame(new GenericTypeMatcher<Frame>(Frame.class) {
            protected boolean isMatching(Frame frame) {
                return "Bobby chess game".equals(frame.getTitle()) && frame.isShowing();
            }
        }).using(robot());
    }

    @Override
    protected void onTearDown() {
        frame.cleanUp();
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

    @Test
    public void testHelpAboutMenu() {
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "About".equals(menuItem.getText());
            }
        }).click();

        frame.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "About Bobby".equals(dialog.getTitle());
            }
        }).close();
    }

    @Test
    public void testNewGameMenuWithSetupClose() {
        exit.expectSystemExitWithStatus(0);

        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "New".equals(menuItem.getText());
            }
        }).click();

        frame.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "Game setup".equals(dialog.getTitle());
            }
        }).close();
    }

    @Test
    public void testNewGameMenuOk() {
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "New".equals(menuItem.getText());
            }
        }).click();

        DialogFixture dialogFixture = frame.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "Game setup".equals(dialog.getTitle());
            }
        });

        dialogFixture.button(new GenericTypeMatcher<JButton>(JButton.class) {
            @Override
            protected boolean isMatching(JButton button) {
                return button.isDefaultButton();
            }
        }).click();
    }

    @Test
    public void testLoadGameMenu() {
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "Load".equals(menuItem.getText());
            }
        }).click();

        frame.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return true;
            }
        }).close();
    }

    @Test
    public void testSaveGameMenu() {
        frame.menuItem(new GenericTypeMatcher<JMenuItem>(JMenuItem.class) {
            @Override
            protected boolean isMatching(JMenuItem menuItem) {
                return "Save".equals(menuItem.getText());
            }
        }).click();

        frame.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return true;
            }
        }).close();
    }

    @Test
    public void testSelectWhiteKingPiece() throws Exception {
        JLabelFixture whiteKing = frame.label(new GenericTypeMatcher<JLabel>(JLabel.class) {
            @Override
            protected boolean isMatching(JLabel label) {
                return "♔".equals(label.getText());
            }
        });
        assertThat(whiteKing.target().getBorder()).isNull();
        whiteKing.click();
        Thread.sleep(1000);
        assertThat(whiteKing.target().getBorder()).isNotNull();
    }

}
