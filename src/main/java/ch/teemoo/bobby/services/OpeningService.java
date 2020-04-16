package ch.teemoo.bobby.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.openings.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpeningService {
	private final static Logger logger = LoggerFactory.getLogger(OpeningService.class);

	private final PortableGameNotationService portableGameNotationService;
	private final FileService fileService;
	private final Node openingsTree;

	public OpeningService(PortableGameNotationService portableGameNotationService, FileService fileService) {
		this.portableGameNotationService = portableGameNotationService;
		this.fileService = fileService;
		this.openingsTree = buildTree();
	}

	public List<Move> findPossibleMovesForHistory(List<Move> history) {
		Node currentNode = openingsTree;
		for (Move move: history) {
			Optional<Node> nodeOpt = currentNode.getNodeForMove(move);
			if (nodeOpt.isPresent()) {
				currentNode = nodeOpt.get();
			} else {
				return Collections.emptyList();
			}
		}
		return currentNode.getChildren().stream().map(Node::getMove).collect(Collectors.toList());
	}

	public String prettyPrintTree() {
		return prettyPrintNode(openingsTree, 0);
	}

	private Node buildTree() {
		List<Game> games = new ArrayList<>();
		for (File file : fileService.getFilesFromResourceFolder("openings")) {
			try {
				games.add(portableGameNotationService.readPgnFile(file));
			} catch (IOException e) {
				logger.error("Opening could not be read from file {}", file.getName(), e);
			}
		}

		Node root = new Node(null);

		for (Game game : games) {
			Node currentNode = root;
			List<Move> moves = game.getHistory();
			for (Move move : moves) {
				Optional<Node> nextNodeOpt = currentNode.getNodeForMove(move);
				if (nextNodeOpt.isEmpty()) {
					Node node = new Node(move);
					currentNode.addNode(node);
					currentNode = node;
				} else {
					currentNode = nextNodeOpt.get();
				}
				if (move.equals(moves.get(moves.size() - 1))) {
					// last move
					currentNode.setOpeningName(game.getOpening());
				}
			}
		}
		logger.info("{} openings loaded", games.size());

		return root;
	}

	private String prettyPrintNode(Node node, int level) {
		String result = "";
		for (int i = 0; i < level; i++) {
			result += "\t";
		}
		if (level == 0) {
			result += "<START>";
		} else {
			result += node.getMove();
		}
		if (node.getOpeningName() != null) {
			result += " [" + node.getOpeningName() + "]";
		}
		result += "\n";
		List<String> subTrees =
			node.getChildren().stream().map(child -> prettyPrintNode(child, level + 1)).collect(Collectors.toList());
		for (String subTree : subTrees) {
			result += subTree;
		}
		return result;
	}

}
