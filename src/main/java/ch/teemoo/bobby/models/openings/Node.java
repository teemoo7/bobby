package ch.teemoo.bobby.models.openings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.teemoo.bobby.models.moves.Move;

public class Node {
	private final Move move;
	private final Map<String, Node> children = new HashMap<>();
	private String openingName;

	public Node(Move move) {
		this.move = move;
	}

	public Move getMove() {
		return move;
	}

	public String getOpeningName() {
		return openingName;
	}

	public void setOpeningName(String openingName) {
		this.openingName = openingName;
	}

	public Optional<Node> getNodeForMove(Move move) {
		return Optional.ofNullable(children.get(move.getBasicNotation()));
	}

	public void addNode(Node node) {
		children.put(node.getMove().getBasicNotation(), node);
	}

	public List<Node> getChildren() {
		return new ArrayList<>(this.children.values());
	}
}
