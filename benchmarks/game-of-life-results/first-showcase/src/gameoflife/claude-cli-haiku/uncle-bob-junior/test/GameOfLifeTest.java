// GameOfLifeTest.java
import org.junit.Test;
import static org.junit.Assert.*;

public class GameOfLifeTest {

    @Test
    public void testEmptyGridStaysEmpty() {
        Grid grid = new Grid(5, 5);
        GameOfLife game = new GameOfLife();

        Grid next = game.nextGeneration(grid);

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                assertFalse(next.isAlive(row, col));
            }
        }
    }

    @Test
    public void testLiveCellWithOneNeighborDies() {
        Grid grid = new Grid(3, 3);
        grid.setCell(1, 1, true);
        grid.setCell(0, 0, true);

        GameOfLife game = new GameOfLife();
        Grid next = game.nextGeneration(grid);

        assertFalse(next.isAlive(1, 1));
    }

    @Test
    public void testLiveCellWithTwoNeighborsSurvives() {
        Grid grid = new Grid(3, 3);
        grid.setCell(0, 0, true);
        grid.setCell(0, 1, true);
        grid.setCell(1, 0, true);

        GameOfLife game = new GameOfLife();
        Grid next = game.nextGeneration(grid);

        assertTrue(next.isAlive(0, 0));
    }

    @Test
    public void testDeadCellWithThreeNeighborsBecomeAlive() {
        Grid grid = new Grid(3, 3);
        grid.setCell(0, 0, true);
        grid.setCell(0, 1, true);
        grid.setCell(1, 0, true);

        GameOfLife game = new GameOfLife();
        Grid next = game.nextGeneration(grid);

        assertTrue(next.isAlive(1, 1));
    }

    @Test
    public void testCountLiveNeighborsFromCorner() {
        Grid grid = new Grid(3, 3);
        grid.setCell(0, 0, true);
        grid.setCell(0, 1, true);
        grid.setCell(1, 1, true);

        assertEquals(3, grid.countLiveNeighbors(1, 0));
    }

    @Test
    public void testBoundaryDoesNotWrap() {
        Grid grid = new Grid(3, 3);
        grid.setCell(0, 0, true);

        assertEquals(0, grid.countLiveNeighbors(-1, -1));
    }
}
