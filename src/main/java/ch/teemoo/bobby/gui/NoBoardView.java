package ch.teemoo.bobby.gui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Optional;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.models.Color;
import ch.teemoo.bobby.models.games.GameSetup;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Piece;

public class NoBoardView implements IBoardView {
	@Override
	public Square[][] getSquares() {
		return new Square[0][];
	}

	@Override
	public void setItemNewActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemSaveActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemLoadActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemPrintToConsoleActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemSuggestMoveActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemUndoMoveActionListener(ActionListener actionListener) {

	}

	@Override
	public void setItemProposeDrawActionListener(ActionListener actionListener) {

	}

	@Override
	public void display(Piece[][] positions, boolean isReversed) {

	}

	@Override
	public void refresh(Piece[][] positions) {

	}

	@Override
	public void resetAllClickables() {

	}

	@Override
	public void cleanSquaresBorder() {

	}

	@Override
	public void addBorderToLastMoveSquares(Move move) {

	}

	@Override
	public Optional<File> saveGameDialog() {
		return Optional.empty();
	}

	@Override
	public Optional<File> loadGameDialog() {
		return Optional.empty();
	}

	@Override
	public GameSetup gameSetupDialog(BotFactory botFactory, boolean exitOnCancel) {
		return null;
	}

	@Override
	public Piece promotionDialog(Color color) {
		return null;
	}

	@Override
	public void popupInfo(String message) {

	}

	@Override
	public void popupError(String message) {

	}
}
