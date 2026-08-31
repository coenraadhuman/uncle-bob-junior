I'll create a clean, modular Conway's Game of Life implementation in Java with Maven. The solution uses ANSI escape codes to keep the display in the same terminal position and demonstrates the key patterns with a glider.

**Assumptions:**
- 80x24 grid (standard terminal size)
- 100ms delay between generations
- Glider pattern initialization for visual interest
- Unix/Linux/macOS terminal with ANSI code support

Here's the complete solution:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>game-of-life</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>GameOfLifeApp</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
// Grid.java
public class Grid {
    private boolean[][] cells;
    private int width;
    private int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
    }

    public void setCell(int row, int col, boolean alive) {
        cells[row][col] = alive;
    }

    public boolean isAlive(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            return false;
        }
        return cells[row][col];
    }

    public int countLiveNeighbors(int row, int col) {
        int count = 0;
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                if (isAlive(row + dRow, col + dCol)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Grid copy() {
        Grid copy = new Grid(width, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                copy.setCell(row, col, cells[row][col]);
            }
        }
        return copy;
    }
}
```

```java
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
```

```java
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
```

```java
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
```

```java
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
```

**To run:**
```bash
mvn clean compile exec:java -Dexec.mainClass=GameOfLifeApp
```

Or build and run the JAR:
```bash
mvn clean package
java -jar target/game-of-life-1.0.0.jar
```

**Code structure:** Grid holds cell state and neighbor counts. GameOfLife applies Conway's rules (survive with 2-3 neighbors, birth with exactly 3). Display renders to stdout with ANSI codes for same-position updates. Main loop updates generations with a glider pattern.