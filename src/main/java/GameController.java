import static helpers.ColorHelper.swap;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import gui.BoardView;
import gui.Square;
import models.Board;
import models.Color;
import models.Game;
import models.Move;
import models.pieces.Piece;

public class GameController {
	private static final Border RED_BORDER = BorderFactory.createLineBorder(java.awt.Color.red);
	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(java.awt.Color.blue);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();

	private final BoardView view;
	private final Board board;
	private final Game game;
	private final MoveService moveService = new MoveService();

	private Square selectedSquare = null;

	public GameController(BoardView view, Game game) {
		this.view = view;
		this.game = game;
		this.board = game.getBoard();
		init();
	}

	public BoardView getView() {
		return view;
	}

	public Board getBoard() {
		return board;
	}

	public MoveService getMoveService() {
		return moveService;
	}

	public Square getSelectedSquare() {
		return selectedSquare;
	}

	public void setSelectedSquare(Square square) {
		this.selectedSquare = square;
	}

	public void cleanSelectedSquare() {
		this.selectedSquare = null;
	}

	private void init() {
		view.display(board.getBoard());
		resetAllClickables();
		markSquaresClickableByColor(game.getToPlay());
	}

	private void resetAllClickables() {
		Square[][] squares = getView().getSquares();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Square square = squares[i][j];
				Stream.of(square.getMouseListeners()).forEach(square::removeMouseListener);
			}
		}
	}

	private void markSquaresClickableByColor(Color color) {
		Square[][] squares = getView().getSquares();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Square square = squares[i][j];
				Piece piece = square.getPiece();
				if (piece != null && piece.getColor() == color) {
					markSquareClickable(square);
				}
			}
		}
	}

	private void markSquareClickable(Square square) {
		square.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							squareClicked(square);
						}
						public void mouseEntered(MouseEvent e) {
							square.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
						}
						public void mouseExited(MouseEvent e) {
							square.setCursor(Cursor.getDefaultCursor());
						}
					});
	}

	private void squareClicked(Square square) {
		Square selectedSquare = getSelectedSquare();
		if (selectedSquare != null) {
			if (selectedSquare == square) {
				// reset current selection
				cleanSquaresBorder();
				cleanSelectedSquare();
				resetAllClickables();
				markSquaresClickableByColor(game.getToPlay());
			} else {
				//check if move is authorized
				List<Move> moves = getMoveService()
					.computeMoves(board, selectedSquare.getPiece(), selectedSquare.getPosition().getX(), selectedSquare.getPosition().getY());

				Optional<Move> moveOpt = moves.stream().filter(move -> move.getToX() == square.getPosition().getX() && move.getToY() == square.getPosition().getY()).findAny();
				boolean isAuthorized = moveOpt.isPresent();
				if (isAuthorized) {
					Move move = moveOpt.get();
					getBoard().doMove(move);
					getView().refresh(board.getBoard());
					game.addMoveToHistory(move);
					game.setToPlay(swap(move.getPiece().getColor()));
					cleanSelectedSquare();
					cleanSquaresBorder();
					resetAllClickables();
					markSquaresClickableByColor(game.getToPlay());
					if (move.isChecking()) {
						getView().popup("Check!");
					}
					//todo: check game ending
				} else {
					//todo: log error, should not happen
				}
			}
		} else {
			if (square.getPiece() != null) {
				if (square.getPiece().getColor() == game.getToPlay()) {
					//todo: check color to play
					cleanSquaresBorder();
					resetAllClickables();
					// to unselect
					markSquareClickable(square);
					setSelectedSquare(square);
					square.setBorder(RED_BORDER);
					List<Move> moves = getMoveService()
						.computeMoves(board, square.getPiece(), square.getPosition().getX(), square.getPosition().getY());
					for (Move move : moves) {
						Square destination = getView().getSquares()[move.getToY()][move.getToX()];
						destination.setBorder(BLUE_BORDER);
						markSquareClickable(destination);
					}
				} else {
					// Should not happen
					//todo: log error
				}
			} else {
				// cannot start by selecting an empty square, do nothing
				//todo: log error
			}
		}
	}

	private void cleanSquaresBorder() {
		Square[][] squares = getView().getSquares();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Square square = squares[i][j];
				square.setBorder(NO_BORDER);
			}
		}
	}
/*
	private GameStatus getGameStatus(Board board, Color color) {
		boolean canMove = getMoveService().canMove(board, color);
		boolean isInCheck = getMoveService().isInCheck(board, color);

		if (!canMove) {
			if (isInCheck) {
				return CheckMate;
			} else {
				return StaleMate;
			}
		}

		//todo: threefold repetition - draw
		//todo: 50-move (no pawn moved, no capture) - draw
	}
	*/
}
