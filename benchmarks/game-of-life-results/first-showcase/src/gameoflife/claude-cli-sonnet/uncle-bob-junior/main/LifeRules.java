// src/main/java/life/LifeRules.java
package life;

final class LifeRules {
    private static final int MIN_SURVIVAL_NEIGHBORS = 2;
    private static final int MAX_SURVIVAL_NEIGHBORS = 3;
    private static final int BIRTH_NEIGHBOR_COUNT = 3;

    private LifeRules() {
    }

    static CellState nextState(CellState current, int aliveNeighbors) {
        if (current == CellState.ALIVE) {
            return survives(aliveNeighbors) ? CellState.ALIVE : CellState.DEAD;
        }
        return isBirth(aliveNeighbors) ? CellState.ALIVE : CellState.DEAD;
    }

    private static boolean survives(int aliveNeighbors) {
        return aliveNeighbors == MIN_SURVIVAL_NEIGHBORS || aliveNeighbors == MAX_SURVIVAL_NEIGHBORS;
    }

    private static boolean isBirth(int aliveNeighbors) {
        return aliveNeighbors == BIRTH_NEIGHBOR_COUNT;
    }
}
