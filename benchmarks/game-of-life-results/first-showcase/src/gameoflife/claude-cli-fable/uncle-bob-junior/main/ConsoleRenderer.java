package com.example.life;

/** Renders a grid as terminal text, one line per row. */
public final class ConsoleRenderer {

    private static final char ALIVE_CELL = '█';
    private static final char DEAD_CELL = '·';

    public String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.height(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int col = 0; col < grid.width(); col++) {
            frame.append(grid.isAlive(row, col) ? ALIVE_CELL : DEAD_CELL);
        }
        frame.append('\n');
    }
}
