package terminal;

public class PromptFormatter {

    private static final String USERNAME  = "user";
    private static final String HOSTNAME  = "server01";
    private static final String HOME_PATH = "/home/user";

    private PromptFormatter() {}

    public static String format(String currentPath) {
        String display = currentPath;
        if (display.startsWith(HOME_PATH)) {
            display = "~" + display.substring(HOME_PATH.length());
        }
        return USERNAME + "@" + HOSTNAME + ":" + display + "$ ";
    }
}
