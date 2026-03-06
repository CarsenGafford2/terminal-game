package terminal;

import javax.swing.SwingUtilities;

/**
 * Entry point for the Linux Terminal Simulator.
 * Launches the Swing-based terminal window on the Event Dispatch Thread.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerminalWindow window = new TerminalWindow();
            window.setVisible(true);
        });
    }
}
