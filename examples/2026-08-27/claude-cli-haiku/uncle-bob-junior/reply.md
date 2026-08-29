I'll create a complete Conway's Game of Life implementation with clean code structure and tests.

**Assumptions:**
- Grid size: 50×20 (width×height) for readable terminal display
- Initial state: ~30% random cells alive
- Frame rate: 200ms per generation
- Topology: Wrapping edges (toroidal, so patterns don't just die out)
- Display: █ for alive, · for dead
- Terminal supports ANSI escape codes (standard on modern systems)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>conway-game-of-life</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Conway's Game of Life</name>

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
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>GameOfLife</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
public class GameOfLife {
    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = 20;
    private static final long GENERATION_DELAY_MS = 200;
    private static final double INITIAL_ALIVE_RATIO = 0.3;

    private final Generation generation;
    private final int width;
    private final int height;

    public GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.generation = Generation.randomInitial(width, height, INITIAL_ALIVE_RATIO);
    }

    public void run(long delayMs) throws InterruptedException {
        while (true) {
            render();
            Thread.sleep(delayMs);
            generation.advance();
        }
    }

    private void render() {
        System.out.print("\u001b[2J\u001b[H");
        System.out.flush();

        StringBuilder sb = new StringBuilder();
        boolean[][] grid = generation.getGrid();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(grid[y][x] ? '█' : '·');
            }
            sb.append('\n');
        }

        System.out.print(sb.toString());
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        GameOfLife game = new GameOfLife(GRID_WIDTH, GRID_HEIGHT);
        game.run(GENERATION_DELAY_MS);
    }
}
```

```java
public class Generation {
    private boolean[][] grid;
    private final int width;
    private final int height;

    public Generation(boolean[][] initialGrid) {
        this.height = initialGrid.length;
        this.width = initialGrid[0].length;
        this.grid = copyGrid(initialGrid);
    }

    public static Generation randomInitial(int width, int height, double livingRatio) {
        boolean[][] grid = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = Math.random() < livingRatio;
            }
        }
        return new Generation(grid);
    }

    public void advance() {
        boolean[][] next = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countNeighbors(x, y);
                next[y][x] = willSurvive(grid[y][x], neighbors);
            }
        }

        this.grid = next;
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = (x + dx + width) % width;
                int ny = (y + dy + height) % height;

                if (grid[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean willSurvive(boolean alive, int neighbors) {
        if (alive) {
            return neighbors == 2 || neighbors == 3;
        }
        return neighbors == 3;
    }

    public boolean[][] getGrid() {
        return copyGrid(grid);
    }

    private static boolean[][] copyGrid(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }
}
```

```java
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
```

**To run:**
```bash
mvn clean compile exec:java
```

Or build and execute directly:
```bash
mvn clean package
java -cp target/conway-game-of-life-1.0.0.jar GameOfLife
```

The grid updates in place with no scrolling. Press Ctrl+C to exit. Core logic is pure; state mutation happens at the game loop edges. Tests verify birth, survival, and death rules.