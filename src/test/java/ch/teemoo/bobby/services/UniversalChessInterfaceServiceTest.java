package ch.teemoo.bobby.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.moves.PromotionMove;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Pawn;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.models.players.Human;
import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UniversalChessInterfaceServiceTest {

	private UniversalChessInterfaceService universalChessInterfaceService;

	@Before
	public void setUp() {
		this.universalChessInterfaceService = new UniversalChessInterfaceService(new MoveService());
	}

	@Test
	public void getMoveFromUciNotationTest() {
		// given
		var uciMove = "g1f3";
		var game = new Game(new Human("a"), new Human("b"));

		// when
		var move = universalChessInterfaceService.getMoveFromUciNotation(uciMove, game);

		// then
		assertThat(move).isNotNull();
		assertThat(move.equalsForPositions(new Move(new Knight(Color.WHITE), 6, 0, 5, 2))).isTrue();
		assertThat(move.getPiece()).isInstanceOf(Knight.class);
		assertThat(move.getPiece().getColor()).isEqualTo(Color.WHITE);
		assertThat(move.getUciNotation()).isEqualTo(uciMove);
	}

	@Test
	public void getPromotionMoveFromUciNotationTest() {
		// given
		var uciMove = "a7a8q";
		var game = new Game(new Human("a"), new Human("b"));
		var promotionMove = new PromotionMove(new Move(new Pawn(Color.BLACK), 0, 6, 0, 7), new Queen(Color.BLACK));
		var moveServiceMock = mock(MoveService.class);
		universalChessInterfaceService = new UniversalChessInterfaceService(moveServiceMock);
		when(moveServiceMock.computeAllMoves(any(), any(), anyList(), anyBoolean())).thenReturn(List.of(promotionMove));

		// when
		var move = universalChessInterfaceService.getMoveFromUciNotation(uciMove, game);

		// then
		assertThat(move).isNotNull();
		assertThat(move).isEqualTo(promotionMove);
		assertThat(move.getUciNotation()).isEqualTo(uciMove);
	}

	@Test
	public void getMoveFromUciNotationUnexpectedMoveTest() {
		// given
		var uciMove = "a1b1";
		var game = new Game(new Human("a"), new Human("b"));

		// when
		ThrowableAssert.ThrowingCallable callable =
			() -> universalChessInterfaceService.getMoveFromUciNotation(uciMove, game);

		// then
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(callable)
			.withMessageStartingWith("Unexpected move");
	}

	@Test
	public void getMoveFromUciNotationAmbiguousMoveTest() {
		// given
		var uciMove = "a1b1";
		var game = new Game(new Human("a"), new Human("b"));
		var move1 = new Move(new Rook(Color.WHITE), 0, 0, 1, 0);
		var move2 = new Move(new Queen(Color.WHITE), 0, 0, 1, 0);
		var moveServiceMock = mock(MoveService.class);
		universalChessInterfaceService = new UniversalChessInterfaceService(moveServiceMock);
		when(moveServiceMock.computeAllMoves(any(), any(), anyList(), anyBoolean())).thenReturn(List.of(move1, move2));

		// when
		ThrowableAssert.ThrowingCallable callable =
			() -> universalChessInterfaceService.getMoveFromUciNotation(uciMove, game);

		// then
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(callable)
			.withMessageStartingWith("Ambiguous move");
	}

	@Test
	public void getPromotedPieceFromCharTest() {
		// given
		var queenChar = 'q';
		var rookChar = 'r';
		var bishopChar = 'b';
		var knightChar = 'n';

		// when
		var queen = universalChessInterfaceService.getPromotedPieceFromChar(queenChar, Color.WHITE);
		var rook = universalChessInterfaceService.getPromotedPieceFromChar(rookChar, Color.BLACK);
		var bishop = universalChessInterfaceService.getPromotedPieceFromChar(bishopChar, Color.WHITE);
		var knight = universalChessInterfaceService.getPromotedPieceFromChar(knightChar, Color.BLACK);

		// then
		assertThat(queen).isInstanceOf(Queen.class);
		assertThat(queen.getColor()).isEqualTo(Color.WHITE);
		assertThat(rook).isInstanceOf(Rook.class);
		assertThat(rook.getColor()).isEqualTo(Color.BLACK);
		assertThat(bishop).isInstanceOf(Bishop.class);
		assertThat(bishop.getColor()).isEqualTo(Color.WHITE);
		assertThat(knight).isInstanceOf(Knight.class);
		assertThat(knight.getColor()).isEqualTo(Color.BLACK);
	}

	@Test
	public void getPromotedPieceFromCharInvalidTest() {
		// given
		var kingChar = 'k';

		// when
		ThrowableAssert.ThrowingCallable callable =
			() -> universalChessInterfaceService.getPromotedPieceFromChar(kingChar, Color.WHITE);

		// then
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(callable).withMessage("Invalid promoted piece");
	}

}
