package terminal.command;

import terminal.VfsNode;
import terminal.VirtualFileSystem;

import java.util.Collections;
import java.util.List;

public class LsCommand extends Command {

    private static final String OWNER = "user";

    public LsCommand(VirtualFileSystem fs) {
        super(fs);
    }

    @Override
    public String getName() { return "ls"; }

    @Override
    public String getDescription() { return "list directory contents  (flags: -l, -a)"; }

    @Override
    public String execute(String[] args) {
        boolean longFmt = false;
        boolean showAll = false;

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (arg.contains("l")) longFmt = true;
                if (arg.contains("a")) showAll = true;
            }
        }

        List<String> entries = fs.listDirectory();
        if (!showAll) entries.removeIf(e -> e.startsWith("."));
        Collections.sort(entries);

        if (entries.isEmpty()) return "";

        if (longFmt) {
            return buildLongListing(entries);
        }

        return buildShortListing(entries);
    }

    private String buildLongListing(List<String> entries) {
        StringBuilder sb = new StringBuilder("total " + (entries.size() * 4) + "\n");
        for (String name : entries) {
            VfsNode node = fs.resolve(name);
            if (node == null) continue;

            String typeChar  = node.isDirectory ? "d" : "-";
            String perms     = node.isDirectory ? "rwxr-xr-x" : "rw-r--r--";
            int    size      = node.isDirectory ? 4096
                                               : (node.content != null ? node.content.length() : 0);

            sb.append(String.format("%s%s  1 %-6s %-6s %6d Jan 01 00:00 %s\n",
                    typeChar, perms, OWNER, OWNER, size, name));
        }
        return sb.toString().trim();
    }

    private String buildShortListing(List<String> entries) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append("  ");
            sb.append(entries.get(i));
        }
        return sb.toString();
    }
}
