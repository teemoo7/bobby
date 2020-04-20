package ch.teemoo.bobby.gui;

import static ch.teemoo.bobby.models.Board.SIZE;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import ch.teemoo.bobby.helpers.BotFactory;
import ch.teemoo.bobby.models.GameSetup;
import ch.teemoo.bobby.models.Move;
import ch.teemoo.bobby.models.Position;
import ch.teemoo.bobby.models.pieces.Piece;
import ch.teemoo.bobby.models.players.Human;
import ch.teemoo.bobby.models.players.Player;
import ch.teemoo.bobby.models.players.RandomBot;
import ch.teemoo.bobby.models.players.TraditionalBot;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;


public class BoardView extends JFrame {
    private static final Border NO_BORDER = BorderFactory.createEmptyBorder();
    private static final Border GREEN_BORDER = BorderFactory.createLineBorder(java.awt.Color.green, 3, true);

    private final boolean visible;
    private final Container contentPane;
    private final Icon logoIcon;
    private Square[][] squares = new Square[SIZE][SIZE];

    private JMenuItem itemNew;
    private JMenuItem itemSave;
    private JMenuItem itemLoad;
    private JMenuItem itemPrintToConsole;
    private JMenuItem itemSuggestMove;
    private JMenuItem itemUndoMove;
    private JMenuItem itemAbout;

    public BoardView(String title) {
        this(title, true);
    }

    public BoardView(String title, boolean visible) {
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

    public void display(Piece[][] positions) {
        contentPane.removeAll();
        addFilesLabels();
        Background background;
        for (int i = positions.length - 1; i >= 0; i--) {
            contentPane.add(getRankLabel(i));
            background = getFirstSquareBackground(i);
            for (int j = 0; j < positions[i].length; j++) {
                Piece piece = positions[i][j];
                // Inverse coordinates (positions is a 2D array, reversed)
                Square square = new Square(piece, new Position(j, i), background);
                background = swapBackground(background);
                contentPane.add(square);
                squares[i][j] = square;
            }
            contentPane.add(getRankLabel(i));
        }
        addFilesLabels();
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

    public GameSetup gameSetupDialog(BotFactory botFactory) {

        JLabel colorLabel = new JLabel("Your color");
        setBold(colorLabel);
        JRadioButton whiteRadioButton = new JRadioButton("White", true);
        JRadioButton blackRadioButton = new JRadioButton("Black", false);
        ButtonGroup colorButtonGroup = new ButtonGroup();
        colorButtonGroup.add(whiteRadioButton);
        colorButtonGroup.add(blackRadioButton);

        JLabel computerLabel = new JLabel("Computer level");
        setBold(computerLabel);
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
            exit();
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

    private void setBold(JLabel colorLabel) {
        colorLabel.setFont(new Font(colorLabel.getFont().getName(), Font.BOLD, colorLabel.getFont().getSize()));
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

    private void addFilesLabels() {
        contentPane.add(new SideLabel(""));
        contentPane.add(new SideLabel("a"));
        contentPane.add(new SideLabel("b"));
        contentPane.add(new SideLabel("c"));
        contentPane.add(new SideLabel("d"));
        contentPane.add(new SideLabel("e"));
        contentPane.add(new SideLabel("f"));
        contentPane.add(new SideLabel("g"));
        contentPane.add(new SideLabel("h"));
        contentPane.add(new SideLabel(""));
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
                + "\t\"Chess is life.\" - Bobby Fischer",
            "About Bobby",
            JOptionPane.INFORMATION_MESSAGE, logoIcon);
    }

    private void exit() {
        System.exit(0);
    }
}
