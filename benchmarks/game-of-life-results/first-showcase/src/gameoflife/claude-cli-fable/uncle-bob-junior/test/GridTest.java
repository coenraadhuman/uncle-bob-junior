package com.example.life;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    @Test
    void blinkerOscillatesWithPeriodTwo() {
        Grid vertical = Grid.fromPattern(List.of(
                ".....",
                "..#..",
                "..#..",
                "..#..",
                "....."));
        Grid horizontal = Grid.fromPattern(List.of(
                ".....",
                ".....",
                ".###.",
                ".....",
                "....."));

        assertEquals(horizontal, vertical.nextGeneration());
        assertEquals(vertical, vertical.nextGeneration().nextGeneration());
    }

    @Test
    void blockStillLifeIsStable() {
        Grid block = Grid.fromPattern(List.of(
                "....",
                ".##.",
                ".##.",
                "...."));

        assertEquals(block, block.nextGeneration());
    }

    @Test
    void lonelyCellDiesOfUnderpopulation() {
        Grid lonely = Grid.fromPattern(List.of(
                ".....",
                "..#..",
                "....."));
        Grid empty = Grid.fromPattern(List.of(
                ".....",
                ".....",
                "....."));

        assertEquals(empty, lonely.nextGeneration());
    }

    @Test
    void deadCellWithExactlyThreeNeighboursIsBorn() {
        Grid seed = Grid.fromPattern(List.of(
                ".....",
                ".#.#.",
                "..#..",
                "....."));

        assertTrue(seed.nextGeneration().isAlive(1, 2));
    }

    @Test
    void liveCellWithFourNeighboursDiesOfOvercrowding() {
        Grid crowded = Grid.fromPattern(List.of(
                ".....",
                ".###.",
                "..#..",
                "..#..",
                "....."));

        assertFalse(crowded.nextGeneration().isAlive(2, 2));
    }

    @Test
    void neighboursWrapAroundBoardEdges() {
        Grid corners = Grid.fromPattern(List.of(
                "#..#",
                "....",
                "....",
                "#..."));

        assertTrue(corners.nextGeneration().isAlive(3, 3),
                "corner cell has three neighbours via wrapping and must be born");
    }

    @Test
    void raggedPatternIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Grid.fromPattern(List.of("...", "....")));
    }

    @Test
    void emptyPatternIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Grid.fromPattern(List.of()));
    }
}
