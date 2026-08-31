package com.example.life;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleRendererTest {

    private final ConsoleRenderer renderer = new ConsoleRenderer();

    @Test
    void rendersLiveAndDeadCellsRowByRow() {
        Grid grid = Grid.fromPattern(List.of(
                "#.",
                ".#"));

        assertEquals("█·\n·█\n", renderer.render(grid));
    }

    @Test
    void rendersFullyDeadGridAsDotsOnly() {
        Grid grid = Grid.fromPattern(List.of("..."));

        assertEquals("···\n", renderer.render(grid));
    }
}
