package terminal;

import terminal.command.Command;
import terminal.command.LsCommand;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandProcessor {

    public static final String SIGNAL_CLEAR = "\u0000CLEAR";
    public static final String SIGNAL_EXIT  = "\u0000EXIT";

    private final VirtualFileSystem    fs;
    private final Map<String, Command> commands;
    private final CommandHistory       history;

    public CommandProcessor() {
        fs       = new VirtualFileSystem();
        commands = new LinkedHashMap<>();
        history  = new CommandHistory();
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
        return PromptFormatter.format(fs.getCurrentPath());
    }

    public String historyUp()           { return history.navigateUp();   }
    public String historyDown()         { return history.navigateDown(); }
    public void   resetHistoryIndex()   { history.resetIndex();          }

    public String process(String input) {
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return "";

        history.add(trimmed);

        List<String> tokens = InputTokenizer.tokenise(trimmed);
        if (tokens.isEmpty()) return "";

        String   cmd  = tokens.get(0);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        switch (cmd) {
            case "clear":   return SIGNAL_CLEAR;
            case "exit":
            case "quit":    return SIGNAL_EXIT;
            case "history": return history.formatted();
            case "help":    return buildHelp();
        }

        Command command = commands.get(cmd);
        if (command != null) {
            return command.execute(args);
        }

        return cmd + ": command not found";
    }

    private String buildHelp() {
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
}
