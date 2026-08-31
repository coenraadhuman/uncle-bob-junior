// src/test/java/life/GridFactoryTest.java
package life;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridFactoryTest {
    private static final int TEST_GRID_SIZE = 3;

    @Test
    void allCellsDeadWhenDensityIsZero() {
        Grid grid = GridFactory.randomGrid(TEST_GRID_SIZE, TEST_GRID_SIZE, 0.0, new Random());
        assertAllCellsAre(grid, CellState.DEAD);
    }

    @Test
    void allCellsAliveWhenDensityIsOne() {
        Grid grid = GridFactory.randomGrid(TEST_GRID_SIZE, TEST_GRID_SIZE, 1.0, new Random());
        assertAllCellsAre(grid, CellState.ALIVE);
    }

    private void assertAllCellsAre(Grid grid, CellState expectedState) {
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                assertEquals(expectedState, grid.cellAt(row, col));
            }
        }
    }
}
