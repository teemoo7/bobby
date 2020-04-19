package ch.teemoo.bobby.gui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import javax.swing.JLabel;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;

public class Square extends JLabel {

    private Piece piece;
    private final Position position;

    Square(Piece piece, Position position, Background background) {
        super(getPieceText(piece));
        this.piece = piece;
        this.position = position;
        setFont(new Font("Sans Serif", Font.PLAIN, 48));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setBackground(background.getColor());
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
        this.setText(getPieceText(piece));
    }

    public Position getPosition() {
        return position;
    }

    private static String getPieceText(Piece piece) {
        if (piece != null) {
            if (piece instanceof Pawn && piece.getColor() == Color.BLACK && System.getProperty("os.name").toLowerCase()
                .contains("mac") && Arrays
                .asList(GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames())
                .contains("Apple Color Emoji")) {

                // on MacOSX this unicode is interpreted as an emoji (Apple Color Emoji), resulting into a bug
                // to display it. A possible workaround could be to move this Font to user's fonts so that it can be
                // disabled oin the Font Book but it requires to disable SIP too.

                return "P";
            }
            return piece.getUnicode();
        }
        return "";
    }
}
