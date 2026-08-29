Assumptions: 40×20 toroidal grid (edges wrap), 35% random initial density, runs 300 generations at 150 ms/frame using ANSI escape codes to reposition the cursor (works in any ANSI-capable terminal), Java 17, JUnit 5 for tests.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>game-of-life</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.jupiter.version>5.10.2</junit.jupiter.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.jupiter.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <finalName>game-of-life</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>life.GameOfLifeApplication</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <mainClass>life.GameOfLifeApplication</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
// src/main/java/life/CellState.java
package life;

public enum CellState {
    ALIVE,
    DEAD
}
```

```java
// src/main/java/life/Grid.java
package life;

import java.util.Arrays;
import java.util.Objects;

public final class Grid {
    private final CellState[][] cells;
    private final int height;
    private final int width;

    Grid(CellState[][] cells) {
        Objects.requireNonNull(cells, "cells must not be null");
        this.cells = deepCopy(cells);
        this.height = cells.length;
        this.width = cells[0].length;
    }

    private static CellState[][] deepCopy(CellState[][] source) {
        CellState[][] copy = new CellState[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    public CellState cellAt(int row, int col) {
        return cells[row][col];
    }

    public int height() {
        return height;
    }

    public int width() {
        return width;
    }

    public Grid nextGeneration() {
        return new Grid(computeNextCells());
    }

    private CellState[][] computeNextCells() {
        CellState[][] nextCells = new CellState[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                nextCells[row][col] = nextStateForCell(row, col);
            }
        }
        return nextCells;
    }

    private CellState nextStateForCell(int row, int col) {
        int aliveNeighbors = NeighborCounter.countAliveNeighbors(this, row, col);
        return LifeRules.nextState(cellAt(row, col), aliveNeighbors);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid otherGrid)) {
            return false;
        }
        return Arrays.deepEquals(this.cells, otherGrid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
```

```java
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
```

```java
// src/main/java/life/NeighborCounter.java
package life;

final class NeighborCounter {
    private static final int[][] NEIGHBOR_OFFSETS = {
        {-1, -1}, {-1, 0}, {-1, 1},
        {0, -1},           {0, 1},
        {1, -1},  {1, 0},  {1, 1}
    };

    private NeighborCounter() {
    }

    static int countAliveNeighbors(Grid grid, int row, int col) {
        int aliveCount = 0;
        for (int[] offset : NEIGHBOR_OFFSETS) {
            if (isNeighborAlive(grid, row, col, offset)) {
                aliveCount++;
            }
        }
        return aliveCount;
    }

    private static boolean isNeighborAlive(Grid grid, int row, int col, int[] offset) {
        int neighborRow = wrapIndex(row + offset[0], grid.height());
        int neighborCol = wrapIndex(col + offset[1], grid.width());
        return grid.cellAt(neighborRow, neighborCol) == CellState.ALIVE;
    }

    private static int wrapIndex(int index, int size) {
        return ((index % size) + size) % size;
    }
}
```

```java
// src/main/java/life/GridFactory.java
package life;

import java.util.Random;

public final class GridFactory {
    private GridFactory() {
    }

    public static Grid randomGrid(int width, int height, double aliveDensity, Random random) {
        CellState[][] cells = new CellState[height][width];
        for (int row = 0; row < height; row++) {
            fillRow(cells[row], aliveDensity, random);
        }
        return new Grid(cells);
    }

    private static void fillRow(CellState[] row, double aliveDensity, Random random) {
        for (int col = 0; col < row.length; col++) {
            row[col] = random.nextDouble() < aliveDensity ? CellState.ALIVE : CellState.DEAD;
        }
    }
}
```

```java
// src/main/java/life/TerminalRenderer.java
package life;

public final class TerminalRenderer {
    private static final char ALIVE_SYMBOL = 'O';
    private static final char DEAD_SYMBOL = ' ';
    private static final String CURSOR_HOME = "\u001b[H";
    private static final String CLEAR_SCREEN = "\u001b[2J";
    private static final int GENERATION_LABEL_WIDTH = 6;

    public void clearScreen() {
        System.out.print(CLEAR_SCREEN + CURSOR_HOME);
        System.out.flush();
    }

    public void render(Grid grid, int generation) {
        StringBuilder frame = new StringBuilder();
        frame.append(CURSOR_HOME);
        appendGrid(frame, grid);
        appendStatusLine(frame, generation);
        System.out.print(frame);
        System.out.flush();
    }

    private void appendGrid(StringBuilder frame, Grid grid) {
        for (int row = 0; row < grid.height(); row++) {
            appendRow(frame, grid, row);
        }
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int col = 0; col < grid.width(); col++) {
            frame.append(grid.cellAt(row, col) == CellState.ALIVE ? ALIVE_SYMBOL : DEAD_SYMBOL);
        }
        frame.append('\n');
    }

    private void appendStatusLine(StringBuilder frame, int generation) {
        frame.append(String.format("Generation: %-" + GENERATION_LABEL_WIDTH + "d%n", generation));
    }
}
```

```java
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
```

```java
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
```

```java
// src/test/java/life/TestGridSupport.java
package life;

final class TestGridSupport {
    private static final char ALIVE_CHAR = 'O';

    private TestGridSupport() {
    }

    static Grid gridFrom(String... rows) {
        CellState[][] cells = new CellState[rows.length][];
        for (int row = 0; row < rows.length; row++) {
            cells[row] = parseRow(rows[row]);
        }
        return new Grid(cells);
    }

    private static CellState[] parseRow(String row) {
        CellState[] cells = new CellState[row.length()];
        for (int col = 0; col < row.length(); col++) {
            cells[col] = row.charAt(col) == ALIVE_CHAR ? CellState.ALIVE : CellState.DEAD;
        }
        return cells;
    }
}
```

```java
// src/test/java/life/LifeRulesTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LifeRulesTest {

    @Test
    void aliveCellWithTwoNeighborsSurvives() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.ALIVE, 2));
    }

    @Test
    void aliveCellWithThreeNeighborsSurvives() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.ALIVE, 3));
    }

    @Test
    void aliveCellWithOneNeighborDies() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.ALIVE, 1));
    }

    @Test
    void aliveCellWithFourNeighborsDies() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.ALIVE, 4));
    }

    @Test
    void deadCellWithThreeNeighborsIsBorn() {
        assertEquals(CellState.ALIVE, LifeRules.nextState(CellState.DEAD, 3));
    }

    @Test
    void deadCellWithTwoNeighborsStaysDead() {
        assertEquals(CellState.DEAD, LifeRules.nextState(CellState.DEAD, 2));
    }
}
```

```java
// src/test/java/life/NeighborCounterTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NeighborCounterTest {

    @Test
    void countsAllEightNeighborsWhenAllAreAlive() {
        Grid grid = TestGridSupport.gridFrom(
            "OOO",
            "O.O",
            "OOO"
        );
        assertEquals(8, NeighborCounter.countAliveNeighbors(grid, 1, 1));
    }

    @Test
    void countsZeroNeighborsWhenAllAreDead() {
        Grid grid = TestGridSupport.gridFrom(
            "...",
            "...",
            "..."
        );
        assertEquals(0, NeighborCounter.countAliveNeighbors(grid, 1, 1));
    }

    @Test
    void wrapsAroundGridEdgesToCountDiagonalNeighbor() {
        Grid grid = TestGridSupport.gridFrom(
            "O..",
            "...",
            "..O"
        );
        assertEquals(1, NeighborCounter.countAliveNeighbors(grid, 0, 0));
    }
}
```

```java
// src/test/java/life/GridTest.java
package life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridTest {

    @Test
    void blockStillLifeRemainsStable() {
        Grid block = TestGridSupport.gridFrom(
            "......",
            ".OO...",
            ".OO...",
            "......"
        );
        assertEquals(block, block.nextGeneration());
    }

    @Test
    void blinkerOscillatesWithPeriodTwo() {
        Grid verticalBlinker = TestGridSupport.gridFrom(
            ".....",
            "..O..",
            "..O..",
            "..O..",
            "....."
        );
        Grid horizontalBlinker = TestGridSupport.gridFrom(
            ".....",
            ".....",
            ".OOO.",
            ".....",
            "....."
        );
        assertEquals(horizontalBlinker, verticalBlinker.nextGeneration());
        assertEquals(verticalBlinker, verticalBlinker.nextGeneration().nextGeneration());
    }

    @Test
    void isolatedAliveCellDiesFromUnderpopulation() {
        Grid singleCell = TestGridSupport.gridFrom(
            "...",
            ".O.",
            "..."
        );
        Grid expected = TestGridSupport.gridFrom(
            "...",
            "...",
            "..."
        );
        assertEquals(expected, singleCell.nextGeneration());
    }
}
```

```java
// src/test/java/life/GridFactoryTest.java
package life;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridFactoryTest {
    private static final int TEST_GRID_SIZE = 3;

    @Test
    void allCellsDeadWhenDensityIsZero() {
        Grid grid = GridFactory.randomGrid(TEST_GRID_SIZE, TEST_GRID_SIZE, 0.0, new Random());
        assertAllCellsAre(grid, CellState.DEAD);
    }

    @Test
    void allCellsAliveWhenDensityIsOne() {
        Grid grid = GridFactory.randomGrid(TEST_GRID_SIZE, TEST_GRID_SIZE, 1.0, new Random());
        assertAllCellsAre(grid, CellState.ALIVE);
    }

    private void assertAllCellsAre(Grid grid, CellState expectedState) {
        for (int row = 0; row < grid.height(); row++) {
            for (int col = 0; col < grid.width(); col++) {
                assertEquals(expectedState, grid.cellAt(row, col));
            }
        }
    }
}
```

Run with `mvn package && java -jar target/game-of-life.jar` or `mvn exec:java`; tests run via `mvn test`.

`Grid`/`LifeRules`/`NeighborCounter` → cleaned: pure functional core with a toroidal-wrap rule engine, unit-tested against known still-life and oscillator patterns; `TerminalRenderer`/`SimulationRunner` isolate the only side effects (stdout, sleep), so the simulation logic stays safe to change and test without a terminal.