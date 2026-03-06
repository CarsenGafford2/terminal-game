# terminal-game

A simulated Linux terminal game built with Java Swing.

The application opens a windowed terminal emulator — not an actual
`cmd` or PowerShell — that lets you explore a virtual filesystem and
run a wide variety of common Linux commands.

---

## Features

- **Windowed terminal** with a dark colour theme (no native shell spawned)
- **Virtual filesystem** – an in-memory Linux directory tree populated
  with sample files so you have something to explore right away
- **30+ simulated commands**:
  `ls`, `cd`, `pwd`, `tree`, `cat`, `touch`, `mkdir`, `rmdir`, `rm`,
  `cp`, `mv`, `echo`, `grep`, `whoami`, `hostname`, `date`, `uname`,
  `uptime`, `ps`, `top`, `df`, `free`, `env`, `id`,
  `ping`, `ifconfig`, `ip addr`, `curl`, `wget`, `ssh`,
  `history`, `man`, `sudo`, `which`, `clear`, `exit`
- **Command history** – navigate with ↑ / ↓ arrow keys
- **Coloured prompt** – `user@server01:path$` with distinct colours for
  each segment

---

## Requirements

- Java 11 or later (tested with OpenJDK 17)
- `make` (optional, for the convenience targets)

---

## Build & Run

### With Make

```bash
make run
```

### Without Make

```bash
javac -d bin -sourcepath src src/terminal/*.java
java  -cp bin terminal.Main
```

---

## Project Layout

```
terminal-game/
├── src/
│   └── terminal/
│       ├── Main.java              # entry point
│       ├── TerminalWindow.java    # Swing GUI
│       ├── CommandProcessor.java  # command parsing & dispatch
│       └── VirtualFileSystem.java # in-memory filesystem
├── Makefile
└── README.md
```
