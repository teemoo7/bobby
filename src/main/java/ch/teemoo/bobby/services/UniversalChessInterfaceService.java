package ch.teemoo.bobby.services;

import java.util.List;
import java.util.function.Predicate;

import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.games.Game;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.moves.PromotionMove;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UniversalChessInterfaceService {
	private final static Logger logger = LoggerFactory.getLogger(UniversalChessInterfaceService.class);

	private final MoveService moveService;

	public UniversalChessInterfaceService(MoveService moveService) {
		this.moveService = moveService;
	}

	public Move getMoveFromUciNotation(String uciMove, Game game) {
		char fromXChar = uciMove.charAt(0);
		char fromYChar = uciMove.charAt(1);
		char toXChar = uciMove.charAt(2);
		char toYChar = uciMove.charAt(3);

		Move basicMove = new Move(null, Move.convertCharToX(fromXChar), Character.getNumericValue(fromYChar) - 1,
			Move.convertCharToX(toXChar), Character.getNumericValue(toYChar) - 1);

		final Move move;
		if (uciMove.length() > 4) {
			char promotionChar = uciMove.charAt(4);
			move = new PromotionMove(basicMove, getPromotedPieceFromChar(promotionChar, game.getToPlay()));
		} else {
			move = basicMove;
		}

		List<Move> allowedMoves =
			moveService.computeAllMoves(game.getBoard(), game.getToPlay(), game.getHistory(), true);

		final Predicate<Move> fromXCond = m -> move.getFromX() < 0 || m.getFromX() == move.getFromX();
		final Predicate<Move> fromYCond = m -> move.getFromY() < 0 || m.getFromY() == move.getFromY();
		final Predicate<Move> toXCond = m -> move.getToX() < 0 || m.getToX() == move.getToX();
		final Predicate<Move> toYCond = m -> move.getToY() < 0 || m.getToY() == move.getToY();
		final Predicate<Move> promotionCond =
			m -> !(move instanceof PromotionMove) || ((m instanceof PromotionMove) && ((PromotionMove) move)
				.getPromotedPiece().getClass().equals(((PromotionMove) m).getPromotedPiece().getClass()));

		List<Move> matchingMoves =
			allowedMoves.stream().filter(fromXCond).filter(fromYCond).filter(toXCond).filter(toYCond)
				.filter(promotionCond).toList();
		if (matchingMoves.size() < 1) {
			logger.error("Move {} is not allowed here. Allowed moves are {}", uciMove, allowedMoves);
			throw new RuntimeException("Unexpected move: " + uciMove);
		} else if (matchingMoves.size() > 1) {
			logger.error("Move {} is ambiguous here. Allowed moves are {}", uciMove, allowedMoves);
			throw new RuntimeException("Ambiguous move: " + uciMove);
		}
		return matchingMoves.get(0);
	}

	Piece getPromotedPieceFromChar(char c, Color color) {
		return switch (c) {
			case 'q' -> new Queen(color);
			case 'r' -> new Rook(color);
			case 'b' -> new Bishop(color);
			case 'n' -> new Knight(color);
			default -> throw new RuntimeException("Invalid promoted piece");
		};
	}

}
