package terminal;

import java.util.*;


public class VirtualFileSystem {

    public static class Node {
        public final String name;
        public final boolean isDirectory;
        public String content;
        public final Map<String, Node> children;
        public Node parent;

        public Node(String name, boolean isDirectory, Node parent) {
            this.name = name;
            this.isDirectory = isDirectory;
            this.parent = parent;
            this.children = isDirectory ? new LinkedHashMap<>() : null;
            this.content  = isDirectory ? null : "";
        }
    }

    private final Node root;
    private Node current;

    public VirtualFileSystem() {
        root = new Node("/", true, null);
        root.parent = root;

        addDir(root, "bin");
        Node etc  = addDir(root, "etc");
        Node home = addDir(root, "home");
        addDir(root, "tmp");
        Node usr  = addDir(root, "usr");
        Node var  = addDir(root, "var");

        addDir(usr, "bin");
        addDir(usr, "lib");
        addDir(usr, "share");
        addDir(var, "log");

        Node user = addDir(home, "user");
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

    public Node getCurrentDir() {
        return current;
    }

    public String getCurrentPath() {
        if (current == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        Node n = current;
        while (n != root) {
            parts.addFirst(n.name);
            n = n.parent;
        }
        return "/" + String.join("/", parts);
    }

    public String getNodePath(Node node) {
        if (node == root) return "/";
        Deque<String> parts = new ArrayDeque<>();
        Node n = node;
        while (n != root) {
            parts.addFirst(n.name);
            n = n.parent;
        }
        return "/" + String.join("/", parts);
    }

    public Node resolve(String path) {
        if (path == null || path.isEmpty() || path.equals("~")) {
            return getHome();
        }
        if (path.startsWith("~/")) {
            Node home = getHome();
            if (home == null) return null;
            return resolveFrom(home, path.substring(2));
        }
        if (path.startsWith("/")) {
            return resolveFrom(root, path.substring(1));
        }
        return resolveFrom(current, path);
    }

    public boolean changeDirectory(String path) {
        Node target = resolve(path);
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
            Node n = current.children.get(name);
            return !n.isDirectory;
        }
        addFile(current, name, "");
        return true;
    }

    public boolean writeFile(String name, String content) {
        Node n = resolve(name);
        if (n == null) {
            addFile(current, name, content);
            return true;
        }
        if (n.isDirectory) return false;
        n.content = content;
        return true;
    }

    public boolean remove(String name, boolean recursive) {
        Node n = current.children.get(name);
        if (n == null) return false;
        if (n.isDirectory && !recursive) return false;
        current.children.remove(name);
        return true;
    }

    public List<String> listDirectory() {
        return new ArrayList<>(current.children.keySet());
    }

    public String readFile(String nameOrPath) {
        Node n = resolve(nameOrPath);
        if (n != null && !n.isDirectory) return n.content;
        return null;
    }

    private static Node addDir(Node parent, String name) {
        Node dir = new Node(name, true, parent);
        parent.children.put(name, dir);
        return dir;
    }

    private static Node addFile(Node parent, String name, String content) {
        Node file = new Node(name, false, parent);
        file.content = content;
        parent.children.put(name, file);
        return file;
    }

    private Node getHome() {
        Node h = root.children.get("home");
        if (h == null) return root;
        Node u = h.children.get("user");
        return u != null ? u : h;
    }

    private Node resolveFrom(Node start, String path) {
        Node n = start;
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
