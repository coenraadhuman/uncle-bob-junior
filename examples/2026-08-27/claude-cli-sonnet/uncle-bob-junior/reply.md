Assumptions: 40×20 toroidal (wrap-around) grid, 25% random initial density, 200 generations, ~150ms per frame, ANSI escape codes for in-place redraw (assumes an ANSI-compatible terminal), Java 17, JUnit 5 for tests.

**pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.plg</groupId>
    <artifactId>game-of-life</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <junit.version>5.10.2</junit.version>
        <exec.mainClass>com.plg.gameoflife.GameOfLifeSimulation</exec.mainClass>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.2.5</version>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <mainClass>${exec.mainClass}</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**src/main/java/com/plg/gameoflife/Grid.java**

```java
package com.plg.gameoflife;

import java.util.Arrays;
import java.util.Objects;
import java.util.random.RandomGenerator;

public final class Grid {

    private static final int MIN_SURVIVAL_NEIGHBORS = 2;
    private static final int MAX_SURVIVAL_NEIGHBORS = 3;
    private static final int BIRTH_NEIGHBORS = 3;

    private final boolean[][] cells;
    private final int rowCount;
    private final int columnCount;

    public Grid(boolean[][] cells) {
        Objects.requireNonNull(cells, "cells must not be null");
        this.cells = deepCopy(cells);
        this.rowCount = cells.length;
        this.columnCount = rowCount == 0 ? 0 : cells[0].length;
    }

    public static Grid random(int rowCount, int columnCount, double aliveProbability, RandomGenerator random) {
        boolean[][] seeded = new boolean[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            seedRow(seeded[row], aliveProbability, random);
        }
        return new Grid(seeded);
    }

    private static void seedRow(boolean[] row, double aliveProbability, RandomGenerator random) {
        for (int column = 0; column < row.length; column++) {
            row[column] = random.nextDouble() < aliveProbability;
        }
    }

    public boolean isAlive(int row, int column) {
        int wrappedRow = Math.floorMod(row, rowCount);
        int wrappedColumn = Math.floorMod(column, columnCount);
        return cells[wrappedRow][wrappedColumn];
    }

    public int rowCount() {
        return rowCount;
    }

    public int columnCount() {
        return columnCount;
    }

    public Grid next() {
        boolean[][] nextCells = new boolean[rowCount][columnCount];
        for (int row = 0; row < rowCount; row++) {
            computeNextRow(nextCells[row], row);
        }
        return new Grid(nextCells);
    }

    private void computeNextRow(boolean[] nextRow, int row) {
        for (int column = 0; column < columnCount; column++) {
            nextRow[column] = nextState(row, column);
        }
    }

    private boolean nextState(int row, int column) {
        int liveNeighbors = countLiveNeighbors(row, column);
        if (isAlive(row, column)) {
            return liveNeighbors == MIN_SURVIVAL_NEIGHBORS || liveNeighbors == MAX_SURVIVAL_NEIGHBORS;
        }
        return liveNeighbors == BIRTH_NEIGHBORS;
    }

    private int countLiveNeighbors(int row, int column) {
        int count = 0;
        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int columnOffset = -1; columnOffset <= 1; columnOffset++) {
                if (rowOffset == 0 && columnOffset == 0) {
                    continue;
                }
                count += isAlive(row + rowOffset, column + columnOffset) ? 1 : 0;
            }
        }
        return count;
    }

    private static boolean[][] deepCopy(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][];
        for (int row = 0; row < source.length; row++) {
            copy[row] = source[row].clone();
        }
        return copy;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid otherGrid)) {
            return false;
        }
        return Arrays.deepEquals(cells, otherGrid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
```

**src/main/java/com/plg/gameoflife/GridRenderer.java**

```java
package com.plg.gameoflife;

public final class GridRenderer {

    private static final char ALIVE_CELL_SYMBOL = '#';
    private static final char DEAD_CELL_SYMBOL = ' ';

    public String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.rowCount(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int column = 0; column < grid.columnCount(); column++) {
            frame.append(grid.isAlive(row, column) ? ALIVE_CELL_SYMBOL : DEAD_CELL_SYMBOL);
        }
        frame.append(System.lineSeparator());
    }
}
```

**src/main/java/com/plg/gameoflife/TerminalScreen.java**

```java
package com.plg.gameoflife;

import java.io.PrintStream;

/** Redraws frames in place using ANSI escape codes; requires an ANSI-compatible terminal. */
public final class TerminalScreen {

    private static final String ANSI_CLEAR_SCREEN = "\u001b[2J";
    private static final String ANSI_CURSOR_HOME = "\u001b[H";
    private static final String ANSI_HIDE_CURSOR = "\u001b[?25l";
    private static final String ANSI_SHOW_CURSOR = "\u001b[?25h";

    private final PrintStream out;

    public TerminalScreen(PrintStream out) {
        this.out = out;
    }

    public void open() {
        out.print(ANSI_CLEAR_SCREEN);
        out.print(ANSI_HIDE_CURSOR);
        out.flush();
    }

    public void draw(String frame) {
        out.print(ANSI_CURSOR_HOME);
        out.print(frame);
        out.flush();
    }

    public void close() {
        out.print(ANSI_SHOW_CURSOR);
        out.flush();
    }
}
```

**src/main/java/com/plg/gameoflife/GameOfLifeSimulation.java**

```java
package com.plg.gameoflife;

import java.util.random.RandomGenerator;

public final class GameOfLifeSimulation {

    private static final int GRID_ROW_COUNT = 20;
    private static final int GRID_COLUMN_COUNT = 40;
    private static final double INITIAL_ALIVE_PROBABILITY = 0.25;
    private static final int GENERATION_COUNT = 200;
    private static final long FRAME_DELAY_MILLISECONDS = 150L;

    public static void main(String[] args) throws InterruptedException {
        Grid initialGrid = Grid.random(
                GRID_ROW_COUNT, GRID_COLUMN_COUNT, INITIAL_ALIVE_PROBABILITY, RandomGenerator.getDefault());
        GridRenderer renderer = new GridRenderer();
        TerminalScreen screen = new TerminalScreen(System.out);

        screen.open();
        try {
            runGenerations(initialGrid, renderer, screen);
        } finally {
            screen.close();
        }
    }

    private static void runGenerations(Grid grid, GridRenderer renderer, TerminalScreen screen)
            throws InterruptedException {
        Grid currentGrid = grid;
        for (int generation = 0; generation < GENERATION_COUNT; generation++) {
            screen.draw(renderer.render(currentGrid));
            currentGrid = currentGrid.next();
            Thread.sleep(FRAME_DELAY_MILLISECONDS);
        }
    }
}
```

**src/test/java/com/plg/gameoflife/GridTest.java**

```java
package com.plg.gameoflife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    @Test
    void blockStillLifeRemainsUnchanged() {
        boolean[][] cells = {
                {false, false, false, false, false, false},
                {false, false, false, false, false, false},
                {false, false, true, true, false, false},
                {false, false, true, true, false, false},
                {false, false, false, false, false, false},
                {false, false, false, false, false, false},
        };
        Grid grid = new Grid(cells);

        assertEquals(grid, grid.next());
    }

    @Test
    void verticalBlinkerBecomesHorizontalBlinker() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false},
        };
        boolean[][] expected = {
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertEquals(new Grid(expected), next);
    }

    @Test
    void isolatedLiveCellDiesFromUnderpopulation() {
        boolean[][] cells = {
                {false, false, false},
                {false, true, false},
                {false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertFalse(next.isAlive(1, 1));
    }

    @Test
    void liveCellWithFourNeighborsDiesFromOverpopulation() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, true, true, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertFalse(next.isAlive(2, 2));
    }

    @Test
    void deadCellWithThreeNeighborsIsBorn() {
        boolean[][] cells = {
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, false, false, false, false},
        };

        Grid next = new Grid(cells).next();

        assertTrue(next.isAlive(2, 2));
    }

    @Test
    void wrapsCoordinatesToOppositeEdge() {
        boolean[][] cells = {
                {true, false},
                {false, false},
        };
        Grid grid = new Grid(cells);

        assertTrue(grid.isAlive(-2, -2));
        assertTrue(grid.isAlive(2, 2));
        assertFalse(grid.isAlive(-1, -1));
    }
}
```

**src/test/java/com/plg/gameoflife/GridRendererTest.java**

```java
package com.plg.gameoflife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridRendererTest {

    @Test
    void rendersAliveCellsAsHashAndDeadCellsAsSpace() {
        boolean[][] cells = {
                {true, false},
                {false, true},
        };
        Grid grid = new Grid(cells);
        String expected = "#" + " " + System.lineSeparator()
                + " " + "#" + System.lineSeparator();

        String frame = new GridRenderer().render(grid);

        assertEquals(expected, frame);
    }
}
```

`Grid` → cleaned: immutable with named rule constants and guard-clause `nextState`, safe to change because behavior is verified by still-life, oscillator, birth/death, and wrap tests. `TerminalScreen`/`GridRenderer` → cleaned: single-purpose classes with named ANSI/symbol constants, safe to change because rendering and terminal control are decoupled from simulation rules.