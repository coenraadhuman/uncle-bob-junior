package com.plg.gameoflife;

public final class GridRenderer {

    private static final char ALIVE_CELL_SYMBOL = '#';
    private static final char DEAD_CELL_SYMBOL = ' ';

    public String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.rowCount(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int column = 0; column < grid.columnCount(); column++) {
            frame.append(grid.isAlive(row, column) ? ALIVE_CELL_SYMBOL : DEAD_CELL_SYMBOL);
        }
        frame.append(System.lineSeparator());
    }
}
