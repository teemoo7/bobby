package ch.teemoo.bobby.gui;

import java.awt.event.ActionListener;
import java.io.File;
import java.util.Optional;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.models.games.GameSetup;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.pieces.Piece;

public interface IBoardView {
	
	Square[][] getSquares();

	void setItemNewActionListener(ActionListener actionListener);

	void setItemSaveActionListener(ActionListener actionListener);

	void setItemLoadActionListener(ActionListener actionListener);

	void setItemPrintToConsoleActionListener(ActionListener actionListener);

	void setItemSuggestMoveActionListener(ActionListener actionListener);

	void setItemUndoMoveActionListener(ActionListener actionListener);

	void setItemProposeDrawActionListener(ActionListener actionListener);

	void display(Piece[][] positions, boolean isReversed);

	void refresh(Piece[][] positions);

	void resetAllClickables();

	void cleanSquaresBorder();

	void addBorderToLastMoveSquares(Move move);

	Optional<File> saveGameDialog();

	Optional<File> loadGameDialog();

	GameSetup gameSetupDialog(BotFactory botFactory, boolean exitOnCancel);

	Piece promotionDialog(ch.teemoo.bobby.models.Color color);

	void popupInfo(String message);

	void popupError(String message);

}
