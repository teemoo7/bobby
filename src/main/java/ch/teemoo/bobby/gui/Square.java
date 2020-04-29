package ch.teemoo.bobby.gui;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JLabel;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Square extends JLabel {
    private final static Logger logger = LoggerFactory.getLogger(Square.class);

    private Piece piece;
    private final Position position;

    Square(Piece piece, Position position, Background background) {
        super(getPieceText(piece));
        this.piece = piece;
        this.position = position;
        setFont();
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
            return piece.getUnicode();
        }
        return "";
    }

    private void setFont() {
        try {
            var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("fonts/FreeSerif.ttf");
            if (inputStream == null) {
                throw new IOException("Cannot load font from resource");
            }
            var font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            setFont(font.deriveFont(Font.PLAIN, 72));
        } catch (IOException | FontFormatException e) {
            logger.warn("Unable to use embedded font, using fallback", e);
            setFont(new Font("Serif", Font.PLAIN, 48));
        }

    }
}
