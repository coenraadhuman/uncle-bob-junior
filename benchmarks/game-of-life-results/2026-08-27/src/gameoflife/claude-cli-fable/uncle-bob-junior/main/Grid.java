package com.example.life;

/**
 * Immutable bounded Game of Life grid. Cells outside the bounds are
 * permanently dead. Each generation is a new Grid; instances never change.
 */
public final class Grid {

    private static final int NEIGHBOURS_FOR_BIRTH = 3;
    private static final int MIN_NEIGHBOURS_TO_SURVIVE = 2;
    private static final int MAX_NEIGHBOURS_TO_SURVIVE = 3;

    private final boolean[][] cells;

    private Grid(boolean[][] cells) {
        this.cells = cells;
    }

    /** Creates a grid with the given {row, column} cells alive. */
    public static Grid withLiveCells(int rows, int columns, int[][] liveCells) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        boolean[][] cells = new boolean[rows][columns];
        for (int[] cell : liveCells) {
            cells[cell[0]][cell[1]] = true;
        }
        return new Grid(cells);
    }

    public int rows() {
        return cells.length;
    }

    public int columns() {
        return cells[0].length;
    }

    /** Out-of-bounds coordinates are dead, never an error. */
    public boolean isAlive(int row, int column) {
        boolean outOfBounds = row < 0 || row >= rows() || column < 0 || column >= columns();
        return !outOfBounds && cells[row][column];
    }

    public Grid nextGeneration() {
        boolean[][] next = new boolean[rows()][columns()];
        for (int row = 0; row < rows(); row++) {
            for (int column = 0; column < columns(); column++) {
                next[row][column] = nextCellState(isAlive(row, column), liveNeighbours(row, column));
            }
        }
        return new Grid(next);
    }

    private int liveNeighbours(int row, int column) {
        int count = 0;
        for (int deltaRow = -1; deltaRow <= 1; deltaRow++) {
            for (int deltaColumn = -1; deltaColumn <= 1; deltaColumn++) {
                count += isNeighbourAlive(row, column, deltaRow, deltaColumn) ? 1 : 0;
            }
        }
        return count;
    }

    private boolean isNeighbourAlive(int row, int column, int deltaRow, int deltaColumn) {
        boolean isSelf = deltaRow == 0 && deltaColumn == 0;
        return !isSelf && isAlive(row + deltaRow, column + deltaColumn);
    }

    private static boolean nextCellState(boolean alive, int liveNeighbours) {
        if (alive) {
            return liveNeighbours >= MIN_NEIGHBOURS_TO_SURVIVE
                && liveNeighbours <= MAX_NEIGHBOURS_TO_SURVIVE;
        }
        return liveNeighbours == NEIGHBOURS_FOR_BIRTH;
    }
}
