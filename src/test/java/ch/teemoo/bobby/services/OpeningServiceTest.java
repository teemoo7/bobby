package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Pawn;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OpeningServiceTest {

	private OpeningService openingService;
	private FileService fileService;
	private PortableGameNotationService portableGameNotationService;
	private MoveService moveService;

	@BeforeEach
	public void setUp() {
		this.fileService = new FileService();
		this.moveService = new MoveService();
		this.portableGameNotationService = new PortableGameNotationService(moveService);
		this.openingService = new OpeningService(portableGameNotationService, fileService);
	}

	@Test
	public void testPrettyPrintTree() {
		// given

		// when
		String tree = openingService.prettyPrintTree();

		// then
		assertThat(tree).isNotEmpty();
		System.out.println(tree);
	}

	@Test
	public void testBuildTreeIOExceptionCaught() throws IOException {
		// given
		FileService fileServiceMock = mock(FileService.class);
		PortableGameNotationService portableGameNotationServiceMock = mock(PortableGameNotationService.class);
		when(fileServiceMock.readFileFromResourceFolder(any(), any())).thenThrow(new IOException("test"));

		// when
		OpeningService openingServiceWithException =
			new OpeningService(portableGameNotationServiceMock, fileServiceMock);


		// then
		String tree = openingServiceWithException.prettyPrintTree();
		assertThat(tree).isNotEmpty();
		assertThat(tree.split("\n")).hasSize(1);
	}

	@Test
	public void testFindPossibleMovesForHistory() {
		// given
		List<Move> history = Arrays.asList(new Move(new Pawn(Color.WHITE), 4, 1, 4, 3));

		// when
		List<Move> moves = openingService.findPossibleMovesForHistory(history);

		// then
		assertThat(moves).hasSizeGreaterThan(0);
	}

	@Test
	public void testFindPossibleMovesForHistoryEmpty() {
		// given
		List<Move> history = Arrays.asList(new Move(new Pawn(Color.WHITE), 0, 1, 0, 3));

		// when
		List<Move> moves = openingService.findPossibleMovesForHistory(history);

		// then
		assertThat(moves).isEmpty();
	}
}
