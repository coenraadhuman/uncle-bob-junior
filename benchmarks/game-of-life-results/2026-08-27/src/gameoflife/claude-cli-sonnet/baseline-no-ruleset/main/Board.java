package com.example.gameoflife;

import java.util.Random;

public final class Board {

    private final int width;
    private final int height;
    private boolean[][] cells;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
    }

    public void randomize(double aliveProbability, long seed) {
        Random random = new Random(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = random.nextDouble() < aliveProbability;
            }
        }
    }

    public boolean isAlive(int x, int y) {
        return cells[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int countLiveNeighbours(int x, int y) {
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

    public void step() {
        boolean[][] next = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbours = countLiveNeighbours(x, y);
                boolean alive = cells[y][x];
                next[y][x] = alive
                        ? (neighbours == 2 || neighbours == 3)
                        : (neighbours == 3);
            }
        }
        cells = next;
    }
}
