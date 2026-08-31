package com.example.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridRendererTest {

    @Test
    void rendersAliveAndDeadCellsWithOneLinePerRow() {
        Grid grid = Grid.withLiveCells(2, 3, new int[][] {{0, 0}, {1, 2}});
        assertEquals("\u2588\u00B7\u00B7\n\u00B7\u00B7\u2588\n", GridRenderer.render(grid));
    }

    @Test
    void rendersEmptyGridAsAllDeadGlyphs() {
        Grid grid = Grid.withLiveCells(1, 2, new int[][] {});
        assertEquals("\u00B7\u00B7\n", GridRenderer.render(grid));
    }
}
