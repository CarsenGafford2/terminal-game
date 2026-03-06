package terminal;

import terminal.command.Command;
import terminal.command.LsCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandProcessor {

    public static final String SIGNAL_CLEAR = "\u0000CLEAR";
    public static final String SIGNAL_EXIT  = "\u0000EXIT";

    private static final String USERNAME  = "user";
    private static final String HOSTNAME  = "server01";
    private static final String HOME_PATH = "/home/user";

    private final VirtualFileSystem    fs;
    private final Map<String, Command> commands;
    private final List<String>         history;
    private int historyIndex = -1;

    public CommandProcessor() {
        fs       = new VirtualFileSystem();
        commands = new LinkedHashMap<>();
        history  = new ArrayList<>();
        registerCommands();
    }

    private void registerCommands() {
        register(new LsCommand(fs));
    }

    private void register(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public void registerCommand(Command cmd) {
        commands.put(cmd.getName(), cmd);
    }

    public VirtualFileSystem getFileSystem() {
        return fs;
    }

    public String getPrompt() {
        String path = fs.getCurrentPath();
        if (path.startsWith(HOME_PATH)) {
            path = "~" + path.substring(HOME_PATH.length());
        }
        return USERNAME + "@" + HOSTNAME + ":" + path + "$ ";
    }

    public String historyUp() {
        if (history.isEmpty()) return "";
        if (historyIndex < 0) historyIndex = history.size();
        if (historyIndex > 0) historyIndex--;
        return history.get(historyIndex);
    }

    public String historyDown() {
        if (history.isEmpty()) return "";
        if (historyIndex < 0) return "";
        historyIndex++;
        if (historyIndex >= history.size()) {
            historyIndex = history.size();
            return "";
        }
        return history.get(historyIndex);
    }

    public void resetHistoryIndex() {
        historyIndex = -1;
    }

    public String process(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";

        history.add(trimmed);
        historyIndex = -1;

        List<String> tokens = tokenise(trimmed);
        if (tokens.isEmpty()) return "";

        String   cmd  = tokens.get(0);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        switch (cmd) {
            case "clear":  return SIGNAL_CLEAR;
            case "exit":
            case "quit":   return SIGNAL_EXIT;
            case "history": return builtinHistory();
            case "help":   return builtinHelp();
        }

        Command command = commands.get(cmd);
        if (command != null) {
            return command.execute(args);
        }

        return cmd + ": command not found";
    }

    private String builtinHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            sb.append(String.format("  %3d  %s%n", i + 1, history.get(i)));
        }
        return sb.toString().stripTrailing();
    }

    private String builtinHelp() {
        StringBuilder sb = new StringBuilder();
        sb.append("Built-in commands:\n");
        sb.append("  clear          clear the screen\n");
        sb.append("  history        show command history\n");
        sb.append("  help           show this help\n");
        sb.append("  exit / quit    close the terminal\n");
        if (!commands.isEmpty()) {
            sb.append("\nRegistered commands:\n");
            commands.values().forEach(c ->
                    sb.append(String.format("  %-14s %s%n", c.getName(), c.getDescription())));
        }
        sb.append("\nTip: use UP/DOWN arrow keys to navigate history.");
        return sb.toString();
    }

    private static List<String> tokenise(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) tokens.add(current.toString());
        return tokens;
    }
}
