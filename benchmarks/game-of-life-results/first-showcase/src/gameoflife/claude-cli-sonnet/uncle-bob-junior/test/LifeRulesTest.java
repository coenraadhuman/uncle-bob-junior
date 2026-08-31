// src/test/java/life/LifeRulesTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeRulesTest {

    @Test
    void aliveCellWithTwoNeighborsSurvives() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.ALIVE, 2));
    }

    @Test
    void aliveCellWithThreeNeighborsSurvives() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.ALIVE, 3));
    }

    @Test
    void aliveCellWithOneNeighborDies() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.ALIVE, 1));
    }

    @Test
    void aliveCellWithFourNeighborsDies() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.ALIVE, 4));
    }

    @Test
    void deadCellWithThreeNeighborsIsBorn() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.DEAD, 3));
    }

    @Test
    void deadCellWithTwoNeighborsStaysDead() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.DEAD, 2));
    }
}
