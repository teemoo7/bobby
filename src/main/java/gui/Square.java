package gui;

import java.awt.Font;

import javax.swing.JLabel;

import models.Position;
import models.pieces.Piece;

public class Square extends JLabel {

    private final Piece piece;
    private final Position position;

    Square(Piece piece, Position position, Background background) {
        super(piece != null ? piece.getUnicode() : "");
        this.piece = piece;
        this.position = position;
        setFont(new Font("DejaVu Sans", Font.PLAIN, 48));
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setBackground(background.getColor());
    }

    Square(Position position, Background background) {
        this(null, position, background);
    }

    public Piece getPiece() {
        return piece;
    }

    public Position getPosition() {
        return position;
    }
}
