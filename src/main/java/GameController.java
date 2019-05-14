import static helpers.ColorHelper.swap;
import static models.Board.SIZE;

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.Border;

import gui.BoardView;
import gui.Square;
import models.Board;
import models.Color;
import models.Game;
import models.GameState;
import models.Move;
import models.pieces.Piece;
import models.players.Bot;
import models.players.Player;
import services.MoveService;

public class GameController {
	private static final Border RED_BORDER = BorderFactory.createLineBorder(java.awt.Color.red, 3, true);
	private static final Border BLUE_BORDER = BorderFactory.createLineBorder(java.awt.Color.blue, 3, true);
	private static final Border NO_BORDER = BorderFactory.createEmptyBorder();

	private final BoardView view;
	private Board board;
	private Game game;
	private final MoveService moveService = new MoveService();

	private Square selectedSquare = null;

	public GameController(BoardView view, Game game) {
		this.view = view;
		this.game = game;
		this.board = game.getBoard();
		init();
		play();
	}

	private void init() {
		view.display(board.getBoard());
		view.setItemLoadActionListener(actionEvent -> loadGame());
		view.setItemSaveActionListener(actionEvent -> saveGame());
	}

	public void play() {
		while (game.getPlayerToPlay().isBot() && !isGameOver(game)) {
			Player player = game.getPlayerToPlay();
			if (!(player instanceof Bot)) {
				throw new RuntimeException("Player has to be a bot");
			}
			Bot bot = (Bot) player;
			Move move = bot.selectMove(game, moveService);
			doMove(move);
		}

		if (!game.getPlayerToPlay().isBot() && !isGameOver(game)) {
			resetAllClickables();
			markSquaresClickableByColor(game.getToPlay());
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
			info(move.getPrettyNotation(), false);
			game.addMoveToHistory(allowedMove);
			game.setToPlay(swap(allowedMove.getPiece().getColor()));
			displayGameInfo(player, allowedMove);
		} else {
			throw new RuntimeException("Unauthorized move");
		}
	}

	private void displayGameInfo(Player player, Move move) {
		GameState state = moveService.getGameState(game.getBoard(), game.getToPlay(), game.getHistory());
		switch (state) {
			case LOSS:
				Color winningColor = move.getPiece().getColor();
				Player winner = game.getPlayerByColor(winningColor);
				if (winningColor == Color.WHITE) {
					info("1-0" + getNbMovesInfo(game), false);
				} else {
					info("0-1" + getNbMovesInfo(game), false);
				}
				info("Checkmate! " + winner.getName() + " (" + winningColor + ") has won!", !player.isBot());
				break;
			case DRAW_STALEMATE:
				info("½–½" + getNbMovesInfo(game), false);
				info("Draw (Stalemate). The game is over.", !player.isBot());
				break;
			case DRAW_50_MOVES:
				info("½–½" + getNbMovesInfo(game), false);
				info("Draw (50 moves). The game is over.", !player.isBot());
				break;
			case DRAW_THREEFOLD:
				info("½–½" + getNbMovesInfo(game), false);
				info("Draw (threefold). The game is over.", !player.isBot());
				break;
			case IN_PROGRESS:
			default:
				if (move.isChecking()) {
					info("Check!", !player.isBot());
				}
				break;
		}
	}

	private String getNbMovesInfo(Game game) {
		return " (" + game.getHistory().size() + " moves)";
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
					error(exception, true);
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
		info(text, true);
	}

	private void info(String text, boolean withPopup) {
		System.out.println("[INFO] " + text);
		if (withPopup) {
			view.popupInfo(text);
		}
	}

	private void error(Exception exception, boolean withPopup) {
		System.err.println("An error happened: " + exception.getMessage());
		exception.printStackTrace();
		if (withPopup) {
			view.popupError(exception.getMessage());
		}
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

	private boolean isGameOver(Game game) {
		return !moveService.getGameState(game.getBoard(), game.getToPlay(), game.getHistory()).isInProgress();
	}

	private void cleanSelectedSquare() {
		this.selectedSquare = null;
	}

	private void saveGame() {
		final JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(view);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			Path path = Paths.get(file.toURI());
			try {
				Files.write(path, game.getHistory().stream().map(Move::getBasicNotation).collect(Collectors.toList()));
			} catch (IOException e) {
				error(e, true);
			}
		}
	}

	private void loadGame() {
		final JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(view);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			Path path = Paths.get(file.toURI());
			List<String> lines = new ArrayList<>();
			try {
				lines.addAll(Files.readAllLines(path));
			} catch (IOException e) {
				error(e, true);
			}
			//todo: here we assume that the game to load is played in the same config as currently
			Game loadedGame = new Game(game.getWhitePlayer(), game.getBlackPlayer());
			this.game = loadedGame;
			this.board = loadedGame.getBoard();
			//todo: reset game (pieces position)
			for (String line: lines) {
				Move move = Move.fromBasicNotation(line);
				Piece piece = board.getPiece(move.getFromX(), move.getFromY())
						.orElseThrow(() -> new RuntimeException("Unexpected move, no piece at this location"));
				doMove(new Move(piece, move.getFromX(), move.getFromY(), move.getToX(), move.getToY()));
			}
			play();
		}
	}
}
