// src/main/java/life/SimulationRunner.java
package life;

public final class SimulationRunner {
    private final TerminalRenderer renderer;
    private final long frameDelayMillis;

    public SimulationRunner(TerminalRenderer renderer, long frameDelayMillis) {
        this.renderer = renderer;
        this.frameDelayMillis = frameDelayMillis;
    }

    public void run(Grid initialGrid, int generationCount) {
        renderer.clearScreen();
        Grid currentGrid = initialGrid;
        for (int generation = 1; generation <= generationCount; generation++) {
            currentGrid = renderFrameAndAdvance(currentGrid, generation);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    private Grid renderFrameAndAdvance(Grid currentGrid, int generation) {
        renderer.render(currentGrid, generation);
        Grid nextGrid = currentGrid.nextGeneration();
        sleepBetweenFrames();
        return nextGrid;
    }

    private void sleepBetweenFrames() {
        try {
            Thread.sleep(frameDelayMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
