package ch.teemoo.bobby.gui;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Dialog;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

import javax.swing.SwingUtilities;

import ch.teemoo.bobby.helpers.GuiHelper;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import org.assertj.swing.core.GenericTypeMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;

public class BoardViewSwingTest extends AssertJSwingJUnitTestCase{
    private BoardView frame;
    private FrameFixture window;

    @Override
    protected void onSetUp() {
            frame = GuiActionRunner.execute(() -> new BoardView("test", new GuiHelper(), true));
            window = new FrameFixture(robot(), frame);
            window.show(); // shows the frame to test
    }

    @Override
    protected void onTearDown() {
        window.cleanUp();
    }

    @Test
    public void testPromotionDialog() throws InterruptedException, ExecutionException {
        // given
        Color color = Color.BLACK;

        // when
        RunnableFuture<Piece> rf = new FutureTask<>(() -> frame.promotionDialog(color));
        SwingUtilities.invokeLater(rf);

        // then
        window.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "Promotion".equals(dialog.getTitle());
            }
        }).close();
        Piece piece = rf.get();
        assertThat(piece).isInstanceOf(Queen.class);
        assertThat(piece.getColor()).isEqualTo(Color.BLACK);
    }

    @Test
    public void testPopupInfo() {
        SwingUtilities.invokeLater(() -> frame.popupInfo("An info here"));
        window.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "Info".equals(dialog.getTitle());
            }
        }).close();
    }

    @Test
    public void testPopupError() {
        SwingUtilities.invokeLater(() -> frame.popupError("An info here"));
        window.dialog(new GenericTypeMatcher<Dialog>(Dialog.class) {
            @Override
            protected boolean isMatching(Dialog dialog) {
                return "Error".equals(dialog.getTitle());
            }
        }).close();
    }
}
