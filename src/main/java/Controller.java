import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import gui.BoardView;
import gui.Square;
import models.Board;
import models.Move;

public class Controller {
	private static final Border RED_BORDER = BorderFactory.createLineBorder(java.awt.Color.red);
	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(java.awt.Color.blue);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();

	private final BoardView view;
	private final Board board;
	private final MoveService moveService = new MoveService();

	private Square selectedSquare = null;

	public Controller(BoardView view, Board board) {
		this.view = view;
		this.board = board;
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

		Square[][] squares = getView().getSquares();
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				Square square = squares[i][j];
				square.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						squareClicked(square);
					}
				});
			}
		}
	}

	private void squareClicked(Square square) {
		Square selectedSquare = getSelectedSquare();
		if (selectedSquare != null) {
			if (selectedSquare == square) {
				// reset current selection
				cleanSquaresBorder();
				cleanSelectedSquare();
			} else {
				//check if move is authorized
				List<Move> moves = getMoveService()
					.computeMoves(board, selectedSquare.getPiece(), selectedSquare.getPosition().getX(), selectedSquare.getPosition().getY(),
						false);

				Optional<Move> myMove = moves.stream().filter(move -> move.getToX() == square.getPosition().getX() && move.getToY() == square.getPosition().getY()).findAny();
				boolean isAuthorized = myMove.isPresent();
				if (isAuthorized) {
					getBoard().doMove(myMove.get());
					cleanSelectedSquare();
					cleanSquaresBorder();
					getView().refresh(board.getBoard());
					if (myMove.get().isTaking()) {
						getView().popup("Taking piece!");
					}
				} else {
					//todo: log error, should not happen
				}
			}
		} else {
			if (square.getPiece() != null) {
				cleanSquaresBorder();
				setSelectedSquare(square);
				square.setBorder(RED_BORDER);
				List<Move> moves = getMoveService()
					.computeMoves(board, square.getPiece(), square.getPosition().getX(), square.getPosition().getY(),
						false);
				for (Move move : moves) {
					getView().getSquares()[move.getToY()][move.getToX()].setBorder(BLUE_BORDER);
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
}
