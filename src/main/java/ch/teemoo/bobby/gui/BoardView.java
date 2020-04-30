package ch.teemoo.bobby.gui;

import static ch.teemoo.bobby.models.Board.SIZE;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.helpers.GuiHelper;
import ch.teemoo.bobby.models.GameSetup;
import ch.teemoo.bobby.models.moves.Move;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Bishop;
import ch.teemoo.bobby.models.pieces.Knight;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.pieces.Queen;
import ch.teemoo.bobby.models.pieces.Rook;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


public class BoardView extends JFrame {
    private static final Border NO_BORDER = BorderFactory.createEmptyBorder();
    private static final Border GREEN_BORDER = BorderFactory.createLineBorder(java.awt.Color.green, 3, true);

    private final boolean visible;
    private final Container contentPane;
    private final Icon logoIcon;
    private final GuiHelper guiHelper;
    private Square[][] squares = new Square[SIZE][SIZE];

    private JMenuItem itemNew;
    private JMenuItem itemSave;
    private JMenuItem itemLoad;
    private JMenuItem itemPrintToConsole;
    private JMenuItem itemSuggestMove;
    private JMenuItem itemUndoMove;
    private JMenuItem itemProposeDraw;
    private JMenuItem itemAbout;

    public BoardView(String title, GuiHelper guiHelper) {
        this(title, guiHelper, true);
    }

    public BoardView(String title, GuiHelper guiHelper, boolean visible) {
        this.guiHelper = guiHelper;
        setTitle(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.visible = visible;
        this.contentPane = getContentPane();
        this.logoIcon =
            new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("img/logo.png"), "Bobby logo");
        int sizeWithLabels = 10;
        GridLayout gridLayout = new GridLayout(sizeWithLabels, sizeWithLabels);
        contentPane.setLayout(gridLayout);
        setSize(800, 800);
        setLocationRelativeTo(null);
        setMenu();
    }

    //fixme: do not expose squares array (mutable internal representation)
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Square[][] getSquares() {
        return squares;
    }

    public void setItemNewActionListener(ActionListener actionListener) {
        itemNew.addActionListener(actionListener);
    }

    public void setItemSaveActionListener(ActionListener actionListener) {
        itemSave.addActionListener(actionListener);
    }

    public void setItemLoadActionListener(ActionListener actionListener) {
        itemLoad.addActionListener(actionListener);
    }

    public void setItemPrintToConsoleActionListener(ActionListener actionListener) {
        itemPrintToConsole.addActionListener(actionListener);
    }

    public void setItemSuggestMoveActionListener(ActionListener actionListener) {
        itemSuggestMove.addActionListener(actionListener);
    }

    public void setItemUndoMoveActionListener(ActionListener actionListener) {
        itemUndoMove.addActionListener(actionListener);
    }

    public void setItemProposeDrawActionListener(ActionListener actionListener) {
        itemProposeDraw.addActionListener(actionListener);
    }

    public void display(Piece[][] positions, boolean isReversed) {
        java.util.List<Component> components = new ArrayList<>((SIZE + 2) ^ 2);
        addFilesLabels(components);
        Background background;
        for (int i = positions.length - 1; i >= 0; i--) {
            components.add(getRankLabel(i));
            background = getFirstSquareBackground(i);
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                // Inverse coordinates (positions is a 2D array, reversed)
                Square square = new Square(piece, new Position(j, i), background, guiHelper.getPieceFont());
                background = swapBackground(background);
                components.add(square);
                squares[i][j] = square;
            }
            components.add(getRankLabel(i));
        }
        addFilesLabels(components);
        contentPane.removeAll();
        if (isReversed) {
            ListIterator<Component> li = components.listIterator(components.size());
            while (li.hasPrevious()) {
                contentPane.add(li.previous());
            }
        } else {
            components.forEach(contentPane::add);
        }
        setVisible(visible);
    }

    public void refresh(Piece[][] positions) {
        for (int i = positions.length - 1; i >= 0; i--) {
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                // get current square, so that only its label is updated
                squares[i][j].setPiece(piece);
            }
        }
    }

    public void resetAllClickables() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Square square = squares[i][j];
                Stream.of(square.getMouseListeners()).forEach(square::removeMouseListener);
                square.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    public void cleanSquaresBorder() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                Square square = squares[i][j];
                square.setBorder(NO_BORDER);
            }
        }
    }

    public void addBorderToLastMoveSquares(Move move) {
        Square from = squares[move.getFromY()][move.getFromX()];
        Square to = squares[move.getToY()][move.getToX()];
        from.setBorder(GREEN_BORDER);
        to.setBorder(GREEN_BORDER);
    }

    public Optional<File> saveGameDialog() {
        final JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Optional.of(fileChooser.getSelectedFile());
        }
        return Optional.empty();
    }

    public Optional<File> loadGameDialog() {
        final JFileChooser fileChooser = new JFileChooser();
        int returnVal = fileChooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return Optional.of(fileChooser.getSelectedFile());
        }
        return Optional.empty();
    }

    public GameSetup gameSetupDialog(BotFactory botFactory, boolean exitOnCancel) {

        JLabel colorLabel = new JLabel("Your color");
        setBoldAndBorder(colorLabel);
        JRadioButton whiteRadioButton = new JRadioButton("White", true);
        JRadioButton blackRadioButton = new JRadioButton("Black", false);
        ButtonGroup colorButtonGroup = new ButtonGroup();
        colorButtonGroup.add(whiteRadioButton);
        colorButtonGroup.add(blackRadioButton);

        JLabel computerLabel = new JLabel("Computer level");
        setBoldAndBorder(computerLabel);
        JSlider levelSlider = getLevelSlider();
        JCheckBox openingsCheckBox = new JCheckBox("Use openings", true);
        JCheckBox timeoutCheckBox = new JCheckBox("Limit computation time to (seconds)", false);
        JSpinner timeoutSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        timeoutSpinner.setEnabled(timeoutCheckBox.isSelected());
        timeoutCheckBox.addActionListener(e -> timeoutSpinner.setEnabled(timeoutCheckBox.isSelected()));

        final JComponent[] inputs = new JComponent[] {
            colorLabel,
            whiteRadioButton,
            blackRadioButton,
            new JSeparator(),
            computerLabel,
            levelSlider,
            openingsCheckBox,
            timeoutCheckBox,
            timeoutSpinner
        };

        int result = JOptionPane
            .showConfirmDialog(this, inputs, "Game setup", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                logoIcon);
        if (result != JOptionPane.OK_OPTION) {
            if (exitOnCancel) {
                exit();
            } else {
                return null;
            }
        }

        Player whitePlayer;
        Player blackPlayer;

        Player human = new Human("Player");
        Player bot;
        Integer timeout = null;
        if (timeoutCheckBox.isSelected() && timeoutSpinner.getValue() instanceof Integer) {
            timeout = (Integer) timeoutSpinner.getValue();
        }
        if (levelSlider.getValue() == 0) {
            bot = botFactory.getRandomBot();
        } else {
            if (openingsCheckBox.isSelected()) {
                bot = botFactory.getExperiencedBot(levelSlider.getValue() - 1, timeout);
            } else {
                bot = botFactory.getTraditionalBot(levelSlider.getValue() - 1, timeout);
            }
        }

        if (whiteRadioButton.isSelected()) {
            whitePlayer = human;
            blackPlayer = bot;
        } else {
            whitePlayer = bot;
            blackPlayer = human;
        }

        return new GameSetup(whitePlayer, blackPlayer);
    }

    public Piece promotionDialog(ch.teemoo.bobby.models.Color color) {
        JLabel funLabel = new JLabel("Wow! Your pawn jus reached the end of the world!\n");
        JLabel promoteLabel = new JLabel("Promote pawn to");
        setBoldAndBorder(promoteLabel);
        JRadioButton queenRadioButton = new JRadioButton("♕ Queen", true);
        JRadioButton rookRadioButton = new JRadioButton("♖ Rook", false);
        JRadioButton bishopRadioButton = new JRadioButton("♗ Bishop", false);
        JRadioButton knightRadioButton = new JRadioButton("♘ Knight", false);
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(queenRadioButton);
        buttonGroup.add(rookRadioButton);
        buttonGroup.add(bishopRadioButton);
        buttonGroup.add(knightRadioButton);

        final JComponent[] inputs = new JComponent[] {
            funLabel,
            new JSeparator(),
            promoteLabel,
            queenRadioButton,
            rookRadioButton,
            bishopRadioButton,
            knightRadioButton
        };

        JOptionPane
            .showConfirmDialog(this, inputs, "Promotion", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                logoIcon);

        Piece piece;
        if (rookRadioButton.isSelected()) {
            piece = new Rook(color);
        } else if (bishopRadioButton.isSelected()) {
            piece = new Bishop(color);
        } else if (knightRadioButton.isSelected()) {
            piece = new Knight(color);
        } else {
            piece = new Queen(color);
        }
        return piece;
    }

    private void setBoldAndBorder(JLabel label) {
        label.setFont(new Font(label.getFont().getName(), Font.BOLD, label.getFont().getSize()));
        label.setBorder(new EmptyBorder(10, 0, 5, 0));
    }

    public void popupInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public void popupError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JSlider getLevelSlider() {
        JSlider levelSlider = new JSlider(JSlider.HORIZONTAL, 0, 3, 3);
        levelSlider.setMajorTickSpacing(1);
        levelSlider.setPaintTicks(true);
        levelSlider.setPaintLabels(true);
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("Stupid"));
        labelTable.put(1, new JLabel("Easy"));
        labelTable.put(2, new JLabel("Medium"));
        labelTable.put(3, new JLabel("Good"));
        levelSlider.setLabelTable(labelTable);
        return levelSlider;
    }

    private void setMenu() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem itemExit = new JMenuItem("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(actionEvent -> exit());

        JMenu gameMenu = new JMenu("Game");
        menuBar.add(gameMenu);

        this.itemNew = new JMenuItem("New");
        gameMenu.add(itemNew);
        gameMenu.addSeparator();
        this.itemSave = new JMenuItem("Save");
        gameMenu.add(itemSave);
        this.itemLoad = new JMenuItem("Load");
        gameMenu.add(itemLoad);
        gameMenu.addSeparator();

        this.itemSuggestMove = new JMenuItem("Suggest move");
        gameMenu.add(itemSuggestMove);
        gameMenu.addSeparator();
        this.itemUndoMove = new JMenuItem("Undo move");
        gameMenu.add(itemUndoMove);

        gameMenu.addSeparator();
        this.itemProposeDraw = new JMenuItem("Propose draw");
        gameMenu.add(itemProposeDraw);

        JMenu debugMenu = new JMenu("Debug");
        menuBar.add(debugMenu);

        this.itemPrintToConsole = new JMenuItem("Print to console");
        debugMenu.add(itemPrintToConsole);

        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);

        this.itemAbout = new JMenuItem("About");
        helpMenu.add(itemAbout);
        itemAbout.addActionListener(actionEvent -> showAboutDialog());
    }

    private void addFilesLabels(java.util.List<Component> components) {
        char a = 'a';
        char h = 'h';
        components.add(new SideLabel(""));
        Stream<Integer> intStream = IntStream.range(a, h + 1).boxed();
        intStream.forEach(i -> components.add(new SideLabel(Character.toString((char) (int)i))));
        components.add(new SideLabel(""));
    }

    private SideLabel getRankLabel(int i) {
        return new SideLabel(String.valueOf(i + 1));
    }

    private Background swapBackground(Background background) {
        if (background == Background.DARK) {
            return Background.LIGHT;
        } else {
            return Background.DARK;
        }
    }

    private Background getFirstSquareBackground(int i) {
        Background background;
        if (i % 2 != 0) {
            background = Background.DARK;
        } else {
            background = Background.LIGHT;
        }
        return background;
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Written with ♥ by Micaël Paquier\n"
                + " \n"
                + "Humble tribute to Robert James \"Bobby\" Fischer,\n"
                + "World Chess Champion\n"
                + " \n"
                + "\t“All that matters on the chessboard is good moves.”\n"
                + "― Bobby Fischer",
            "About Bobby",
            JOptionPane.INFORMATION_MESSAGE, logoIcon);
    }

    private void exit() {
        System.exit(0);
    }
}
