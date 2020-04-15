package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.openings.Node;
import ch.teemoo.bobby.models.pieces.Pawn;
import org.junit.Before;
import org.junit.Test;

public class OpeningServiceTest {

	private OpeningService openingService;
	private FileService fileService;
	private PortableGameNotationService portableGameNotationService;
	private MoveService moveService;

	@Before
	public void setUp() {
		this.fileService = new FileService();
		this.moveService = new MoveService();
		this.portableGameNotationService = new PortableGameNotationService(fileService, moveService);
		this.openingService = new OpeningService(portableGameNotationService, fileService);
	}

	@Test
	public void testPrettyPrintTree() {
		String tree = openingService.prettyPrintTree();
		assertThat(tree).isNotEmpty();
		System.out.println(tree);
	}

	@Test
	public void testFindPossibleMovesForHistory() {
		List<Move> history = Arrays.asList(
			new Move(new Pawn(Color.WHITE), 4, 1, 4, 3)
		);
		List<Node> nodes = openingService.findPossibleMovesForHistory(history);
		assertThat(nodes).hasSize(1);
	}

	@Test
	public void testFindPossibleMovesForHistoryEmpty() {
		List<Move> history = Arrays.asList(
			new Move(new Pawn(Color.WHITE), 0, 1, 0, 3)
		);
		List<Node> nodes = openingService.findPossibleMovesForHistory(history);
		assertThat(nodes).isEmpty();
	}
}
