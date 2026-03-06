SRCDIR  = src
BINDIR  = bin
MAIN    = terminal.Main
SOURCES = $(wildcard $(SRCDIR)/terminal/*.java)

.PHONY: all run clean

## Compile all Java sources into bin/
all: $(BINDIR)
	javac -d $(BINDIR) -sourcepath $(SRCDIR) $(SOURCES)

## Compile and launch the terminal window
run: all
	java -cp $(BINDIR) $(MAIN)

$(BINDIR):
	mkdir -p $(BINDIR)

## Remove compiled classes
clean:
	rm -rf $(BINDIR)
