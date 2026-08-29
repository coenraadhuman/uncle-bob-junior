Assumptions: toroidal (edge-wrapping) board, a glider as the seed pattern, a fixed run of 300 generations at ~8 fps, and an ANSI-capable terminal (the in-place redraw uses cursor-home escape codes, so each frame overwrites the last with no scrolling).

**pom.xml**

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
                    <mainClass>com.example.life.GameOfLife</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**src/main/java/com/example/life/Grid.java** — the pure, immutable core.

```java
package com.example.life;

import java.util.Arrays;
import java.util.List;

/**
 * Immutable Game of Life grid on a toroidal board: neighbours wrap
 * around the edges, so a glider re-enters on the opposite side.
 */
public final class Grid {

    /** Character that marks a live cell in a seed pattern; any other character is dead. */
    public static final char ALIVE_MARKER = '#';

    private static final int SURVIVAL_MIN_NEIGHBOURS = 2;
    private static final int SURVIVAL_MAX_NEIGHBOURS = 3;
    private static final int BIRTH_NEIGHBOURS = 3;

    private static final int[][] NEIGHBOUR_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    private final boolean[][] cells;

    private Grid(boolean[][] cells) {
        this.cells = cells;
    }

    /**
     * Parses a rectangular pattern of equal-length rows.
     *
     * @throws IllegalArgumentException if the pattern is empty or ragged
     */
    public static Grid fromPattern(List<String> rows) {
        if (rows.isEmpty() || rows.get(0).isEmpty()) {
            throw new IllegalArgumentException("Pattern must have at least one non-empty row");
        }
        int width = rows.get(0).length();
        boolean[][] cells = new boolean[rows.size()][];
        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            cells[rowIndex] = parseRow(rows.get(rowIndex), width);
        }
        return new Grid(cells);
    }

    private static boolean[] parseRow(String row, int expectedWidth) {
        if (row.length() != expectedWidth) {
            throw new IllegalArgumentException("All pattern rows must have length " + expectedWidth);
        }
        boolean[] parsed = new boolean[expectedWidth];
        for (int col = 0; col < expectedWidth; col++) {
            parsed[col] = row.charAt(col) == ALIVE_MARKER;
        }
        return parsed;
    }

    public Grid nextGeneration() {
        boolean[][] next = new boolean[height()][width()];
        for (int row = 0; row < height(); row++) {
            for (int col = 0; col < width(); col++) {
                next[row][col] = nextCellState(row, col);
            }
        }
        return new Grid(next);
    }

    public boolean isAlive(int row, int col) {
        return cells[row][col];
    }

    public int height() {
        return cells.length;
    }

    public int width() {
        return cells[0].length;
    }

    private boolean nextCellState(int row, int col) {
        int neighbours = liveNeighbours(row, col);
        if (isAlive(row, col)) {
            return neighbours >= SURVIVAL_MIN_NEIGHBOURS && neighbours <= SURVIVAL_MAX_NEIGHBOURS;
        }
        return neighbours == BIRTH_NEIGHBOURS;
    }

    private int liveNeighbours(int row, int col) {
        int count = 0;
        for (int[] offset : NEIGHBOUR_OFFSETS) {
            if (isAliveWrapped(row + offset[0], col + offset[1])) {
                count++;
            }
        }
        return count;
    }

    private boolean isAliveWrapped(int row, int col) {
        return cells[Math.floorMod(row, height())][Math.floorMod(col, width())];
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Grid grid)) {
            return false;
        }
        return Arrays.deepEquals(cells, grid.cells);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(cells);
    }
}
```

**src/main/java/com/example/life/ConsoleRenderer.java** — pure grid-to-text rendering, no I/O.

```java
package com.example.life;

/** Renders a grid as terminal text, one line per row. */
public final class ConsoleRenderer {

    private static final char ALIVE_CELL = '█';
    private static final char DEAD_CELL = '·';

    public String render(Grid grid) {
        StringBuilder frame = new StringBuilder();
        for (int row = 0; row < grid.height(); row++) {
            appendRow(frame, grid, row);
        }
        return frame.toString();
    }

    private void appendRow(StringBuilder frame, Grid grid, int row) {
        for (int col = 0; col < grid.width(); col++) {
            frame.append(grid.isAlive(row, col) ? ALIVE_CELL : DEAD_CELL);
        }
        frame.append('\n');
    }
}
```

**src/main/java/com/example/life/GameOfLife.java** — the imperative shell. Each frame moves the cursor back to the top-left before printing, so the UI redraws in place with no scrolling.

```java
package com.example.life;

import java.util.ArrayList;
import java.util.List;

/** Terminal entry point: seeds a glider and animates it in place using ANSI cursor control. */
public final class GameOfLife {

    private static final String CLEAR_SCREEN = "\u001B[2J";
    private static final String CURSOR_HOME = "\u001B[H";
    private static final String HIDE_CURSOR = "\u001B[?25l";
    private static final String SHOW_CURSOR = "\u001B[?25h";

    private static final long FRAME_DELAY_MILLIS = 120;
    private static final int GENERATIONS = 300;
    private static final int FIELD_HEIGHT = 15;
    private static final int FIELD_WIDTH = 40;
    private static final String DEAD_CELL_MARK = ".";
    private static final List<String> GLIDER = List.of(".#.", "..#", "###");

    private GameOfLife() {
    }

    public static void main(String[] args) throws InterruptedException {
        Grid grid = Grid.fromPattern(gliderSeed());
        System.out.print(CLEAR_SCREEN + HIDE_CURSOR);
        try {
            runSimulation(grid, new ConsoleRenderer());
        } finally {
            System.out.print(SHOW_CURSOR);
        }
    }

    private static void runSimulation(Grid grid, ConsoleRenderer renderer) throws InterruptedException {
        for (int generation = 0; generation < GENERATIONS; generation++) {
            System.out.print(CURSOR_HOME + renderer.render(grid));
            System.out.flush();
            Thread.sleep(FRAME_DELAY_MILLIS);
            grid = grid.nextGeneration();
        }
    }

    private static List<String> gliderSeed() {
        List<String> rows = new ArrayList<>();
        for (int row = 0; row < FIELD_HEIGHT; row++) {
            rows.add(seedRow(row));
        }
        return rows;
    }

    private static String seedRow(int row) {
        if (row >= GLIDER.size()) {
            return DEAD_CELL_MARK.repeat(FIELD_WIDTH);
        }
        String glider = GLIDER.get(row);
        return glider + DEAD_CELL_MARK.repeat(FIELD_WIDTH - glider.length());
    }
}
```

**src/test/java/com/example/life/GridTest.java**

```java
package com.example.life;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GridTest {

    @Test
    void blinkerOscillatesWithPeriodTwo() {
        Grid vertical = Grid.fromPattern(List.of(
                ".....",
                "..#..",
                "..#..",
                "..#..",
                "....."));
        Grid horizontal = Grid.fromPattern(List.of(
                ".....",
                ".....",
                ".###.",
                ".....",
                "....."));

        assertEquals(horizontal, vertical.nextGeneration());
        assertEquals(vertical, vertical.nextGeneration().nextGeneration());
    }

    @Test
    void blockStillLifeIsStable() {
        Grid block = Grid.fromPattern(List.of(
                "....",
                ".##.",
                ".##.",
                "...."));

        assertEquals(block, block.nextGeneration());
    }

    @Test
    void lonelyCellDiesOfUnderpopulation() {
        Grid lonely = Grid.fromPattern(List.of(
                ".....",
                "..#..",
                "....."));
        Grid empty = Grid.fromPattern(List.of(
                ".....",
                ".....",
                "....."));

        assertEquals(empty, lonely.nextGeneration());
    }

    @Test
    void deadCellWithExactlyThreeNeighboursIsBorn() {
        Grid seed = Grid.fromPattern(List.of(
                ".....",
                ".#.#.",
                "..#..",
                "....."));

        assertTrue(seed.nextGeneration().isAlive(1, 2));
    }

    @Test
    void liveCellWithFourNeighboursDiesOfOvercrowding() {
        Grid crowded = Grid.fromPattern(List.of(
                ".....",
                ".###.",
                "..#..",
                "..#..",
                "....."));

        assertFalse(crowded.nextGeneration().isAlive(2, 2));
    }

    @Test
    void neighboursWrapAroundBoardEdges() {
        Grid corners = Grid.fromPattern(List.of(
                "#..#",
                "....",
                "....",
                "#..."));

        assertTrue(corners.nextGeneration().isAlive(3, 3),
                "corner cell has three neighbours via wrapping and must be born");
    }

    @Test
    void raggedPatternIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> Grid.fromPattern(List.of("...", "....")));
    }

    @Test
    void emptyPatternIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Grid.fromPattern(List.of()));
    }
}
```

**src/test/java/com/example/life/ConsoleRendererTest.java**

```java
package com.example.life;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleRendererTest {

    private final ConsoleRenderer renderer = new ConsoleRenderer();

    @Test
    void rendersLiveAndDeadCellsRowByRow() {
        Grid grid = Grid.fromPattern(List.of(
                "#.",
                ".#"));

        assertEquals("█·\n·█\n", renderer.render(grid));
    }

    @Test
    void rendersFullyDeadGridAsDotsOnly() {
        Grid grid = Grid.fromPattern(List.of("..."));

        assertEquals("···\n", renderer.render(grid));
    }
}
```

Run with `mvn compile exec:java`; test with `mvn test`.

Cleaned: rules live once in an immutable `Grid` (pure core), rendering is a side-effect-free `ConsoleRenderer`, and all ANSI/timing side effects sit only in `GameOfLife`. Safe to change because every rule branch (survival, birth, under- and overpopulation, edge wrapping, ragged input) is pinned by a test, and swapping the seed, board size, or renderer touches exactly one place.