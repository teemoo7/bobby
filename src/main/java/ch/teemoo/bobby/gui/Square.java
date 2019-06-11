package ch.teemoo.bobby.gui;

import java.awt.Font;

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
                .contains("mac")) {
                //fixme: for some unclear reason, the black pawn char is not correctly rendered on Mac
                return "P";
            }
            return piece.getUnicode();
        }
        return "";
    }
}
