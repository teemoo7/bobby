package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
		File file = File.createTempFile("test", "tmp");
		file.deleteOnExit();
		when(fileServiceMock.getFilesFromResourceFolder(anyString())).thenReturn(new File[]{file});
		when(portableGameNotationServiceMock.readPgnFile(eq(file))).thenThrow(new IOException("test"));

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
