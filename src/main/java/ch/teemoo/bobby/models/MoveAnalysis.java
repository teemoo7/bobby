package ch.teemoo.bobby.models;

public class MoveAnalysis {
	private final Move move;
	private int score;
	private MoveAnalysis nextProbableMove;

	public MoveAnalysis(Move move) {
		this.move = move;
	}

	public Move getMove() {
		return move;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public MoveAnalysis getNextProbableMove() {
		return nextProbableMove;
	}

	public void setNextProbableMove(MoveAnalysis nextProbableMove) {
		this.nextProbableMove = nextProbableMove;
	}
}
