// src/main/java/life/TerminalRenderer.java
package life;

public final class TerminalRenderer {
    private static final char ALIVE_SYMBOL = 'O';
    private static final char DEAD_SYMBOL = ' ';
    private static final String CURSOR_HOME = "\u001b[H";
    private static final String CLEAR_SCREEN = "\u001b[2J";
    private static final int GENERATION_LABEL_WIDTH = 6;

    public void clearScreen() {
        System.out.print(CLEAR_SCREEN + CURSOR_HOME);
        System.out.flush();
    }

    public void render(Grid grid, int generation) {
        StringBuilder frame = new StringBuilder();
        frame.append(CURSOR_HOME);
        appendGrid(frame, grid);
        appendStatusLine(frame, generation);
        System.out.print(frame);
        System.out.flush();
    }

    private void appendGrid(StringBuilder frame, Grid grid) {
        for (int row = 0; row < grid.height(); row++) {
            appendRow(frame, grid, row);
        }
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int col = 0; col < grid.width(); col++) {
            frame.append(grid.cellAt(row, col) == CellState.ALIVE ? ALIVE_SYMBOL : DEAD_SYMBOL);
        }
        frame.append('\n');
    }

    private void appendStatusLine(StringBuilder frame, int generation) {
        frame.append(String.format("Generation: %-" + GENERATION_LABEL_WIDTH + "d%n", generation));
    }
}
