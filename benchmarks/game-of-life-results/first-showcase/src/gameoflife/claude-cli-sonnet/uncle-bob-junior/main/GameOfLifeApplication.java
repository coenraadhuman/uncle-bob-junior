// src/main/java/life/GameOfLifeApplication.java
package life;

import java.util.Random;

public final class GameOfLifeApplication {
    private static final int GRID_WIDTH = 40;
    private static final int GRID_HEIGHT = 20;
    private static final double INITIAL_ALIVE_DENSITY = 0.35;
    private static final int GENERATION_COUNT = 300;
    private static final long FRAME_DELAY_MILLIS = 150L;

    private GameOfLifeApplication() {
    }

    public static void main(String[] args) {
        Grid initialGrid = GridFactory.randomGrid(GRID_WIDTH, GRID_HEIGHT, INITIAL_ALIVE_DENSITY, new Random());
        SimulationRunner simulationRunner = new SimulationRunner(new TerminalRenderer(), FRAME_DELAY_MILLIS);
        simulationRunner.run(initialGrid, GENERATION_COUNT);
    }
}
