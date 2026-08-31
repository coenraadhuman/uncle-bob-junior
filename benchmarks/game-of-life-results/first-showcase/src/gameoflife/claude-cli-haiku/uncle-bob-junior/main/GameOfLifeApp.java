// GameOfLifeApp.java
public class GameOfLifeApp {
    private static final int GRID_WIDTH = 80;
    private static final int GRID_HEIGHT = 24;
    private static final long GENERATION_DELAY_MS = 100;
    private static final int TOTAL_GENERATIONS = 1000;

    public static void main(String[] args) {
        Grid grid = initializeGrid();
        GameOfLife game = new GameOfLife();
        Display display = new Display();

        for (int generation = 0; generation < TOTAL_GENERATIONS; generation++) {
            display.render(grid, generation);
            grid = game.nextGeneration(grid);
            display.pause(GENERATION_DELAY_MS);
        }
    }

    private static Grid initializeGrid() {
        Grid grid = new Grid(GRID_WIDTH, GRID_HEIGHT);
        int centerRow = GRID_HEIGHT / 2;
        int centerCol = GRID_WIDTH / 2;

        addGlider(grid, centerRow, centerCol);

        return grid;
    }

    private static void addGlider(Grid grid, int row, int col) {
        grid.setCell(row, col + 1, true);
        grid.setCell(row + 1, col + 2, true);
        grid.setCell(row + 2, col, true);
        grid.setCell(row + 2, col + 1, true);
        grid.setCell(row + 2, col + 2, true);
    }
}
