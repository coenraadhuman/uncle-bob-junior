// src/test/java/life/NeighborCounterTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeighborCounterTest {

    @Test
    void countsAllEightNeighborsWhenAllAreAlive() {
        Grid grid = TestGridSupport.gridFrom(
            "OOO",
            "O.O",
            "OOO"
        );
        assertEquals(8, NeighborCounter.countAliveNeighbors(grid, 1, 1));
    }

    @Test
    void countsZeroNeighborsWhenAllAreDead() {
        Grid grid = TestGridSupport.gridFrom(
            "...",
            "...",
            "..."
        );
        assertEquals(0, NeighborCounter.countAliveNeighbors(grid, 1, 1));
    }

    @Test
    void wrapsAroundGridEdgesToCountDiagonalNeighbor() {
        Grid grid = TestGridSupport.gridFrom(
            "O..",
            "...",
            "..O"
        );
        assertEquals(1, NeighborCounter.countAliveNeighbors(grid, 0, 0));
    }
}
