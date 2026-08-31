// src/main/java/life/Grid.java
package life;

import java.util.Arrays;
import java.util.Objects;

public final class Grid {
    private final CellState[][] cells;
    private final int height;
    private final int width;

    Grid(CellState[][] cells) {
        Objects.requireNonNull(cells, "cells must not be null");
        this.cells = deepCopy(cells);
        this.height = cells.length;
        this.width = cells[0].length;
    }

    private static CellState[][] deepCopy(CellState[][] source) {
        CellState[][] copy = new CellState[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    public CellState cellAt(int row, int col) {
        return cells[row][col];
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public Grid nextGeneration() {
        return new Grid(computeNextCells());
    }

    private CellState[][] computeNextCells() {
        CellState[][] nextCells = new CellState[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                nextCells[row][col] = nextStateForCell(row, col);
            }
        }
        return nextCells;
    }

    private CellState nextStateForCell(int row, int col) {
        int aliveNeighbors = NeighborCounter.countAliveNeighbors(this, row, col);
        return LifeRules.nextState(cellAt(row, col), aliveNeighbors);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid otherGrid)) {
            return false;
        }
        return Arrays.deepEquals(this.cells, otherGrid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
