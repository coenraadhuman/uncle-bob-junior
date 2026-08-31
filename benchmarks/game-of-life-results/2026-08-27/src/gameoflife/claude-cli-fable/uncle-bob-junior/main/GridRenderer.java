package com.example.life;

/** Renders a Grid as a block of text, one line per row. Pure function, no I/O. */
public final class GridRenderer {

    private static final char ALIVE_GLYPH = '\u2588'; // █
    private static final char DEAD_GLYPH = '\u00B7';  // ·

    private GridRenderer() {
    }

    public static String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.rows(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private static void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int column = 0; column < grid.columns(); column++) {
            frame.append(grid.isAlive(row, column) ? ALIVE_GLYPH : DEAD_GLYPH);
        }
        frame.append('\n');
    }
}
