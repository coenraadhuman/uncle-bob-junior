package com.example.life;

/**
 * Terminal front end. Clears the screen once, then redraws every frame from
 * the cursor-home position so the UI stays fixed in place with no scrolling.
 */
public final class GameOfLifeApp {

    private static final int ROWS = 24;
    private static final int COLUMNS = 60;
    private static final long FRAME_DELAY_MILLIS = 150;

    private static final String CLEAR_SCREEN = "\u001B[2J";
    private static final String CURSOR_HOME = "\u001B[H";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";

    private static final int[][] SEED = {
        {1, 2}, {2, 3}, {3, 1}, {3, 2}, {3, 3},   // glider
        {10, 28}, {10, 29}, {10, 30},             // blinker
        {5, 45}, {5, 46}, {6, 45}, {6, 46}        // block
    };

    private GameOfLifeApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.print(SHOW_CURSOR)));
        System.out.print(CLEAR_SCREEN + HIDE_CURSOR);
        runForever(Grid.withLiveCells(ROWS, COLUMNS, SEED));
    }

    private static void runForever(Grid grid) throws InterruptedException {
        long generation = 0;
        while (true) {
            drawFrame(grid, generation);
            Thread.sleep(FRAME_DELAY_MILLIS);
            grid = grid.nextGeneration();
            generation++;
        }
    }

    private static void drawFrame(Grid grid, long generation) {
        String statusLine = "Generation " + generation + "  (Ctrl+C to quit)";
        System.out.print(CURSOR_HOME + GridRenderer.render(grid) + statusLine);
        System.out.flush();
    }
}
