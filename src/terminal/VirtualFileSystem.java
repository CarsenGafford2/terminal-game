package terminal;

import java.util.*;

public class VirtualFileSystem {

    private final VfsNode root;
    private VfsNode current;

    public VirtualFileSystem() {
        root = new VfsNode("/", true, null);
        root.parent = root;

        addDir(root, "bin");
        VfsNode etc  = addDir(root, "etc");
        VfsNode home = addDir(root, "home");
        addDir(root, "tmp");
        VfsNode usr  = addDir(root, "usr");
        VfsNode var  = addDir(root, "var");

        addDir(usr, "bin");
        addDir(usr, "lib");
        addDir(usr, "share");
        addDir(var, "log");

        VfsNode user = addDir(home, "user");
        addDir(user, "projects");
        addDir(user, "downloads");

        addFile(etc, "hostname", "server01\n");
        addFile(etc, "os-release",
                "NAME=\"Linux Terminal Simulator\"\n" +
                "VERSION=\"1.0\"\n" +
                "ID=lts\n" +
                "PRETTY_NAME=\"Linux Terminal Simulator 1.0\"\n");
        addFile(etc, "motd",
                "Welcome to Linux Terminal Simulator v1.0\n" +
                "Type 'help' to see available commands.\n");

        addFile(user, "README.txt",
                "Welcome to the Linux Terminal Simulator!\n\n" +
                "This is a simulated Linux terminal environment.\n" +
                "You can explore the filesystem and run common commands.\n\n" +
                "Type 'help' to see what commands are available.\n");
        addFile(user, "notes.txt",
                "Quick notes:\n" +
                "  - Use 'ls' to list files\n" +
                "  - Use 'cd <dir>' to change directories\n" +
                "  - Use 'cat <file>' to read a file\n" +
                "  - Use 'mkdir <dir>' to create a directory\n" +
                "  - Use 'touch <file>' to create an empty file\n");
        addFile(user, ".bashrc",
                "# ~/.bashrc\nexport PS1='\\u@\\h:\\w\\$ '\nexport EDITOR=nano\n");

        current = user;
    }

    public VfsNode getCurrentDir() {
        return current;
    }

    public String getCurrentPath() {
        if (current == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        VfsNode n = current;
        while (n != root) {
            parts.addFirst(n.name);
            n = n.parent;
        }
        return "/" + String.join("/", parts);
    }

    public String getNodePath(VfsNode VfsNode) {
        if (VfsNode == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        VfsNode n = VfsNode;
        while (n != root) {
            parts.addFirst(n.name);
            n = n.parent;
        }
        return "/" + String.join("/", parts);
    }

    public VfsNode resolve(String path) {
        if (path == null || path.isEmpty() || path.equals("~")) {
            return getHome();
        }
        if (path.startsWith("~/")) {
            VfsNode home = getHome();
            if (home == null) return null;
            return resolveFrom(home, path.substring(2));
        }
        if (path.startsWith("/")) {
            return resolveFrom(root, path.substring(1));
        }
        return resolveFrom(current, path);
    }

    public boolean changeDirectory(String path) {
        VfsNode target = resolve(path);
        if (target != null && target.isDirectory) {
            current = target;
            return true;
        }
        return false;
    }

    public boolean createDirectory(String name) {
        if (current.children.containsKey(name)) return false;
        addDir(current, name);
        return true;
    }

    public boolean createFile(String name) {
        if (current.children.containsKey(name)) {
            VfsNode n = current.children.get(name);
            return !n.isDirectory;
        }
        addFile(current, name, "");
        return true;
    }

    public boolean writeFile(String name, String content) {
        VfsNode n = resolve(name);
        if (n == null) {
            addFile(current, name, content);
            return true;
        }
        if (n.isDirectory) return false;
        n.content = content;
        return true;
    }

    public boolean remove(String name, boolean recursive) {
        VfsNode n = current.children.get(name);
        if (n == null) return false;
        if (n.isDirectory && !recursive) return false;
        current.children.remove(name);
        return true;
    }

    public List<String> listDirectory() {
        return new ArrayList<>(current.children.keySet());
    }

    public String readFile(String nameOrPath) {
        VfsNode n = resolve(nameOrPath);
        if (n != null && !n.isDirectory) return n.content;
        return null;
    }

    private static VfsNode addDir(VfsNode parent, String name) {
        VfsNode dir = new VfsNode(name, true, parent);
        parent.children.put(name, dir);
        return dir;
    }

    private static VfsNode addFile(VfsNode parent, String name, String content) {
        VfsNode file = new VfsNode(name, false, parent);
        file.content = content;
        parent.children.put(name, file);
        return file;
    }

    private VfsNode getHome() {
        VfsNode h = root.children.get("home");
        if (h == null) return root;
        VfsNode u = h.children.get("user");
        return u != null ? u : h;
    }

    private VfsNode resolveFrom(VfsNode start, String path) {
        VfsNode n = start;
        for (String part : path.split("/")) {
            if (part.isEmpty() || part.equals(".")) continue;
            if (part.equals("..")) {
                n = n.parent;
            } else {
                if (!n.isDirectory || !n.children.containsKey(part)) return null;
                n = n.children.get(part);
            }
        }
        return n;
    }
}
