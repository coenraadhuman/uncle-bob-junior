// src/test/java/life/GridTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridTest {

    @Test
    void blockStillLifeRemainsStable() {
        Grid block = TestGridSupport.gridFrom(
            "......",
            ".OO...",
            ".OO...",
            "......"
        );
        assertEquals(block, block.nextGeneration());
    }

    @Test
    void blinkerOscillatesWithPeriodTwo() {
        Grid verticalBlinker = TestGridSupport.gridFrom(
            ".....",
            "..O..",
            "..O..",
            "..O..",
            "....."
        );
        Grid horizontalBlinker = TestGridSupport.gridFrom(
            ".....",
            ".....",
            ".OOO.",
            ".....",
            "....."
        );
        assertEquals(horizontalBlinker, verticalBlinker.nextGeneration());
        assertEquals(verticalBlinker, verticalBlinker.nextGeneration().nextGeneration());
    }

    @Test
    void isolatedAliveCellDiesFromUnderpopulation() {
        Grid singleCell = TestGridSupport.gridFrom(
            "...",
            ".O.",
            "..."
        );
        Grid expected = TestGridSupport.gridFrom(
            "...",
            "...",
            "..."
        );
        assertEquals(expected, singleCell.nextGeneration());
    }
}
