package com.example.life;

import java.util.Random;

/**
 * Conway's Game of Life rendered in-place in the terminal.
 *
 * The screen is cleared once at start-up; every subsequent generation is
 * drawn by moving the cursor back to the top-left (ANSI "cursor home"),
 * so the UI never scrolls and no new lines accumulate in the terminal.
 *
 * Usage: java -jar game-of-life.jar [width] [height] [generations]
 * Defaults: 40 x 20, unlimited generations (Ctrl+C to stop).
 */
public final class GameOfLife {

    private static final String ANSI_CLEAR_SCREEN = "\u001B[2J";
    private static final String ANSI_CURSOR_HOME = "\u001B[H";
    private static final String ANSI_HIDE_CURSOR = "\u001B[?25l";
    private static final String ANSI_SHOW_CURSOR = "\u001B[?25h";

    private static final char ALIVE = '\u2588'; // full block
    private static final char DEAD = '\u00B7';  // middle dot
    private static final long FRAME_DELAY_MS = 100;

    private final int width;
    private final int height;
    private boolean[][] cells;

    public GameOfLife(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
        Random random = new Random(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = random.nextInt(4) == 0; // ~25% initial density
            }
        }
    }

    /** Applies the standard B3/S23 rules on a toroidal (wrap-around) grid. */
    private void step() {
        boolean[][] next = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbours = countNeighbours(x, y);
                next[y][x] = neighbours == 3 || (cells[y][x] && neighbours == 2);
            }
        }
        cells = next;
    }

    private int countNeighbours(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = Math.floorMod(x + dx, width);
                int ny = Math.floorMod(y + dy, height);
                if (cells[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private String render(long generation) {
        StringBuilder frame = new StringBuilder((width + 1) * (height + 2));
        frame.append(ANSI_CURSOR_HOME);
        frame.append("Conway's Game of Life  |  generation ").append(generation)
             .append("  |  Ctrl+C to quit").append('\n');
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                frame.append(cells[y][x] ? ALIVE : DEAD);
            }
            frame.append('\n');
        }
        return frame.toString();
    }

    public void run(long maxGenerations) throws InterruptedException {
        System.out.print(ANSI_HIDE_CURSOR + ANSI_CLEAR_SCREEN);
        try {
            long generation = 0;
            while (maxGenerations < 0 || generation < maxGenerations) {
                System.out.print(render(generation));
                System.out.flush();
                Thread.sleep(FRAME_DELAY_MS);
                step();
                generation++;
            }
        } finally {
            System.out.print(ANSI_SHOW_CURSOR);
            System.out.flush();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int width = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        int height = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        long maxGenerations = args.length > 2 ? Long.parseLong(args[2]) : -1;

        // Restore the cursor if the process is killed mid-run (e.g. Ctrl+C).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(ANSI_SHOW_CURSOR);
            System.out.flush();
        }));

        new GameOfLife(width, height, System.nanoTime()).run(maxGenerations);
    }
}
