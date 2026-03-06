package terminal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A Swing-based window that looks and behaves like a Linux terminal.
 *
 * Layout:
 *   ┌──────────────────────────────────────────┐
 *   │  [title bar] Terminal — user@server01    │
 *   ├──────────────────────────────────────────┤
 *   │                                          │
 *   │  [scrollable output pane]                │
 *   │                                          │
 *   ├──────────────────────────────────────────┤
 *   │  [prompt label] [input text field]       │
 *   └──────────────────────────────────────────┘
 */
public class TerminalWindow extends JFrame {

    // ------------------------------------------------------------------
    // Colour palette (dark terminal theme)
    // ------------------------------------------------------------------
    private static final Color COL_BG       = new Color(0x1a, 0x1a, 0x2e); // deep navy
    private static final Color COL_TEXT     = new Color(0xe0, 0xe0, 0xe0); // light grey
    private static final Color COL_PROMPT   = new Color(0x79, 0xb8, 0xff); // blue
    private static final Color COL_PATH     = new Color(0xb3, 0xf0, 0xa5); // green
    private static final Color COL_DOLLAR   = new Color(0xff, 0xff, 0xff); // white
    private static final Color COL_ERROR    = new Color(0xff, 0x6b, 0x6b); // red
    private static final Color COL_WELCOME  = new Color(0xff, 0xd7, 0x00); // gold
    private static final Color COL_INPUT    = new Color(0xe0, 0xe0, 0xe0); // same as text
    private static final Color COL_BORDER   = new Color(0x30, 0x30, 0x55); // subtle border

    private static final Font  FONT         = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    private static final int   PAD          = 8;

    // ------------------------------------------------------------------
    // UI components
    // ------------------------------------------------------------------
    private final JTextPane         outputPane;
    private final StyledDocument    doc;
    private final JLabel            promptLabel;
    private final JTextField        inputField;
    private final CommandProcessor  processor;

    // ------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------
    public TerminalWindow() {
        processor = new CommandProcessor();

        // ----- output pane -----
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(COL_BG);
        outputPane.setForeground(COL_TEXT);
        outputPane.setFont(FONT);
        outputPane.setMargin(new Insets(PAD, PAD, PAD, PAD));
        doc = outputPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(COL_BG);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(COL_BG);

        // ----- input row -----
        promptLabel = new JLabel(processor.getPrompt());
        promptLabel.setFont(FONT);
        promptLabel.setForeground(COL_PROMPT);
        promptLabel.setBorder(new EmptyBorder(2, PAD, 2, 0));
        promptLabel.setOpaque(true);
        promptLabel.setBackground(COL_BG);

        inputField = new JTextField();
        inputField.setFont(FONT);
        inputField.setBackground(COL_BG);
        inputField.setForeground(COL_INPUT);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(new EmptyBorder(2, 4, 2, PAD));
        inputField.setOpaque(true);

        JPanel inputRow = new JPanel(new BorderLayout());
        inputRow.setBackground(COL_BG);
        inputRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, COL_BORDER));
        inputRow.add(promptLabel, BorderLayout.WEST);
        inputRow.add(inputField,  BorderLayout.CENTER);

        // ----- main panel -----
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(COL_BG);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputRow,   BorderLayout.SOUTH);

        // ----- frame setup -----
        setTitle("Terminal — user@server01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setSize(900, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        // ----- event listeners -----
        inputField.addActionListener(e -> handleEnter());

        inputField.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    inputField.setText(processor.historyUp());
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    inputField.setText(processor.historyDown());
                    e.consume();
                }
            }
        });

        // Click anywhere on the output → focus input
        outputPane.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                inputField.requestFocusInWindow();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override public void windowOpened(WindowEvent e) {
                inputField.requestFocusInWindow();
            }
        });

        // ----- initial content -----
        printWelcome();
    }

    // ------------------------------------------------------------------
    // Input handling
    // ------------------------------------------------------------------

    private void handleEnter() {
        String input = inputField.getText();
        inputField.setText("");
        processor.resetHistoryIndex();

        // Echo the command with a coloured prompt in the output pane
        appendPrompt(processor.getPrompt());
        appendText(input + "\n", COL_TEXT);

        String result = processor.process(input);

        if (CommandProcessor.SIGNAL_CLEAR.equals(result)) {
            try { doc.remove(0, doc.getLength()); } catch (BadLocationException ignored) {}
            updatePromptLabel();
            scrollToBottom();
            return;
        }
        if (CommandProcessor.SIGNAL_EXIT.equals(result)) {
            dispose();
            System.exit(0);
        }

        if (result != null && !result.isEmpty()) {
            // Print error lines in red, rest in normal text colour
            for (String line : result.split("\n", -1)) {
                boolean isError = line.startsWith("bash:") || line.endsWith(": command not found")
                        || line.contains(": No such file") || line.contains(": Not a directory")
                        || line.contains(": Is a directory") || line.contains("cannot")
                        || line.contains("failed to") || line.contains("missing");
                appendText(line + "\n", isError ? COL_ERROR : COL_TEXT);
            }
        }

        updatePromptLabel();
        scrollToBottom();
    }

    // ------------------------------------------------------------------
    // Styled text helpers
    // ------------------------------------------------------------------

    /**
     * Append a coloured prompt.  The prompt has the form "user@host:path$ ".
     * We split it into segments and colour each one.
     */
    private void appendPrompt(String prompt) {
        // user@host   :   path   $
        int atIdx    = prompt.indexOf('@');
        int colonIdx = prompt.indexOf(':');
        int dollarIdx = prompt.lastIndexOf('$');

        if (atIdx < 0 || colonIdx < 0 || dollarIdx < 0) {
            appendText(prompt, COL_PROMPT);
            return;
        }
        // "user@host"
        appendText(prompt.substring(0, colonIdx), COL_PROMPT);
        // ":"
        appendText(":", COL_TEXT);
        // path
        appendText(prompt.substring(colonIdx + 1, dollarIdx), COL_PATH);
        // "$ "
        appendText(prompt.substring(dollarIdx), COL_DOLLAR);
    }

    private void appendText(String text, Color color) {
        Style style = outputPane.addStyle(null, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBackground(style, COL_BG);
        StyleConstants.setFontFamily(style, Font.MONOSPACED);
        StyleConstants.setFontSize(style, 14);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() ->
                outputPane.setCaretPosition(doc.getLength()));
    }

    private void updatePromptLabel() {
        promptLabel.setText(processor.getPrompt());
    }

    // ------------------------------------------------------------------
    // Welcome banner
    // ------------------------------------------------------------------

    private void printWelcome() {
        appendText(
            " _     _                  _____                      _             _\n" +
            "| |   (_)_ __  _   ___  |_   _|__ _ __ _ __ ___ (_)_ __   __ _| |\n" +
            "| |   | | '_ \\| | | \\ \\/ / | |/ _ \\ '__| '_ ` _ \\| | '_ \\ / _` | |\n" +
            "| |___| | | | | |_| |>  <  | |  __/ |  | | | | | | | | | | (_| | |\n" +
            "|_____|_|_| |_|\\__,_/_/\\_\\ |_|\\___|_|  |_| |_| |_|_|_| |_|\\__,_|_|\n" +
            "  Simulator\n",
            COL_WELCOME);
        appendText("─".repeat(72) + "\n", COL_BORDER);
        appendText("  Linux Terminal Simulator  |  hostname: server01  |  user: user\n", COL_PROMPT);
        appendText("  Type 'help' to list all available commands.\n", COL_TEXT);
        appendText("  Use ↑/↓ arrow keys to navigate command history.\n", COL_TEXT);
        appendText("─".repeat(72) + "\n\n", COL_BORDER);
        updatePromptLabel();
    }
}
