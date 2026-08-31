package com.example.life;

import java.util.Arrays;
import java.util.List;

/**
 * Immutable Game of Life grid on a toroidal board: neighbours wrap
 * around the edges, so a glider re-enters on the opposite side.
 */
public final class Grid {

    /** Character that marks a live cell in a seed pattern; any other character is dead. */
    public static final char ALIVE_MARKER = '#';

    private static final int SURVIVAL_MIN_NEIGHBOURS = 2;
    private static final int SURVIVAL_MAX_NEIGHBOURS = 3;
    private static final int BIRTH_NEIGHBOURS = 3;

    private static final int[][] NEIGHBOUR_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    private final boolean[][] cells;

    private Grid(boolean[][] cells) {
        this.cells = cells;
    }

    /**
     * Parses a rectangular pattern of equal-length rows.
     *
     * @throws IllegalArgumentException if the pattern is empty or ragged
     */
    public static Grid fromPattern(List<String> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new IllegalArgumentException("Pattern must have at least one non-empty row");
        }
        int width = rows.get(0).length();
        boolean[][] cells = new boolean[rows.size()][];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            cells[rowIndex] = parseRow(rows.get(rowIndex), width);
        }
        return new Grid(cells);
    }

    private static boolean[] parseRow(String row, int expectedWidth) {
        if (row.length() != expectedWidth) {
            throw new IllegalArgumentException("All pattern rows must have length " + expectedWidth);
        }
        boolean[] parsed = new boolean[expectedWidth];
        for (int col = 0; col < expectedWidth; col++) {
            parsed[col] = row.charAt(col) == ALIVE_MARKER;
        }
        return parsed;
    }

    public Grid nextGeneration() {
        boolean[][] next = new boolean[height()][width()];
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                next[row][col] = nextCellState(row, col);
            }
        }
        return new Grid(next);
    }

    public boolean isAlive(int row, int col) {
        return cells[row][col];
    }

    public int height() {
        return cells.length;
    }

    public int width() {
        return cells[0].length;
    }

    private boolean nextCellState(int row, int col) {
        int neighbours = liveNeighbours(row, col);
        if (isAlive(row, col)) {
            return neighbours >= SURVIVAL_MIN_NEIGHBOURS && neighbours <= SURVIVAL_MAX_NEIGHBOURS;
        }
        return neighbours == BIRTH_NEIGHBOURS;
    }

    private int liveNeighbours(int row, int col) {
        int count = 0;
        for (int[] offset : NEIGHBOUR_OFFSETS) {
            if (isAliveWrapped(row + offset[0], col + offset[1])) {
                count++;
            }
        }
        return count;
    }

    private boolean isAliveWrapped(int row, int col) {
        return cells[Math.floorMod(row, height())][Math.floorMod(col, width())];
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid grid)) {
            return false;
        }
        return Arrays.deepEquals(cells, grid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
