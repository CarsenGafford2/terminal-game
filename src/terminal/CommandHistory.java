package terminal;

import java.util.ArrayList;
import java.util.List;

public class CommandHistory {

    private final List<String> entries = new ArrayList<>();
    private int index = -1;

    public void add(String entry) {
        entries.add(entry);
        index = -1;
    }

    public void resetIndex() {
        index = -1;
    }

    public String navigateUp() {
        if (entries.isEmpty()) return "";
        if (index < 0) index = entries.size();
        if (index > 0) index--;
        return entries.get(index);
    }

    public String navigateDown() {
        if (entries.isEmpty()) return "";
        if (index < 0) return "";
        index++;
        if (index >= entries.size()) {
            index = entries.size();
            return "";
        }
        return entries.get(index);
    }

    public String formatted() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            sb.append(String.format("  %3d  %s%n", i + 1, entries.get(i)));
        }
        return sb.toString().stripTrailing();
    }
}
