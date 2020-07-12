package ch.teemoo.bobby.models.games;

public class GameResult {
	private final int nbMoves;
	private final Result result;

	public GameResult(int nbMoves, Result result) {
		this.nbMoves = nbMoves;
		this.result = result;
	}

	public int getNbMoves() {
		return nbMoves;
	}

	public Result getResult() {
		return result;
	}

	public enum Result {
		WHITE_WINS, BLACK_WINS, DRAW
	}
}
