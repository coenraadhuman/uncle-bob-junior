Assumptions: bounded grid (cells beyond the edge are dead), fixed 24x60 board seeded with a glider, blinker, and block, runs until Ctrl+C, and the terminal supports ANSI escape codes (any modern terminal does). The frame is redrawn in place with cursor-home escapes, so nothing scrolls.

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
        <maven.compiler.release>17</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.2</version>
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
                    <mainClass>com.example.life.GameOfLifeApp</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

`src/main/java/com/example/life/Grid.java` — the functional core, fully immutable:

```java
package com.example.life;

/**
 * Immutable bounded Game of Life grid. Cells outside the bounds are
 * permanently dead. Each generation is a new Grid; instances never change.
 */
public final class Grid {

    private static final int NEIGHBOURS_FOR_BIRTH = 3;
    private static final int MIN_NEIGHBOURS_TO_SURVIVE = 2;
    private static final int MAX_NEIGHBOURS_TO_SURVIVE = 3;

    private final boolean[][] cells;

    private Grid(boolean[][] cells) {
        this.cells = cells;
    }

    /** Creates a grid with the given {row, column} cells alive. */
    public static Grid withLiveCells(int rows, int columns, int[][] liveCells) {
        if (rows <= 0 || columns <= 0) {
            throw new IllegalArgumentException("Grid dimensions must be positive");
        }
        boolean[][] cells = new boolean[rows][columns];
        for (int[] cell : liveCells) {
            cells[cell[0]][cell[1]] = true;
        }
        return new Grid(cells);
    }

    public int rows() {
        return cells.length;
    }

    public int columns() {
        return cells[0].length;
    }

    /** Out-of-bounds coordinates are dead, never an error. */
    public boolean isAlive(int row, int column) {
        boolean outOfBounds = row < 0 || row >= rows() || column < 0 || column >= columns();
        return !outOfBounds && cells[row][column];
    }

    public Grid nextGeneration() {
        boolean[][] next = new boolean[rows()][columns()];
        for (int row = 0; row < rows(); row++) {
            for (int column = 0; column < columns(); column++) {
                next[row][column] = nextCellState(isAlive(row, column), liveNeighbours(row, column));
            }
        }
        return new Grid(next);
    }

    private int liveNeighbours(int row, int column) {
        int count = 0;
        for (int deltaRow = -1; deltaRow <= 1; deltaRow++) {
            for (int deltaColumn = -1; deltaColumn <= 1; deltaColumn++) {
                count += isNeighbourAlive(row, column, deltaRow, deltaColumn) ? 1 : 0;
            }
        }
        return count;
    }

    private boolean isNeighbourAlive(int row, int column, int deltaRow, int deltaColumn) {
        boolean isSelf = deltaRow == 0 && deltaColumn == 0;
        return !isSelf && isAlive(row + deltaRow, column + deltaColumn);
    }

    private static boolean nextCellState(boolean alive, int liveNeighbours) {
        if (alive) {
            return liveNeighbours >= MIN_NEIGHBOURS_TO_SURVIVE
                && liveNeighbours <= MAX_NEIGHBOURS_TO_SURVIVE;
        }
        return liveNeighbours == NEIGHBOURS_FOR_BIRTH;
    }
}
```

`src/main/java/com/example/life/GridRenderer.java` — pure grid-to-text, no I/O:

```java
package com.example.life;

/** Renders a Grid as a block of text, one line per row. Pure function, no I/O. */
public final class GridRenderer {

    private static final char ALIVE_GLYPH = '\u2588'; // █
    private static final char DEAD_GLYPH = '\u00B7';  // ·

    private GridRenderer() {
    }

    public static String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.rows(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private static void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int column = 0; column < grid.columns(); column++) {
            frame.append(grid.isAlive(row, column) ? ALIVE_GLYPH : DEAD_GLYPH);
        }
        frame.append('\n');
    }
}
```

`src/main/java/com/example/life/GameOfLifeApp.java` — the imperative shell:

```java
package com.example.life;

/**
 * Terminal front end. Clears the screen once, then redraws every frame from
 * the cursor-home position so the UI stays fixed in place with no scrolling.
 */
public final class GameOfLifeApp {

    private static final int ROWS = 24;
    private static final int COLUMNS = 60;
    private static final long FRAME_DELAY_MILLIS = 150;

    private static final String CLEAR_SCREEN = "\u001B[2J";
    private static final String CURSOR_HOME = "\u001B[H";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";

    private static final int[][] SEED = {
        {1, 2}, {2, 3}, {3, 1}, {3, 2}, {3, 3},   // glider
        {10, 28}, {10, 29}, {10, 30},             // blinker
        {5, 45}, {5, 46}, {6, 45}, {6, 46}        // block
    };

    private GameOfLifeApp() {
    }

    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.print(SHOW_CURSOR)));
        System.out.print(CLEAR_SCREEN + HIDE_CURSOR);
        runForever(Grid.withLiveCells(ROWS, COLUMNS, SEED));
    }

    private static void runForever(Grid grid) throws InterruptedException {
        long generation = 0;
        while (true) {
            drawFrame(grid, generation);
            Thread.sleep(FRAME_DELAY_MILLIS);
            grid = grid.nextGeneration();
            generation++;
        }
    }

    private static void drawFrame(Grid grid, long generation) {
        String statusLine = "Generation " + generation + "  (Ctrl+C to quit)";
        System.out.print(CURSOR_HOME + GridRenderer.render(grid) + statusLine);
        System.out.flush();
    }
}
```

`src/test/java/com/example/life/GridTest.java`:

```java
package com.example.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    private static final int[][] NO_LIVE_CELLS = {};

    @Test
    void lonelyCellDiesOfUnderpopulation() {
        Grid grid = Grid.withLiveCells(3, 3, new int[][] {{1, 1}});
        assertFalse(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void cellWithFourNeighboursDiesOfOvercrowding() {
        Grid grid = Grid.withLiveCells(3, 3,
            new int[][] {{1, 1}, {0, 0}, {0, 2}, {2, 0}, {2, 2}});
        assertFalse(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void deadCellWithExactlyThreeNeighboursComesToLife() {
        Grid grid = Grid.withLiveCells(3, 3, new int[][] {{0, 0}, {0, 1}, {0, 2}});
        assertTrue(grid.nextGeneration().isAlive(1, 1));
    }

    @Test
    void blockStillLifeIsStable() {
        Grid block = Grid.withLiveCells(4, 4, new int[][] {{1, 1}, {1, 2}, {2, 1}, {2, 2}});
        Grid next = block.nextGeneration();
        assertTrue(next.isAlive(1, 1));
        assertTrue(next.isAlive(1, 2));
        assertTrue(next.isAlive(2, 1));
        assertTrue(next.isAlive(2, 2));
        assertFalse(next.isAlive(0, 0));
    }

    @Test
    void blinkerOscillatesBetweenVerticalAndHorizontal() {
        Grid vertical = Grid.withLiveCells(5, 5, new int[][] {{1, 2}, {2, 2}, {3, 2}});
        Grid horizontal = vertical.nextGeneration();
        assertTrue(horizontal.isAlive(2, 1));
        assertTrue(horizontal.isAlive(2, 2));
        assertTrue(horizontal.isAlive(2, 3));
        assertFalse(horizontal.isAlive(1, 2));
        assertFalse(horizontal.isAlive(3, 2));
        Grid backToVertical = horizontal.nextGeneration();
        assertTrue(backToVertical.isAlive(1, 2));
        assertTrue(backToVertical.isAlive(3, 2));
    }

    @Test
    void outOfBoundsCoordinatesAreDead() {
        Grid grid = Grid.withLiveCells(2, 2, new int[][] {{0, 0}});
        assertFalse(grid.isAlive(-1, 0));
        assertFalse(grid.isAlive(0, -1));
        assertFalse(grid.isAlive(2, 0));
        assertFalse(grid.isAlive(0, 2));
    }

    @Test
    void emptyGridStaysEmpty() {
        Grid empty = Grid.withLiveCells(3, 3, NO_LIVE_CELLS);
        Grid next = empty.nextGeneration();
        for (int row = 0; row < next.rows(); row++) {
            for (int column = 0; column < next.columns(); column++) {
                assertFalse(next.isAlive(row, column));
            }
        }
    }

    @Test
    void nonPositiveDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Grid.withLiveCells(0, 3, NO_LIVE_CELLS));
        assertThrows(IllegalArgumentException.class, () -> Grid.withLiveCells(3, 0, NO_LIVE_CELLS));
    }
}
```

`src/test/java/com/example/life/GridRendererTest.java`:

```java
package com.example.life;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridRendererTest {

    @Test
    void rendersAliveAndDeadCellsWithOneLinePerRow() {
        Grid grid = Grid.withLiveCells(2, 3, new int[][] {{0, 0}, {1, 2}});
        assertEquals("\u2588\u00B7\u00B7\n\u00B7\u00B7\u2588\n", GridRenderer.render(grid));
    }

    @Test
    void rendersEmptyGridAsAllDeadGlyphs() {
        Grid grid = Grid.withLiveCells(1, 2, new int[][] {});
        assertEquals("\u00B7\u00B7\n", GridRenderer.render(grid));
    }
}
```

Run it with `mvn test` to verify, then `mvn compile exec:java` (or `mvn package` and `java -cp target/classes com.example.life.GameOfLifeApp`). Each frame is drawn by moving the cursor home (`ESC[H`) and overprinting, so the board occupies the same terminal rows for the whole run; a shutdown hook restores the cursor on Ctrl+C.

Cleaned: pure immutable `Grid` core separated from the ANSI shell, rules and glyphs as named constants, rendering testable without a terminal. Safe to change because the Life rules and rendering are covered by tests and the only side effects live in `GameOfLifeApp`.