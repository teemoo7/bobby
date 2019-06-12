package ch.teemoo.bobby.models;

import java.util.Objects;

import ch.teemoo.bobby.models.pieces.Piece;

public class Move {
	private final Piece piece;
	private final int fromX;
	private final int fromY;
	private final int toX;
	private final int toY;
	private Piece tookPiece = null;
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
		return tookPiece != null;
	}

	public Piece getTookPiece() {
		return tookPiece;
	}

	public void setTookPiece(Piece tookPiece) {
		this.tookPiece = tookPiece;
	}

	public boolean isChecking() {
		return isChecking;
	}

	public void setChecking(boolean checking) {
		isChecking = checking;
	}

	public String getPrettyNotation() {
		StringBuilder builder = new StringBuilder()
			.append(piece.getColor())
			.append(" ")
			.append(getBasicNotation())
			.append(" (")
			.append(piece.getClass().getSimpleName())
			.append(")");
		return builder.toString();
	}

	public String getBasicNotation() {
		StringBuilder builder = new StringBuilder();
		builder.append(convertXToChar(fromX))
			.append(fromY + 1)
			.append(isTaking() ? "x" : "-")
			.append(convertXToChar(toX))
			.append(toY + 1);
		if (isChecking) {
			builder.append("+");
		}
		return builder.toString();
	}

	public static Move fromBasicNotation(String notation, Color color) {
		if (notation == null) {
			throw new IllegalArgumentException("Unexpected format for basic notation move: " + notation);
		}

		Move move;
		if (notation.equalsIgnoreCase("0-0")) {
			if (color == Color.WHITE) {
				move = new CastlingMove(null, 4, 0, 6, 0, null, 7, 0, 5, 0);
			} else {
				move = new CastlingMove(null, 4, 7, 6, 7, null, 7, 7, 5, 7);
			}
			if (notation.indexOf('+') > -1) {
				move.setChecking(true);
			}
			return move;
		} else if (notation.equalsIgnoreCase("0-0-0")) {
			if (color == Color.WHITE) {
				move = new CastlingMove(null, 4, 0, 2, 0, null, 0, 0, 3, 0);
			} else {
				move = new CastlingMove(null, 4, 7, 2, 7, null, 0, 7, 3, 7);
			}
			if (notation.indexOf('+') > -1) {
				move.setChecking(true);
			}
			return move;
		}

		if (notation.length() < 5) {
			//todo: improve with regex
			throw new IllegalArgumentException("Unexpected format for basic notation move: " + notation);
		}
		char fromXChar = notation.charAt(0);
		char fromYChar = notation.charAt(1);
		char toXChar = notation.charAt(3);
		char toYChar = notation.charAt(4);

		move = new Move(null, convertCharToX(fromXChar), Character.getNumericValue(fromYChar) - 1,
				convertCharToX(toXChar), Character.getNumericValue(toYChar) - 1);

		if (notation.indexOf('+') > -1) {
			move.setChecking(true);
		}

		return move;
	}

	private static String convertXToChar(int x) {
		final int aAscii = (int) 'a';
		return String.valueOf((char) (aAscii + x));
	}

	private static int convertCharToX(char character) {
		final int aAscii = (int) 'a';
		final int charAscii = (int) character;
		return charAscii - aAscii;
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
			&& tookPiece == move.tookPiece && isChecking == move.isChecking && Objects.equals(piece, move.piece);
	}

	@Override
	public int hashCode() {
		return Objects.hash(piece, fromX, fromY, toX, toY, tookPiece, isChecking);
	}

	@Override
	public String toString() {
		return getPrettyNotation();
	}
}
