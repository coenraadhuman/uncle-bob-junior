// src/main/java/life/NeighborCounter.java
package life;

final class NeighborCounter {
    private static final int[][] NEIGHBOR_OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},           {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    private NeighborCounter() {
    }

    static int countAliveNeighbors(Grid grid, int row, int col) {
        int aliveCount = 0;
        for (int[] offset : NEIGHBOR_OFFSETS) {
            if (isNeighborAlive(grid, row, col, offset)) {
                aliveCount++;
            }
        }
        return aliveCount;
    }

    private static boolean isNeighborAlive(Grid grid, int row, int col, int[] offset) {
        int neighborRow = wrapIndex(row + offset[0], grid.height());
        int neighborCol = wrapIndex(col + offset[1], grid.width());
        return grid.cellAt(neighborRow, neighborCol) == CellState.ALIVE;
    }

    private static int wrapIndex(int index, int size) {
        return ((index % size) + size) % size;
    }
}
