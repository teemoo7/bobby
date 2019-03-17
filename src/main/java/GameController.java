import static helpers.ColorHelper.swap;
import static models.Board.SIZE;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

import gui.BoardView;
import gui.Square;
import models.*;
import models.pieces.Pawn;
import models.pieces.Piece;

public class GameController {
	private static final Border RED_BORDER = BorderFactory.createLineBorder(java.awt.Color.red, 3, true);
	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(java.awt.Color.blue, 3, true);
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

	private void init() {
		view.display(board.getBoard());
		play();
	}

	public void play() {
		if (getGameState(game) == GameState.IN_PROGRESS) {
			Color colorToPlay = game.getToPlay();
			Player nextPlayer = game.getPlayerByColor(colorToPlay);
			if (!nextPlayer.isBot()) {
				resetAllClickables();
				markSquaresClickableByColor(colorToPlay);
			} else {
				//todo: refactor bot IA
				List<Move> moves = moveService.computeAllMoves(board, colorToPlay);
				Move randomMove = moves.get(new Random().nextInt(moves.size()));
				doMove(randomMove);
				play();
			}
		}
	}

	private void doMove(Move move) {
		Player player = game.getPlayerByColor(move.getPiece().getColor());
		if (!player.isBot()) {
			cleanSelectedSquare();
			cleanSquaresBorder();
			resetAllClickables();
		}

		List<Move> allowedMoves = moveService.computeMoves(board, move.getPiece(), move.getFromX(), move.getFromY());
		Optional<Move> allowedMoveOpt = allowedMoves.stream().filter(m -> m.equalsForPositions(move)).findAny();
		if (allowedMoveOpt.isPresent()) {
			final Move allowedMove = allowedMoveOpt.get();
			// We use allowedMove instead of given move since it contains additional info like taking and check
			board.doMove(allowedMove);
			view.refresh(board.getBoard());
			game.addMoveToHistory(allowedMove);
			game.setToPlay(swap(allowedMove.getPiece().getColor()));

			GameState state = getGameState(game);
			switch (state) {
				case LOSS:
					Color winningColor = allowedMove.getPiece().getColor();
					Player winner = game.getPlayerByColor(winningColor);
					info("Checkmate! " + winner.getName() + " (" + winningColor + ") has won!");
					break;
				case DRAW:
					info("Draw. The game is over.");
					break;
				case IN_PROGRESS:
				default:
					if (allowedMove.isChecking()) {
						info("Check!");
					}
					break;
			}
		} else {
			throw new RuntimeException("Unauthorized move");
		}
	}

	private void resetAllClickables() {
		Square[][] squares = view.getSquares();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				Square square = squares[i][j];
				Stream.of(square.getMouseListeners()).forEach(square::removeMouseListener);
				square.setCursor(Cursor.getDefaultCursor());
			}
		}
	}

	private void markSquaresClickableByColor(Color color) {
		Square[][] squares = view.getSquares();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
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
				try {
					squareClicked(square);
				} catch (Exception exception) {
					error(exception);
				}
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
		if (selectedSquare != null) {
			if (selectedSquare == square) {
				// cancel current selection
				cleanSquaresBorder();
				cleanSelectedSquare();
				resetAllClickables();
				markSquaresClickableByColor(game.getToPlay());
			} else {
				doMove(new Move(selectedSquare.getPiece(), selectedSquare.getPosition().getX(), selectedSquare.getPosition().getY(), square.getPosition().getX(), square.getPosition().getY()));
				play();
			}
		} else {
			if (square.getPiece() != null) {
				if (square.getPiece().getColor() == game.getToPlay()) {
					selectedSquare = square;
					cleanSquaresBorder();
					resetAllClickables();
					// Self piece is clickable so that it selection can be cancelled
					markSquareClickable(square);
					square.setBorder(RED_BORDER);
					List<Move> moves = moveService
						.computeMoves(board, square.getPiece(), square.getPosition().getX(), square.getPosition().getY());
					for (Move move : moves) {
						Square destination = view.getSquares()[move.getToY()][move.getToX()];
						destination.setBorder(BLUE_BORDER);
						markSquareClickable(destination);
					}
				} else {
					throw new RuntimeException("Cannot select a piece from opponent to start a move");
				}
			} else {
				throw new RuntimeException("Cannot select an empty square to start a move");
			}
		}
	}

	private void info(String text) {
		System.out.println("[INFO] " + text);
		view.popupInfo(text);
	}

	private void error(Exception exception) {
		System.err.println(exception);
		view.popupError(exception.getMessage());
	}

	private void cleanSquaresBorder() {
		Square[][] squares = view.getSquares();
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				Square square = squares[i][j];
				square.setBorder(NO_BORDER);
			}
		}
	}

	private GameState getGameState(Game game) {
		if (!moveService.canMove(game.getBoard(), game.getToPlay())) {
			if (moveService.isInCheck(game.getBoard(), game.getToPlay())) {
				// Checkmate
				return GameState.LOSS;
			} else {
				// Stalemate
				return GameState.DRAW;
			}
		}

		List<Move> history = game.getHistory();
		if (history.size() >= 10) {
			Move move6 = history.get(history.size()-1);
			Move move4 = history.get(history.size()-5);
			Move move2 = history.get(history.size()-9);
			Move move5 = history.get(history.size()-2);
			Move move3 = history.get(history.size()-6);
			Move move1 = history.get(history.size()-10);
			if (move6.equals(move4) && move6.equals(move2) && move5.equals(move3) && move5.equals(move1)) {
				// Threefold repetition
				return GameState.DRAW;
			}
		}

		if (history.size() >= 50) {
			List<Move> last50Moves = history.subList(history.size() - 1 - 50, history.size() - 1);
			if (last50Moves.stream().noneMatch(move -> move.isTaking() || move.getPiece() instanceof Pawn)) {
				// 50-move (no pawn moved, no capture)
				return GameState.DRAW;
			}
		}

		return GameState.IN_PROGRESS;
	}

	private void cleanSelectedSquare() {
		this.selectedSquare = null;
	}
}
