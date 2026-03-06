package terminal;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CommandProcessor {

    public static final String SIGNAL_CLEAR = "\u0000CLEAR";
    public static final String SIGNAL_EXIT  = "\u0000EXIT";

    private static final String USERNAME = "user";
    private static final String HOSTNAME = "server01";
    private static final String HOME_PATH = "/home/user";

    private final VirtualFileSystem fs;
    private final List<String>      history;
    private int historyIndex = -1;

    public CommandProcessor() {
        fs      = new VirtualFileSystem();
        history = new ArrayList<>();
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

        String cmd  = tokens.get(0);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        switch (cmd) {
            case "ls":       return cmdLs(args);
            case "cd":       return cmdCd(args);
            case "pwd":      return cmdPwd();
            case "tree":     return cmdTree(args);

            case "cat":      return cmdCat(args);
            case "touch":    return cmdTouch(args);
            case "mkdir":    return cmdMkdir(args);
            case "rmdir":    return cmdRmdir(args);
            case "rm":       return cmdRm(args);
            case "cp":       return cmdCp(args);
            case "mv":       return cmdMv(args);
            case "echo":     return cmdEcho(args);
            case "grep":     return cmdGrep(args);

            case "whoami":   return USERNAME;
            case "hostname": return HOSTNAME;
            case "date":     return LocalDateTime.now()
                                     .format(DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy"));
            case "uname":    return cmdUname(args);
            case "uptime":   return "up 3 days, 14:22, 1 user, load average: 0.08, 0.05, 0.01";
            case "ps":       return cmdPs(args);
            case "top":      return cmdTop();
            case "df":       return cmdDf(args);
            case "du":       return cmdDu(args);
            case "free":     return cmdFree(args);
            case "env":      return cmdEnv();
            case "id":       return "uid=1000(" + USERNAME + ") gid=1000(" + USERNAME + ") groups=1000(" + USERNAME + "),4(adm),27(sudo)";

            case "ping":     return cmdPing(args);
            case "ifconfig": return cmdIfconfig();
            case "ip":       return cmdIp(args);
            case "curl":     return cmdCurl(args);
            case "wget":     return cmdWget(args);
            case "ssh":      return cmdSsh(args);

            case "history":  return cmdHistory();
            case "clear":    return SIGNAL_CLEAR;
            case "exit":
            case "quit":     return SIGNAL_EXIT;
            case "help":     return cmdHelp();
            case "man":      return cmdMan(args);
            case "sudo":     return cmdSudo(args);
            case "su":       return "su: Authentication failure";
            case "which":    return cmdWhich(args);
            case "alias":    return "";
            case "export":   return "";
            case "source":   return "";
            case ".":        return "";
            case "sleep":    return "";

            default:
                return cmd + ": command not found";
        }
    }

    private String cmdLs(String[] args) {
        boolean longFmt  = false;
        boolean showAll  = false;

        for (String a : args) {
            if (a.startsWith("-")) {
                if (a.contains("l")) longFmt = true;
                if (a.contains("a")) showAll = true;
            }
        }

        List<String> entries = fs.listDirectory();
        if (!showAll) entries.removeIf(e -> e.startsWith("."));
        Collections.sort(entries);

        if (entries.isEmpty()) return "";

        if (longFmt) {
            StringBuilder sb = new StringBuilder("total " + (entries.size() * 4) + "\n");
            for (String name : entries) {
                VirtualFileSystem.Node n = fs.resolve(name);
                if (n == null) continue;
                String type  = n.isDirectory ? "d" : "-";
                String perms = n.isDirectory ? "rwxr-xr-x" : "rw-r--r--";
                int    size  = n.isDirectory ? 4096 : (n.content != null ? n.content.length() : 0);
                sb.append(String.format("%s%s  1 %-6s %-6s %6d Jan 01 00:00 %s\n",
                        type, perms, USERNAME, USERNAME, size, name));
            }
            return sb.toString().trim();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            if (i > 0) sb.append("  ");
            sb.append(entries.get(i));
        }
        return sb.toString();
    }

    private String cmdCd(String[] args) {
        String path = (args.length == 0) ? "~" : args[0];
        if (!fs.changeDirectory(path))
            return "bash: cd: " + path + ": No such file or directory";
        return "";
    }

    private String cmdPwd() {
        return fs.getCurrentPath();
    }

    private String cmdTree(String[] args) {
        return treeStr(fs.getCurrentDir(), "", true);
    }

    private String treeStr(VirtualFileSystem.Node node, String prefix, boolean isRoot) {
        StringBuilder sb = new StringBuilder();
        if (isRoot) {
            sb.append(node.name).append("\n");
        }
        if (!node.isDirectory) return sb.toString();
        List<String> keys = new ArrayList<>(node.children.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            boolean last = (i == keys.size() - 1);
            VirtualFileSystem.Node child = node.children.get(keys.get(i));
            sb.append(prefix).append(last ? "└── " : "├── ").append(child.name).append("\n");
            if (child.isDirectory) {
                sb.append(treeStr(child, prefix + (last ? "    " : "│   "), false));
            }
        }
        return sb.toString();
    }

    private String cmdCat(String[] args) {
        if (args.length == 0) return "cat: missing operand";
        StringBuilder sb = new StringBuilder();
        for (String a : args) {
            String c = fs.readFile(a);
            if (c == null) sb.append("cat: ").append(a).append(": No such file or directory\n");
            else           sb.append(c);
        }
        return sb.toString().stripTrailing();
    }

    private String cmdTouch(String[] args) {
        if (args.length == 0) return "touch: missing file operand";
        for (String a : args) fs.createFile(a);
        return "";
    }

    private String cmdMkdir(String[] args) {
        if (args.length == 0) return "mkdir: missing operand";
        StringBuilder sb = new StringBuilder();
        for (String a : args) {
            if (!fs.createDirectory(a))
                sb.append("mkdir: cannot create directory '").append(a).append("': File exists\n");
        }
        return sb.toString().stripTrailing();
    }

    private String cmdRmdir(String[] args) {
        if (args.length == 0) return "rmdir: missing operand";
        StringBuilder sb = new StringBuilder();
        for (String a : args) {
            VirtualFileSystem.Node n = fs.resolve(a);
            if (n == null)            sb.append("rmdir: failed to remove '").append(a).append("': No such file or directory\n");
            else if (!n.isDirectory)  sb.append("rmdir: failed to remove '").append(a).append("': Not a directory\n");
            else if (!n.children.isEmpty()) sb.append("rmdir: failed to remove '").append(a).append("': Directory not empty\n");
            else                      fs.remove(a, false);
        }
        return sb.toString().stripTrailing();
    }

    private String cmdRm(String[] args) {
        if (args.length == 0) return "rm: missing operand";
        boolean recursive = false;
        boolean force     = false;
        List<String> targets = new ArrayList<>();
        for (String a : args) {
            if (a.startsWith("-")) {
                if (a.contains("r") || a.contains("R")) recursive = true;
                if (a.contains("f")) force = true;
            } else {
                targets.add(a);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String t : targets) {
            VirtualFileSystem.Node n = fs.resolve(t);
            if (n == null) {
                if (!force) sb.append("rm: cannot remove '").append(t).append("': No such file or directory\n");
            } else if (n.isDirectory && !recursive) {
                sb.append("rm: cannot remove '").append(t).append("': Is a directory\n");
            } else {
                fs.remove(t, recursive);
            }
        }
        return sb.toString().stripTrailing();
    }

    private String cmdCp(String[] args) {
        if (args.length < 2) return "cp: missing destination file operand after '" + (args.length > 0 ? args[0] : "") + "'";
        String content = fs.readFile(args[0]);
        if (content == null) return "cp: cannot stat '" + args[0] + "': No such file or directory";
        fs.writeFile(args[1], content);
        return "";
    }

    private String cmdMv(String[] args) {
        if (args.length < 2) return "mv: missing destination file operand after '" + (args.length > 0 ? args[0] : "") + "'";
        String content = fs.readFile(args[0]);
        if (content == null) return "mv: cannot stat '" + args[0] + "': No such file or directory";
        fs.writeFile(args[1], content);
        fs.remove(args[0], false);
        return "";
    }

    private String cmdEcho(String[] args) {
        int start = 0;
        if (args.length > 0 && args[0].equals("-n")) start = 1;
        return String.join(" ", Arrays.copyOfRange(args, start, args.length));
    }

    private String cmdGrep(String[] args) {
        if (args.length < 2) return "Usage: grep PATTERN FILE";
        String pattern = args[0];
        String content = fs.readFile(args[1]);
        if (content == null) return "grep: " + args[1] + ": No such file or directory";
        StringBuilder sb = new StringBuilder();
        for (String line : content.split("\n")) {
            if (line.contains(pattern)) sb.append(line).append("\n");
        }
        return sb.toString().stripTrailing();
    }

    private String cmdUname(String[] args) {
        boolean all = args.length > 0 && args[0].contains("a");
        if (all) return "Linux " + HOSTNAME + " 5.15.0-1-generic #1 SMP Mon Jan 01 00:00:00 UTC 2024 x86_64 GNU/Linux";
        return "Linux";
    }

    private String cmdPs(String[] args) {
        return "  PID TTY          TIME CMD\n" +
               "    1 ?        00:00:01 systemd\n" +
               "  432 ?        00:00:00 sshd\n" +
               "  891 pts/0    00:00:00 bash\n" +
               "  892 pts/0    00:00:00 ps";
    }

    private String cmdTop() {
        return "top - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) +
               " up 3 days, 14:22,  1 user,  load average: 0.08, 0.05, 0.01\n" +
               "Tasks:  87 total,   1 running,  86 sleeping,   0 stopped\n" +
               "%Cpu(s):  0.3 us,  0.1 sy,  0.0 ni, 99.5 id,  0.0 wa,  0.0 hi\n" +
               "MiB Mem :   1990.5 total,   1402.3 free,    312.6 used,    275.6 buff/cache\n\n" +
               "  PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND\n" +
               "    1 root      20   0  169032  13600   8540 S   0.0   0.7   0:01.23 systemd\n" +
               "  432 root      20   0  136548   7400   6400 S   0.0   0.4   0:00.12 sshd\n" +
               "  891 user      20   0   24456   5800   4600 S   0.3   0.3   0:00.08 bash";
    }

    private String cmdDf(String[] args) {
        return "Filesystem      Size  Used Avail Use% Mounted on\n" +
               "overlay          50G   12G   38G  24% /\n" +
               "tmpfs            64M     0   64M   0% /dev\n" +
               "tmpfs           997M     0  997M   0% /sys/fs/cgroup\n" +
               "/dev/sda1        50G   12G   38G  24% /etc/hosts\n" +
               "tmpfs           997M     0  997M   0% /tmp\n" +
               "tmpfs           200M     0  200M   0% /run/user/1000";
    }

    private String cmdDu(String[] args) {
        return "4\t" + fs.getCurrentPath();
    }

    private String cmdFree(String[] args) {
        boolean human = args.length > 0 && args[0].contains("h");
        if (human) {
            return "              total        used        free      shared  buff/cache   available\n" +
                   "Mem:           1.9G        312M        1.4G        1.0M        276M        1.5G\n" +
                   "Swap:          2.0G          0B        2.0G";
        }
        return "              total        used        free      shared  buff/cache   available\n" +
               "Mem:        2038272      320000     1435000       1024      283272     1582000\n" +
               "Swap:       2097152           0     2097152";
    }

    private String cmdEnv() {
        return "SHELL=/bin/bash\nTERM=xterm-256color\nUSER=" + USERNAME +
               "\nHOME=/home/" + USERNAME + "\nPATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin\n" +
               "HOSTNAME=" + HOSTNAME + "\nLANG=en_US.UTF-8\nEDITOR=nano";
    }

    private String cmdPing(String[] args) {
        if (args.length == 0) return "ping: usage error: Destination address required";
        String host = args[args.length - 1];
        return "PING " + host + " (93.184.216.34) 56(84) bytes of data.\n" +
               "64 bytes from " + host + " (93.184.216.34): icmp_seq=1 ttl=56 time=14.2 ms\n" +
               "64 bytes from " + host + " (93.184.216.34): icmp_seq=2 ttl=56 time=13.9 ms\n" +
               "64 bytes from " + host + " (93.184.216.34): icmp_seq=3 ttl=56 time=14.1 ms\n" +
               "\n--- " + host + " ping statistics ---\n" +
               "3 packets transmitted, 3 received, 0% packet loss, time 2003ms\n" +
               "rtt min/avg/max/mdev = 13.9/14.1/14.2/0.1 ms";
    }

    private String cmdIfconfig() {
        return "eth0: flags=4163<UP,BROADCAST,RUNNING,MULTICAST>  mtu 1500\n" +
               "        inet 192.168.1.100  netmask 255.255.255.0  broadcast 192.168.1.255\n" +
               "        inet6 fe80::a00:27ff:fe1e:ac9a  prefixlen 64  scopeid 0x20<link>\n" +
               "        ether 08:00:27:1e:ac:9a  txqueuelen 1000  (Ethernet)\n" +
               "        RX packets 12345  bytes 1234567 (1.2 MB)\n" +
               "        TX packets 6789   bytes 678901 (678.9 KB)\n\n" +
               "lo: flags=73<UP,LOOPBACK,RUNNING>  mtu 65536\n" +
               "        inet 127.0.0.1  netmask 255.0.0.0\n" +
               "        loop  txqueuelen 1000  (Local Loopback)";
    }

    private String cmdIp(String[] args) {
        if (args.length > 0 && (args[0].equals("addr") || args[0].equals("a"))) {
            return cmdIfconfig();
        }
        return "Usage: ip [ OPTIONS ] OBJECT { COMMAND | help }\nOBJECTs: addr, link, route";
    }

    private String cmdCurl(String[] args) {
        if (args.length == 0) return "curl: try 'curl --help' for more information";
        String url = args[args.length - 1];
        return "<!DOCTYPE html><html><head><title>Example</title></head><body>\n" +
               "<h1>Example Domain</h1><p>This domain is for illustrative examples.</p>\n" +
               "</body></html>";
    }

    private String cmdWget(String[] args) {
        if (args.length == 0) return "wget: missing URL";
        String url = args[args.length - 1];
        String filename = url.contains("/") ? url.substring(url.lastIndexOf('/') + 1) : "index.html";
        if (filename.isEmpty()) filename = "index.html";
        fs.createFile(filename);
        return "--" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +
               "--  " + url + "\nResolving " + url.replaceAll("https?://", "").split("/")[0] + "...\n" +
               "Connecting... connected.\nHTTP request sent, awaiting response... 200 OK\n" +
               "Length: 1270 (1.2K) [text/html]\nSaving to: '" + filename + "'\n\n" +
               filename + "          100%[===================>]   1.24K  --.-KB/s   in 0s\n\n" +
               "'" + filename + "' saved [1270/1270]";
    }

    private String cmdSsh(String[] args) {
        if (args.length == 0) return "usage: ssh [-l login_name] hostname [command]";
        return "ssh: connect to host " + args[args.length - 1] + " port 22: Connection refused";
    }

    private String cmdHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++)
            sb.append(String.format("  %3d  %s\n", i + 1, history.get(i)));
        return sb.toString().stripTrailing();
    }

    private String cmdHelp() {
        return "Linux Terminal Simulator — available commands\n" +
               "─────────────────────────────────────────────\n" +
               "Navigation:\n" +
               "  ls [-la]         list directory contents\n" +
               "  cd [dir]         change directory  (cd ~ goes home)\n" +
               "  pwd              print working directory\n" +
               "  tree             display directory tree\n\n" +
               "Files:\n" +
               "  cat [file]       print file contents\n" +
               "  touch [file]     create empty file\n" +
               "  mkdir [dir]      create directory\n" +
               "  rmdir [dir]      remove empty directory\n" +
               "  rm [-rf] [file]  remove file or directory\n" +
               "  cp src dst       copy file\n" +
               "  mv src dst       move/rename file\n" +
               "  echo [text]      print text\n" +
               "  grep PAT FILE    search file for pattern\n\n" +
               "System:\n" +
               "  whoami           current user\n" +
               "  hostname         machine hostname\n" +
               "  date             current date/time\n" +
               "  uname [-a]       system information\n" +
               "  uptime           system uptime\n" +
               "  ps               process list\n" +
               "  top              resource overview\n" +
               "  df [-h]          disk space usage\n" +
               "  free [-h]        memory usage\n" +
               "  id               user/group IDs\n" +
               "  env              environment variables\n\n" +
               "Network (simulated):\n" +
               "  ping HOST        ping a host\n" +
               "  ifconfig         network interfaces\n" +
               "  ip addr          same as ifconfig\n" +
               "  curl URL         HTTP request\n" +
               "  wget URL         download file\n" +
               "  ssh HOST         connect via SSH\n\n" +
               "Other:\n" +
               "  history          command history\n" +
               "  man CMD          manual page for CMD\n" +
               "  sudo CMD         run as root (simulated)\n" +
               "  which CMD        locate a command\n" +
               "  clear            clear the screen\n" +
               "  exit             close the terminal\n\n" +
               "Tip: use ↑/↓ arrow keys to navigate command history.";
    }

    private String cmdMan(String[] args) {
        if (args.length == 0) return "What manual page do you want?\nFor example, try 'man ls'";
        Map<String, String> pages = new HashMap<>();
        pages.put("ls",   "LS(1)\nNAME\n  ls - list directory contents\nSYNOPSIS\n  ls [OPTION]... [FILE]...\nOPTIONS\n  -l  long listing format\n  -a  show hidden files\n  -la combine both");
        pages.put("cd",   "CD(1)\nNAME\n  cd - change working directory\nSYNOPSIS\n  cd [DIR]\nDESCRIPTION\n  Change the current directory to DIR.\n  With no argument, change to HOME (~).");
        pages.put("cat",  "CAT(1)\nNAME\n  cat - concatenate and print files\nSYNOPSIS\n  cat [FILE]...\nDESCRIPTION\n  Concatenate FILE(s) to standard output.");
        pages.put("echo", "ECHO(1)\nNAME\n  echo - display a line of text\nSYNOPSIS\n  echo [-n] [STRING]\nOPTIONS\n  -n  do not output trailing newline");
        pages.put("grep", "GREP(1)\nNAME\n  grep - print lines that match patterns\nSYNOPSIS\n  grep PATTERN FILE\nDESCRIPTION\n  Search for PATTERN in FILE and print matching lines.");
        pages.put("rm",   "RM(1)\nNAME\n  rm - remove files or directories\nSYNOPSIS\n  rm [OPTION]... FILE...\nOPTIONS\n  -r  remove directories recursively\n  -f  ignore nonexistent files");
        pages.put("mkdir","MKDIR(1)\nNAME\n  mkdir - make directories\nSYNOPSIS\n  mkdir [OPTION]... DIRECTORY...\nDESCRIPTION\n  Create the DIRECTORY(ies), if they do not already exist.");
        pages.put("ping", "PING(8)\nNAME\n  ping - send ICMP ECHO_REQUEST to network hosts\nSYNOPSIS\n  ping HOST\nDESCRIPTION\n  ping uses the ICMP protocol's ECHO_REQUEST datagram.");
        pages.put("ssh",  "SSH(1)\nNAME\n  ssh - OpenSSH remote login client\nSYNOPSIS\n  ssh [-l login] hostname [command]\nDESCRIPTION\n  Connects to the given hostname using SSH protocol.");
        String page = pages.get(args[0]);
        return page != null ? page : "No manual entry for " + args[0];
    }

    private String cmdSudo(String[] args) {
        if (args.length == 0) return "usage: sudo command";
        if (args[0].equals("-s") || args[0].equals("-i")) return "root@" + HOSTNAME + ":~# (simulated root — type 'exit' to leave)";
        String[] subArgs = Arrays.copyOfRange(args, 0, args.length);
        String sub = String.join(" ", subArgs);
        String result = process(sub);
        if (result.equals(SIGNAL_EXIT) || result.equals(SIGNAL_CLEAR)) return result;
        return result.isEmpty() ? "" : result;
    }

    private String cmdWhich(String[] args) {
        if (args.length == 0) return "";
        Set<String> known = new HashSet<>(Arrays.asList(
                "ls","cd","pwd","cat","touch","mkdir","rmdir","rm","cp","mv","echo","grep",
                "whoami","hostname","date","uname","uptime","ps","top","df","du","free","env","id",
                "ping","ifconfig","ip","curl","wget","ssh",
                "history","clear","exit","help","man","sudo","su","which","alias","export","sleep"));
        StringBuilder sb = new StringBuilder();
        for (String a : args) {
            if (known.contains(a)) sb.append("/usr/bin/").append(a).append("\n");
            else                   sb.append(a).append(" not found\n");
        }
        return sb.toString().stripTrailing();
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
