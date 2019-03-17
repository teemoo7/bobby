package models;

import java.util.Objects;

import models.pieces.Piece;

public class Move {
	private final Piece piece;
	private final int fromX;
	private final int fromY;
	private final int toX;
	private final int toY;
	private boolean isTaking;
	private boolean isChecking;

	public Move(Piece piece, int fromX, int fromY, int toX, int toY) {
		this.piece = piece;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	}

	public Piece getPiece() {
		return piece;
	}

	public int getFromX() {
		return fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public int getToX() {
		return toX;
	}

	public int getToY() {
		return toY;
	}

	public boolean isTaking() {
		return isTaking;
	}

	public void setTaking(boolean taking) {
		isTaking = taking;
	}

	public boolean isChecking() {
		return isChecking;
	}

	public void setChecking(boolean checking) {
		isChecking = checking;
	}

	public String getBasicNotation() {
		return convertXToLetter(fromX) + fromY + "-" + convertXToLetter(toX) + toY;
	}

	private static String convertXToLetter(int x) {
		final int aAscii = (int) 'a';
		return String.valueOf((char) (aAscii + x));
	}

	public boolean equalsForPositions(Move move) {
		return fromX == move.fromX && fromY == move.fromY && toX == move.toX && toY == move.toY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Move move = (Move) o;
		return equalsForPositions(move)
			&& isTaking == move.isTaking && isChecking == move.isChecking && Objects.equals(piece, move.piece);
	}

	@Override
	public int hashCode() {
		return Objects.hash(piece, fromX, fromY, toX, toY, isTaking, isChecking);
	}
}
