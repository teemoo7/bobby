package models;

import models.pieces.Piece;

public class Move {
	private final Piece piece;
	private final int fromX;
	private final int fromY;
	private final int toX;
	private final int toY;
	private boolean isTaking;

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
}
