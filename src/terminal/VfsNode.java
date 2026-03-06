package terminal;

import java.util.LinkedHashMap;
import java.util.Map;

public class VfsNode {

    public final String              name;
    public final boolean             isDirectory;
    public String                    content;
    public final Map<String, VfsNode> children;
    public VfsNode                   parent;

    public VfsNode(String name, boolean isDirectory, VfsNode parent) {
        this.name        = name;
        this.isDirectory = isDirectory;
        this.parent      = parent;
        this.children    = isDirectory ? new LinkedHashMap<>() : null;
        this.content     = isDirectory ? null : "";
    }
}
