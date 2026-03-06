package terminal;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerminalWindow window = new TerminalWindow();
            window.setVisible(true);
        });
    }
}
