package terminal;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.Color;
import java.awt.Font;

public class TerminalRenderer {

    private final JTextPane        outputPane;
    private final StyledDocument   doc;
    private final JLabel           promptLabel;
    private final CommandProcessor processor;

    public TerminalRenderer(JTextPane outputPane, StyledDocument doc,
                            JLabel promptLabel, CommandProcessor processor) {
        this.outputPane  = outputPane;
        this.doc         = doc;
        this.promptLabel = promptLabel;
        this.processor   = processor;
    }

    public void appendPrompt(String prompt) {
        int atIdx     = prompt.indexOf('@');
        int colonIdx  = prompt.indexOf(':');
        int dollarIdx = prompt.lastIndexOf('$');

        if (atIdx < 0 || colonIdx < 0 || dollarIdx < 0) {
            appendText(prompt, TerminalTheme.COL_PROMPT);
            return;
        }
        appendText(prompt.substring(0, colonIdx),              TerminalTheme.COL_PROMPT);
        appendText(":",                                         TerminalTheme.COL_TEXT);
        appendText(prompt.substring(colonIdx + 1, dollarIdx),  TerminalTheme.COL_PATH);
        appendText(prompt.substring(dollarIdx),                TerminalTheme.COL_DOLLAR);
    }

    public void appendText(String text, Color color) {
        Style style = outputPane.addStyle(null, null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBackground(style, TerminalTheme.COL_BG);
        StyleConstants.setFontFamily(style, Font.MONOSPACED);
        StyleConstants.setFontSize(style, 14);
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ignored) {}
    }

    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> outputPane.setCaretPosition(doc.getLength()));
    }

    public void updatePromptLabel() {
        promptLabel.setText(processor.getPrompt());
    }

    public void printWelcome() {
        appendText("─".repeat(72) + "\n",                                              TerminalTheme.COL_BORDER);
        appendText("  Linux Terminal Simulator  |  hostname: server01  |  user: user\n", TerminalTheme.COL_PROMPT);
        appendText("  Type 'help' for available commands. Use \u2191/\u2193 for history.\n", TerminalTheme.COL_TEXT);
        appendText("─".repeat(72) + "\n\n",                                            TerminalTheme.COL_BORDER);
        updatePromptLabel();
    }
}
