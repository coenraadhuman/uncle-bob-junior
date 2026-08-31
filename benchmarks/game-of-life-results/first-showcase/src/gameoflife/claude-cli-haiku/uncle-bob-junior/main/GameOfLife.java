// GameOfLife.java
public class GameOfLife {
    private static final int SURVIVAL_MIN_NEIGHBORS = 2;
    private static final int SURVIVAL_MAX_NEIGHBORS = 3;
    private static final int BIRTH_NEIGHBOR_COUNT = 3;

    public Grid nextGeneration(Grid current) {
        Grid next = new Grid(current.getWidth(), current.getHeight());

        for (int row = 0; row < current.getHeight(); row++) {
            for (int col = 0; col < current.getWidth(); col++) {
                int neighbors = current.countLiveNeighbors(row, col);
                boolean alive = current.isAlive(row, col);
                boolean survives = cellSurvives(alive, neighbors);
                next.setCell(row, col, survives);
            }
        }

        return next;
    }

    private boolean cellSurvives(boolean alive, int neighbors) {
        if (alive) {
            return neighbors >= SURVIVAL_MIN_NEIGHBORS && neighbors <= SURVIVAL_MAX_NEIGHBORS;
        }
        return neighbors == BIRTH_NEIGHBOR_COUNT;
    }
}
