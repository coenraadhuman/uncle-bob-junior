package com.plg.gameoflife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridRendererTest {

    @Test
    void rendersAliveCellsAsHashAndDeadCellsAsSpace() {
        boolean[][] cells = {
                {true, false},
                {false, true},
        };
        Grid grid = new Grid(cells);
        String expected = "#" + " " + System.lineSeparator()
                + " " + "#" + System.lineSeparator();

        String frame = new GridRenderer().render(grid);

        assertEquals(expected, frame);
    }
}
