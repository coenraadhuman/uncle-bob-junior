package com.plg.gameoflife;

import java.io.PrintStream;

/** Redraws frames in place using ANSI escape codes; requires an ANSI-compatible terminal. */
public final class TerminalScreen {

    private static final String ANSI_CLEAR_SCREEN = "\u001b[2J";
    private static final String ANSI_CURSOR_HOME = "\u001b[H";
    private static final String ANSI_HIDE_CURSOR = "\u001b[?25l";
    private static final String ANSI_SHOW_CURSOR = "\u001b[?25h";

    private final PrintStream out;

    public TerminalScreen(PrintStream out) {
        this.out = out;
    }

    public void open() {
        out.print(ANSI_CLEAR_SCREEN);
        out.print(ANSI_HIDE_CURSOR);
        out.flush();
    }

    public void draw(String frame) {
        out.print(ANSI_CURSOR_HOME);
        out.print(frame);
        out.flush();
    }

    public void close() {
        out.print(ANSI_SHOW_CURSOR);
        out.flush();
    }
}
