package com.plg.gameoflife;

import java.util.random.RandomGenerator;

public final class GameOfLifeSimulation {

    private static final int GRID_ROW_COUNT = 20;
    private static final int GRID_COLUMN_COUNT = 40;
    private static final double INITIAL_ALIVE_PROBABILITY = 0.25;
    private static final int GENERATION_COUNT = 200;
    private static final long FRAME_DELAY_MILLISECONDS = 150L;

    public static void main(String[] args) throws InterruptedException {
        Grid initialGrid = Grid.random(
                GRID_ROW_COUNT, GRID_COLUMN_COUNT, INITIAL_ALIVE_PROBABILITY, RandomGenerator.getDefault());
        GridRenderer renderer = new GridRenderer();
        TerminalScreen screen = new TerminalScreen(System.out);

        screen.open();
        try {
            runGenerations(initialGrid, renderer, screen);
        } finally {
            screen.close();
        }
    }

    private static void runGenerations(Grid grid, GridRenderer renderer, TerminalScreen screen)
            throws InterruptedException {
        Grid currentGrid = grid;
        for (int generation = 0; generation < GENERATION_COUNT; generation++) {
            screen.draw(renderer.render(currentGrid));
            currentGrid = currentGrid.next();
            Thread.sleep(FRAME_DELAY_MILLISECONDS);
        }
    }
}
