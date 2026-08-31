package com.example.gameoflife;

import java.util.Random;

public final class GameOfLife {

    private static final int WIDTH = 80;
    private static final int HEIGHT = 24;
    private static final long FRAME_DELAY_MS = 150L;
    private static final double INITIAL_ALIVE_PROBABILITY = 0.25;
    private static final char ALIVE_CHAR = '#';
    private static final char DEAD_CHAR = ' ';

    private boolean[][] grid;

    public GameOfLife(long seed) {
        this.grid = randomGrid(seed);
    }

    private boolean[][] randomGrid(long seed) {
        Random random = new Random(seed);
        boolean[][] result = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                result[y][x] = random.nextDouble() < INITIAL_ALIVE_PROBABILITY;
            }
        }
        return result;
    }

    private int countLiveNeighbours(int row, int col) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dy == 0 && dx == 0) {
                    continue;
                }
                int ny = (row + dy + HEIGHT) % HEIGHT;
                int nx = (col + dx + WIDTH) % WIDTH;
                if (grid[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void step() {
        boolean[][] next = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int neighbours = countLiveNeighbours(y, x);
                boolean alive = grid[y][x];
                next[y][x] = alive
                        ? (neighbours == 2 || neighbours == 3)
                        : (neighbours == 3);
            }
        }
        grid = next;
    }

    private String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H");
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(grid[y][x] ? ALIVE_CHAR : DEAD_CHAR);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public void run() throws InterruptedException {
        System.out.print("\033[2J\033[H");
        System.out.flush();
        while (true) {
            System.out.print(render());
            System.out.flush();
            step();
            Thread.sleep(FRAME_DELAY_MS);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long seed = System.nanoTime();
        new GameOfLife(seed).run();
    }
}
