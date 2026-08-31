package com.plg.gameoflife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    @Test
    void blockStillLifeRemainsUnchanged() {
        boolean[][] cells = {
                {false, false, false, false, false, false},
                {false, false, false, false, false, false},
                {false, false, true, true, false, false},
                {false, false, true, true, false, false},
                {false, false, false, false, false, false},
                {false, false, false, false, false, false},
        };
        Grid grid = new Grid(cells);

        assertEquals(grid, grid.next());
    }

    @Test
    void verticalBlinkerBecomesHorizontalBlinker() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false},
        };
        boolean[][] expected = {
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertEquals(new Grid(expected), next);
    }

    @Test
    void isolatedLiveCellDiesFromUnderpopulation() {
        boolean[][] cells = {
                {false, false, false},
                {false, true, false},
                {false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertFalse(next.isAlive(1, 1));
    }

    @Test
    void liveCellWithFourNeighborsDiesFromOverpopulation() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, true, true, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertFalse(next.isAlive(2, 2));
    }

    @Test
    void deadCellWithThreeNeighborsIsBorn() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertTrue(next.isAlive(2, 2));
    }

    @Test
    void wrapsCoordinatesToOppositeEdge() {
        boolean[][] cells = {
                {true, false},
                {false, false},
        };
        Grid grid = new Grid(cells);

        assertTrue(grid.isAlive(-2, -2));
        assertTrue(grid.isAlive(2, 2));
        assertFalse(grid.isAlive(-1, -1));
    }
}
