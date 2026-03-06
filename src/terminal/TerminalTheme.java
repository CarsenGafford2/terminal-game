package terminal;

import java.awt.Color;
import java.awt.Font;

public final class TerminalTheme {

    public static final Color COL_BG     = new Color(0x1a, 0x1a, 0x2e); // deep navy
    public static final Color COL_TEXT   = new Color(0xe0, 0xe0, 0xe0); // light grey
    public static final Color COL_PROMPT = new Color(0x79, 0xb8, 0xff); // blue
    public static final Color COL_PATH   = new Color(0xb3, 0xf0, 0xa5); // green
    public static final Color COL_DOLLAR = new Color(0xff, 0xff, 0xff); // white
    public static final Color COL_ERROR  = new Color(0xff, 0x6b, 0x6b); // red
    public static final Color COL_INPUT  = new Color(0xe0, 0xe0, 0xe0); // same as text
    public static final Color COL_BORDER = new Color(0x30, 0x30, 0x55); // subtle border

    public static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    public static final int  PAD  = 8;

    private TerminalTheme() {}
}
