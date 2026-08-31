package com.example.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    private static final int[][] NO_LIVE_CELLS = {};

    @Test
    void lonelyCellDiesOfUnderpopulation() {
        Grid grid = Grid.withLiveCells(3, 3, new int[][] {{1, 1}});
        assertFalse(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void cellWithFourNeighboursDiesOfOvercrowding() {
        Grid grid = Grid.withLiveCells(3, 3,
            new int[][] {{1, 1}, {0, 0}, {0, 2}, {2, 0}, {2, 2}});
        assertFalse(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void deadCellWithExactlyThreeNeighboursComesToLife() {
        Grid grid = Grid.withLiveCells(3, 3, new int[][] {{0, 0}, {0, 1}, {0, 2}});
        assertTrue(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void blockStillLifeIsStable() {
        Grid block = Grid.withLiveCells(4, 4, new int[][] {{1, 1}, {1, 2}, {2, 1}, {2, 2}});
        Grid next = block.nextGeneration();
        assertTrue(next.isAlive(1, 1));
        assertTrue(next.isAlive(1, 2));
        assertTrue(next.isAlive(2, 1));
        assertTrue(next.isAlive(2, 2));
        assertFalse(next.isAlive(0, 0));
    }

    @Test
    void blinkerOscillatesBetweenVerticalAndHorizontal() {
        Grid vertical = Grid.withLiveCells(5, 5, new int[][] {{1, 2}, {2, 2}, {3, 2}});
        Grid horizontal = vertical.nextGeneration();
        assertTrue(horizontal.isAlive(2, 1));
        assertTrue(horizontal.isAlive(2, 2));
        assertTrue(horizontal.isAlive(2, 3));
        assertFalse(horizontal.isAlive(1, 2));
        assertFalse(horizontal.isAlive(3, 2));
        Grid backToVertical = horizontal.nextGeneration();
        assertTrue(backToVertical.isAlive(1, 2));
        assertTrue(backToVertical.isAlive(3, 2));
    }

    @Test
    void outOfBoundsCoordinatesAreDead() {
        Grid grid = Grid.withLiveCells(2, 2, new int[][] {{0, 0}});
        assertFalse(grid.isAlive(-1, 0));
        assertFalse(grid.isAlive(0, -1));
        assertFalse(grid.isAlive(2, 0));
        assertFalse(grid.isAlive(0, 2));
    }

    @Test
    void emptyGridStaysEmpty() {
        Grid empty = Grid.withLiveCells(3, 3, NO_LIVE_CELLS);
        Grid next = empty.nextGeneration();
        for (int row = 0; row < next.rows(); row++) {
            for (int column = 0; column < next.columns(); column++) {
                assertFalse(next.isAlive(row, column));
            }
        }
    }

    @Test
    void nonPositiveDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Grid.withLiveCells(0, 3, NO_LIVE_CELLS));
        assertThrows(IllegalArgumentException.class, () -> Grid.withLiveCells(3, 0, NO_LIVE_CELLS));
    }
}
