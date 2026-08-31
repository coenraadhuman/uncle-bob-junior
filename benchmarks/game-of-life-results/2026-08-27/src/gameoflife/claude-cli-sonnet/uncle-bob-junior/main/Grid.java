package com.plg.gameoflife;

import java.util.Arrays;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Grid {

    private static final int MIN_SURVIVAL_NEIGHBORS = 2;
    private static final int MAX_SURVIVAL_NEIGHBORS = 3;
    private static final int BIRTH_NEIGHBORS = 3;

    private final boolean[][] cells;
    private final int rowCount;
    private final int columnCount;

    public Grid(boolean[][] cells) {
        Objects.requireNonNull(cells, "cells must not be null");
        this.cells = deepCopy(cells);
        this.rowCount = cells.length;
        this.columnCount = rowCount == 0 ? 0 : cells[0].length;
    }

    public static Grid random(int rowCount, int columnCount, double aliveProbability, RandomGenerator random) {
        boolean[][] seeded = new boolean[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            seedRow(seeded[row], aliveProbability, random);
        }
        return new Grid(seeded);
    }

    private static void seedRow(boolean[] row, double aliveProbability, RandomGenerator random) {
        for (int column = 0; column < row.length; column++) {
            row[column] = random.nextDouble() < aliveProbability;
        }
    }

    public boolean isAlive(int row, int column) {
        int wrappedRow = Math.floorMod(row, rowCount);
        int wrappedColumn = Math.floorMod(column, columnCount);
        return cells[wrappedRow][wrappedColumn];
    }

    public int rowCount() {
        return rowCount;
    }

    public int columnCount() {
        return columnCount;
    }

    public Grid next() {
        boolean[][] nextCells = new boolean[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            computeNextRow(nextCells[row], row);
        }
        return new Grid(nextCells);
    }

    private void computeNextRow(boolean[] nextRow, int row) {
        for (int column = 0; column < columnCount; column++) {
            nextRow[column] = nextState(row, column);
        }
    }

    private boolean nextState(int row, int column) {
        int liveNeighbors = countLiveNeighbors(row, column);
        if (isAlive(row, column)) {
            return liveNeighbors == MIN_SURVIVAL_NEIGHBORS || liveNeighbors == MAX_SURVIVAL_NEIGHBORS;
        }
        return liveNeighbors == BIRTH_NEIGHBORS;
    }

    private int countLiveNeighbors(int row, int column) {
        int count = 0;
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }
                count += isAlive(row + rowOffset, column + columnOffset) ? 1 : 0;
            }
        }
        return count;
    }

    private static boolean[][] deepCopy(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid otherGrid)) {
            return false;
        }
        return Arrays.deepEquals(cells, otherGrid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
