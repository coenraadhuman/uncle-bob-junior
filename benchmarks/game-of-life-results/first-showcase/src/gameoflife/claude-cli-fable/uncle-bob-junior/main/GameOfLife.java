package com.example.life;

import java.util.ArrayList;
import java.util.List;

/** Terminal entry point: seeds a glider and animates it in place using ANSI cursor control. */
public final class GameOfLife {

    private static final String CLEAR_SCREEN = "\u001B[2J";
    private static final String CURSOR_HOME = "\u001B[H";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";

    private static final long FRAME_DELAY_MILLIS = 120;
    private static final int GENERATIONS = 300;
    private static final int FIELD_HEIGHT = 15;
    private static final int FIELD_WIDTH = 40;
    private static final String DEAD_CELL_MARK = ".";
    private static final List<String> GLIDER = List.of(".#.", "..#", "###");

    private GameOfLife() {
    }

    public static void main(String[] args) throws InterruptedException {
        Grid grid = Grid.fromPattern(gliderSeed());
        System.out.print(CLEAR_SCREEN + HIDE_CURSOR);
        try {
            runSimulation(grid, new ConsoleRenderer());
        } finally {
            System.out.print(SHOW_CURSOR);
        }
    }

    private static void runSimulation(Grid grid, ConsoleRenderer renderer) throws InterruptedException {
        for (int generation = 0; generation < GENERATIONS; generation++) {
            System.out.print(CURSOR_HOME + renderer.render(grid));
            System.out.flush();
            Thread.sleep(FRAME_DELAY_MILLIS);
            grid = grid.nextGeneration();
        }
    }

    private static List<String> gliderSeed() {
        List<String> rows = new ArrayList<>();
        for (int row = 0; row < FIELD_HEIGHT; row++) {
            rows.add(seedRow(row));
        }
        return rows;
    }

    private static String seedRow(int row) {
        if (row >= GLIDER.size()) {
            return DEAD_CELL_MARK.repeat(FIELD_WIDTH);
        }
        String glider = GLIDER.get(row);
        return glider + DEAD_CELL_MARK.repeat(FIELD_WIDTH - glider.length());
    }
}
