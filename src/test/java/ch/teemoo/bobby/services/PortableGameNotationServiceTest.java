package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import ch.teemoo.bobby.models.CastlingMove;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.Game;
import ch.teemoo.bobby.models.PromotionMove;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PortableGameNotationServiceTest {
	private final static String PGN_OPENING_RUY_LOPEZ_CONTENT = "[Event \"?\"]\n" + "[Site \"?\"]\n"
		+ "[Date \"????.??.??\"]\n" + "[Round \"?\"]\n" + "[White \"?\"]\n" + "[Black \"?\"]\n" + "[Result \"*\"]\n"
		+ "\n" + "1. e4 e5 2. Nf3 Nc6 3. Bb5 *";

	private final static String PGN_GAME_CONTENT = "[Event \"F/S Return Match\"]\n" + "[Site \"Belgrade, Serbia JUG\"]\n"
		+ "[Date \"1992.11.04\"]\n" + "[Round \"29\"]\n" + "[White \"Fischer, Robert J.\"]\n"
		+ "[Black \"Spassky, Boris V.\"]\n" + "[Result \"1/2-1/2\"]\n" + "\n"
		+ "1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 {This opening is called the Ruy Lopez.}\n"
		+ "4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7\n"
		+ "11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5\n"
		+ "Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6\n"
		+ "23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5\n"
		+ "hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kf2 Bf5\n"
		+ "35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6\n"
		+ "Nf2 42. g4 Bd3 43. Re6 1/2-1/2";

	private final static String PGN_GAME_WORLD_CHAMPIONSHIP_CONTENT = "[Event \"World Championship 28th\"]\n"
		+ "[Site \"Reykjavik\"]\n" + "[Date \"1972.??.??\"]\n" + "[Round \"13\"]\n" + "[White \"Spassky, Boris V\"]\n"
		+ "[Black \"Fischer, Robert James\"]\n" + "[Result \"0-1\"]\n" + "[WhiteElo \"2660\"]\n"
		+ "[BlackElo \"2785\"]\n" + "[ECO \"B04\"]\n" + "\n"
		+ "1.e4 Nf6 2.e5 Nd5 3.d4 d6 4.Nf3 g6 5.Bc4 Nb6 6.Bb3 Bg7 7.Nbd2 O-O 8.h3 a5\n"
		+ "9.a4 dxe5 10.dxe5 Na6 11.O-O Nc5 12.Qe2 Qe8 13.Ne4 Nbxa4 14.Bxa4 Nxa4 15.Re1 Nb6\n"
		+ "16.Bd2 a4 17.Bg5 h6 18.Bh4 Bf5 19.g4 Be6 20.Nd4 Bc4 21.Qd2 Qd7 22.Rad1 Rfe8\n"
		+ "23.f4 Bd5 24.Nc5 Qc8 25.Qc3 e6 26.Kh2 Nd7 27.Nd3 c5 28.Nb5 Qc6 29.Nd6 Qxd6\n"
		+ "30.exd6 Bxc3 31.bxc3 f6 32.g5 hxg5 33.fxg5 f5 34.Bg3 Kf7 35.Ne5+ Nxe5 36.Bxe5 b5\n"
		+ "37.Rf1 Rh8 38.Bf6 a3 39.Rf4 a2 40.c4 Bxc4 41.d7 Bd5 42.Kg3 Ra3+ 43.c3 Rha8\n"
		+ "44.Rh4 e5 45.Rh7+ Ke6 46.Re7+ Kd6 47.Rxe5 Rxc3+ 48.Kf2 Rc2+ 49.Ke1 Kxd7 50.Rexd5+ Kc6\n"
		+ "51.Rd6+ Kb7 52.Rd7+ Ka6 53.R7d2 Rxd2 54.Kxd2 b4 55.h4 Kb5 56.h5 c4 57.Ra1 gxh5\n"
		+ "58.g6 h4 59.g7 h3 60.Be7 Rg8 61.Bf8 h2 62.Kc2 Kc6 63.Rd1 b3+ 64.Kc3 h1=Q\n"
		+ "65.Rxh1 Kd5 66.Kb2 f4 67.Rd1+ Ke4 68.Rc1 Kd3 69.Rd1+ Ke2 70.Rc1 f3 71.Bc5 Rxg7\n"
		+ "72.Rxc4 Rd7 73.Re4+ Kf1 74.Bd4 f2  0-1\n";

	private PortableGameNotationService portableGameNotationService;

	@Mock
	private FileService fileService;

	@Spy
	private MoveService moveService;

	@Before
	public void setUp() {
		this.moveService = new MoveService();
		this.portableGameNotationService = new PortableGameNotationService(fileService, moveService);
	}

	@Test
	public void readPgnOpeningRuyLopezFileTest() throws IOException {
		// given
		when(fileService.readFile(any())).thenReturn(Arrays.asList(PGN_OPENING_RUY_LOPEZ_CONTENT.split("\\n")));

		// when
		Game game = portableGameNotationService.readPgnFile(null);

		// then
		// headers check
		assertThat(game.getWhitePlayer()).isNotNull();
		assertThat(game.getWhitePlayer().getName()).isEqualTo("?");
		assertThat(game.getBlackPlayer()).isNotNull();
		assertThat(game.getBlackPlayer().getName()).isEqualTo("?");

		// moves check
		assertThat(game.getHistory()).hasSize(5);
	}

	@Test
	public void readPgnGameFileTest() throws IOException {
		// given
		when(fileService.readFile(any())).thenReturn(Arrays.asList(PGN_GAME_CONTENT.split("\\n")));

		// when
		Game game = portableGameNotationService.readPgnFile(null);

		// then
		// headers check
		assertThat(game.getWhitePlayer()).isNotNull();
		assertThat(game.getWhitePlayer().getName()).isEqualTo("Fischer, Robert J.");
		assertThat(game.getBlackPlayer()).isNotNull();
		assertThat(game.getBlackPlayer().getName()).isEqualTo("Spassky, Boris V.");

		// moves check
		assertThat(game.getHistory()).hasSize(85);

		assertThat(game.getHistory().get(0).getPiece()).isInstanceOf(Pawn.class);
		assertThat(game.getHistory().get(0).getPiece().getColor()).isEqualTo(Color.WHITE);
		assertThat(game.getHistory().get(0).getToY()).isEqualTo(3);

		assertThat(game.getHistory().get(1).getPiece()).isInstanceOf(Pawn.class);
		assertThat(game.getHistory().get(1).getPiece().getColor()).isEqualTo(Color.BLACK);
		assertThat(game.getHistory().get(1).getToY()).isEqualTo(4);

		assertThat(game.getHistory().get(8)).isInstanceOf(CastlingMove.class);
		assertThat(game.getHistory().get(8).getToX()).isEqualTo(6);

		assertThat(game.getHistory().get(34).getPiece()).isInstanceOf(Bishop.class);
		assertThat(game.getHistory().get(34).getPiece().getColor()).isEqualTo(Color.WHITE);
		assertThat(game.getHistory().get(34).isTaking()).isTrue();

		assertThat(game.getHistory().get(49).getPiece()).isInstanceOf(Rook.class);
		assertThat(game.getHistory().get(49).getPiece().getColor()).isEqualTo(Color.BLACK);
		assertThat(game.getHistory().get(49).isTaking()).isTrue();
		assertThat(game.getHistory().get(49).isChecking()).isTrue();
	}

	@Test
	public void readPgnBigGameFileTest() throws IOException {
		// given
		when(fileService.readFile(any())).thenReturn(Arrays.asList(PGN_GAME_WORLD_CHAMPIONSHIP_CONTENT.split("\\n")));

		// when
		Game game = portableGameNotationService.readPgnFile(null);

		// then
		// headers check
		assertThat(game.getWhitePlayer()).isNotNull();
		assertThat(game.getWhitePlayer().getName()).isEqualTo("Spassky, Boris V");
		assertThat(game.getBlackPlayer()).isNotNull();
		assertThat(game.getBlackPlayer().getName()).isEqualTo("Fischer, Robert James");

		// moves check
		assertThat(game.getHistory()).hasSize(148);

		assertThat(game.getHistory().get(127).getPiece()).isInstanceOf(Pawn.class);
		assertThat(game.getHistory().get(127).getPiece().getColor()).isEqualTo(Color.BLACK);
		assertThat(game.getHistory().get(127).isTaking()).isFalse();
		assertThat(game.getHistory().get(127).isChecking()).isFalse();
		assertThat(game.getHistory().get(127)).isInstanceOf(PromotionMove.class);
		assertThat(((PromotionMove) game.getHistory().get(127)).getPromotedPiece()).isInstanceOf(Queen.class);
	}

	@Test
	public void testUnexpectedMove() throws IOException {
		// given
		String pgn = "[Event \"?\"]\n" + "[Site \"?\"]\n" + "[Date \"????.??.??\"]\n" + "[Round \"?\"]\n"
			+ "[White \"?\"]\n" + "[Black \"?\"]\n" + "[Result \"*\"]\n\n1. O-O-O *";
		when(fileService.readFile(any())).thenReturn(Arrays.asList(pgn.split("\\n")));

		// when
		ThrowableAssert.ThrowingCallable callable = () -> portableGameNotationService.readPgnFile(null);

		// then
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(callable).withMessageContaining("Unexpected move");
	}

}
