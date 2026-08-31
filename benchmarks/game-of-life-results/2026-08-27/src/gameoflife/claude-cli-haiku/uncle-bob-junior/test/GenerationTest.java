import org.junit.Test;
import static org.junit.Assert.*;

public class GenerationTest {
    @Test
    public void testBirthFromThreeNeighbors() {
        boolean[][] grid = new boolean[3][3];
        grid[0][0] = true;
        grid[0][1] = true;
        grid[1][0] = true;

        Generation gen = new Generation(grid);
        gen.advance();
        boolean[][] result = gen.getGrid();

        assertTrue("Dead cell with 3 neighbors should be born", result[1][1]);
    }

    @Test
    public void testDeathByUnderpopulation() {
        boolean[][] grid = new boolean[3][3];
        grid[1][1] = true;

        Generation gen = new Generation(grid);
        gen.advance();
        boolean[][] result = gen.getGrid();

        assertFalse("Live cell with < 2 neighbors dies", result[1][1]);
    }

    @Test
    public void testSurvivalWithTwoNeighbors() {
        boolean[][] grid = new boolean[3][3];
        grid[0][0] = true;
        grid[0][1] = true;
        grid[1][1] = true;

        Generation gen = new Generation(grid);
        gen.advance();
        boolean[][] result = gen.getGrid();

        assertTrue("Live cell with 2 neighbors survives", result[1][1]);
    }

    @Test
    public void testSurvivalWithThreeNeighbors() {
        boolean[][] grid = new boolean[3][3];
        grid[0][0] = true;
        grid[0][1] = true;
        grid[0][2] = true;
        grid[1][1] = true;

        Generation gen = new Generation(grid);
        gen.advance();
        boolean[][] result = gen.getGrid();

        assertTrue("Live cell with 3 neighbors survives", result[0][1]);
    }

    @Test
    public void testDeathByOvercrowding() {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grid[i][j] = true;
            }
        }

        Generation gen = new Generation(grid);
        gen.advance();
        boolean[][] result = gen.getGrid();

        assertFalse("Live cell with > 3 neighbors dies", result[1][1]);
    }
}
