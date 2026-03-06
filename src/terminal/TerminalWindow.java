package terminal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class TerminalWindow extends JFrame {

    private final JTextPane        outputPane;
    private final StyledDocument   doc;
    private final JLabel           promptLabel;
    private final JTextField       inputField;
    private final CommandProcessor processor;
    private final TerminalRenderer renderer;

    public CommandProcessor getCommandProcessor() {
        return processor;
    }

    public TerminalWindow() {
        processor = new CommandProcessor();

        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(TerminalTheme.COL_BG);
        outputPane.setForeground(TerminalTheme.COL_TEXT);
        outputPane.setFont(TerminalTheme.FONT);
        outputPane.setMargin(new Insets(TerminalTheme.PAD, TerminalTheme.PAD, TerminalTheme.PAD, TerminalTheme.PAD));
        doc = outputPane.getStyledDocument();

        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(TerminalTheme.COL_BG);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBackground(TerminalTheme.COL_BG);

        promptLabel = new JLabel(processor.getPrompt());
        promptLabel.setFont(TerminalTheme.FONT);
        promptLabel.setForeground(TerminalTheme.COL_PROMPT);
        promptLabel.setBorder(new EmptyBorder(2, TerminalTheme.PAD, 2, 0));
        promptLabel.setOpaque(true);
        promptLabel.setBackground(TerminalTheme.COL_BG);

        inputField = new JTextField();
        inputField.setFont(TerminalTheme.FONT);
        inputField.setBackground(TerminalTheme.COL_BG);
        inputField.setForeground(TerminalTheme.COL_INPUT);
        inputField.setCaretColor(Color.WHITE);
        inputField.setBorder(new EmptyBorder(2, 4, 2, TerminalTheme.PAD));
        inputField.setOpaque(true);

        JPanel inputRow = new JPanel(new BorderLayout());
        inputRow.setBackground(TerminalTheme.COL_BG);
        inputRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TerminalTheme.COL_BORDER));
        inputRow.add(promptLabel, BorderLayout.WEST);
        inputRow.add(inputField,  BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(TerminalTheme.COL_BG);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputRow,   BorderLayout.SOUTH);

        renderer = new TerminalRenderer(outputPane, doc, promptLabel, processor);

        setTitle("Terminal — user@server01");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setSize(900, 600);
        setMinimumSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

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

        renderer.printWelcome();
    }

    private void handleEnter() {
        String input = inputField.getText();
        inputField.setText("");
        processor.resetHistoryIndex();

        renderer.appendPrompt(processor.getPrompt());
        renderer.appendText(input + "\n", TerminalTheme.COL_TEXT);

        String result = processor.process(input);

        if (CommandProcessor.SIGNAL_CLEAR.equals(result)) {
            try { doc.remove(0, doc.getLength()); } catch (BadLocationException ignored) {}
            renderer.updatePromptLabel();
            renderer.scrollToBottom();
            return;
        }
        if (CommandProcessor.SIGNAL_EXIT.equals(result)) {
            dispose();
            System.exit(0);
        }

        if (result != null && !result.isEmpty()) {
            for (String line : result.split("\n", -1)) {
                boolean isError = line.startsWith("bash:") || line.endsWith(": command not found")
                        || line.contains(": No such file") || line.contains(": Not a directory")
                        || line.contains(": Is a directory") || line.contains("cannot")
                        || line.contains("failed to") || line.contains("missing");
                renderer.appendText(line + "\n", isError ? TerminalTheme.COL_ERROR : TerminalTheme.COL_TEXT);
            }
        }

        renderer.updatePromptLabel();
        renderer.scrollToBottom();
    }
}

