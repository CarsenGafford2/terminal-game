package terminal;

import java.util.ArrayList;
import java.util.List;

public class InputTokenizer {

    private InputTokenizer() {}

    public static List<String> tokenise(String input) {
        List<String>  tokens  = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean       inQuotes = false;

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
