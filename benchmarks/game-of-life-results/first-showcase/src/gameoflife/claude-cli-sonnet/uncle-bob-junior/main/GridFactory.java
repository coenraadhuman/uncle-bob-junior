// src/main/java/life/GridFactory.java
package life;

import java.util.Random;

public final class GridFactory {
    private GridFactory() {
    }

    public static Grid randomGrid(int width, int height, double aliveDensity, Random random) {
        CellState[][] cells = new CellState[height][width];
        for (int row = 0; row < height; row++) {
            fillRow(cells[row], aliveDensity, random);
        }
        return new Grid(cells);
    }

    private static void fillRow(CellState[] row, double aliveDensity, Random random) {
        for (int col = 0; col < row.length; col++) {
            row[col] = random.nextDouble() < aliveDensity ? CellState.ALIVE : CellState.DEAD;
        }
    }
}
