package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.RandomBot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileServiceTest {

	private FileService fileService = new FileService();

	private File file;

	@Before
	public void setUp() throws IOException {
		file = File.createTempFile("temp", "tmp");
		file.deleteOnExit();
	}

	@Test
	public void testReadFile() throws IOException {
		// given
		Files.write(Paths.get(file.toURI()), Arrays.asList("Hello", "World"));

		// when
		List<String> lines = fileService.readFile(file);

		// then
		assertThat(lines).hasSize(2);
	}

	@Test
	public void testWriteGameToFileBasicNotation() throws IOException {
		// given
		Game game = new Game(new Human("A"), new Human("B"));
		game.addMoveToHistory(new Move(new Pawn(Color.WHITE), 0, 1, 0, 2));

		// when
		fileService.writeGameToFileBasicNotation(game, file);

		// then
		assertThat(file.length()).isGreaterThan(0);
	}
}
