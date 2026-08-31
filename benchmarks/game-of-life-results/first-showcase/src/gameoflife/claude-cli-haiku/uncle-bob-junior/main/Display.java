// Display.java
public class Display {
    private static final String LIVE_CELL = "█";
    private static final String DEAD_CELL = " ";
    private static final String CLEAR_SCREEN = "\u001b[2J";
    private static final String HOME_CURSOR = "\u001b[H";

    public void render(Grid grid, int generation) {
        System.out.print(CLEAR_SCREEN + HOME_CURSOR);
        System.out.println("Generation: " + generation);

        for (int row = 0; row < grid.getHeight(); row++) {
            for (int col = 0; col < grid.getWidth(); col++) {
                String cell = grid.isAlive(row, col) ? LIVE_CELL : DEAD_CELL;
                System.out.print(cell);
            }
            System.out.println();
        }
    }

    public void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
