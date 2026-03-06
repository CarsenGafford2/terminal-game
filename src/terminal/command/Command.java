package terminal.command;

import terminal.VirtualFileSystem;

/**
 * Abstract base class for all terminal commands.
 *
 * To add a new command:
 *  1. Create a class in this package that extends Command.
 *  2. Pass the shared VirtualFileSystem to super() in the constructor.
 *  3. Implement getName(), getDescription(), and execute(String[] args).
 *  4. Register your command in CommandProcessor#registerCommands(), or
 *     call commandProcessor.registerCommand(new YourCommand(fs)) from Main.
 *
 * Example:
 * <pre>
 *   public class PwdCommand extends Command {
 *       public PwdCommand(VirtualFileSystem fs) { super(fs); }
 *
 *       {@literal @}Override public String getName()        { return "pwd"; }
 *       {@literal @}Override public String getDescription() { return "print working directory"; }
 *
 *       {@literal @}Override
 *       public String execute(String[] args) {
 *           return fs.getCurrentPath();
 *       }
 *   }
 * </pre>
 */
public abstract class Command {

    /** Shared virtual filesystem — available to every command. */
    protected final VirtualFileSystem fs;

    protected Command(VirtualFileSystem fs) {
        this.fs = fs;
    }

    /** The name used to invoke this command, e.g. {@code "ls"}. */
    public abstract String getName();

    /** One-line description shown in the help listing. */
    public abstract String getDescription();

    /**
     * Execute the command.
     *
     * @param args  the arguments that followed the command name (may be empty)
     * @return      the text to print in the terminal, or {@code ""} for no output
     */
    public abstract String execute(String[] args);
}
