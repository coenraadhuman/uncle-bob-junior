<h1 align="center">Uncle Bob Junior</h1>

<p align="center">
  <em>Easy to read. Simple to understand. Safe to change.</em>
</p>

---

You know the reviewer. Never raises their voice. Reads your fifty-line handler,
says nothing, and leaves eleven comments that each name a function you should
have extracted. A month later you thank them, because the change request that
would have taken a day took twenty minutes.

Uncle Bob Junior puts that reviewer inside your AI agent.

## Before / After

See the [examples](./examples) for the differences between a baseline with no rule set and uncle bob junior in action!

<table>
  <tr>
    <td>Claude (Safe Mode) - Model: Fable</td>
    <td>Claude (Safe Mode) - Model: Fable with Uncle Bob Junior</td>
  </tr> 
  <tr>
    <td>Requests: 1, Total Cost: $0.2152, Avg Latency: 1m 59s</td>
    <td>Requests: 1, Total Cost: $0.5907, Avg Latency: 1m 30s</td>
  </tr>
  <tr>
    <td>
Here's a complete Maven-based Conway's Game of Life that renders in place in the terminal using ANSI escape codes.

Assumptions I made:

- The terminal supports ANSI escape sequences (standard on macOS, Linux, and modern Windows terminals).
- Grid size defaults to 40x20, seeded randomly, with a fixed frame delay of 150 ms. Grid width, height, and delay can be overridden via command-line arguments.
- The grid edges are treated as dead cells (no wraparound).
- The simulation runs until interrupted with Ctrl+C, at which point the cursor is restored.

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

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.4.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.gameoflife.GameOfLife</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <mainClass>com.example.gameoflife.GameOfLife</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**src/main/java/com/example/gameoflife/GameOfLife.java**

```java
package com.example.gameoflife;

import java.util.Random;

/**
 * Conway's Game of Life rendered in place in the terminal.
 *
 * The frame is drawn once, then each generation moves the cursor back to the
 * top of the frame with ANSI escape codes and overwrites it, so the UI never
 * scrolls or adds new lines.
 *
 * Usage: java GameOfLife [width] [height] [delayMs]
 */
public final class GameOfLife {

    private static final String ESC = "\u001B[";
    private static final String HIDE_CURSOR = ESC + "?25l";
    private static final String SHOW_CURSOR = ESC + "?25h";

    private final int width;
    private final int height;
    private boolean[][] cells;

    private GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
    }

    private void seedRandom(long seed) {
        Random random = new Random(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = random.nextInt(100) < 30;
            }
        }
    }

    private int liveNeighbours(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = x + dx;
                int ny = y + dy;
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && cells[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void step() {
        boolean[][] next = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbours = liveNeighbours(x, y);
                next[y][x] = cells[y][x]
                        ? (neighbours == 2 || neighbours == 3)
                        : (neighbours == 3);
            }
        }
        cells = next;
    }

    private int population() {
        int count = 0;
        for (boolean[] row : cells) {
            for (boolean cell : row) {
                if (cell) {
                    count++;
                }
            }
        }
        return count;
    }

    private String render(long generation) {
        StringBuilder sb = new StringBuilder((width + 4) * (height + 3));
        sb.append('+').append("-".repeat(width)).append('+').append('\n');
        for (int y = 0; y < height; y++) {
            sb.append('|');
            for (int x = 0; x < width; x++) {
                sb.append(cells[y][x] ? 'O' : ' ');
            }
            sb.append('|').append('\n');
        }
        sb.append('+').append("-".repeat(width)).append('+').append('\n');
        // Pad the status line so shorter values fully overwrite longer ones.
        String status = String.format("Generation: %d  Population: %d  (Ctrl+C to quit)",
                generation, population());
        sb.append(String.format("%-" + (width + 2) + "s", status));
        return sb.toString();
    }

    public static void main(String[] args) throws InterruptedException {
        int width = args.length > 0 ? Integer.parseInt(args[0]) : 40;
        int height = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        long delayMs = args.length > 2 ? Long.parseLong(args[2]) : 150L;

        if (width < 3 || height < 3) {
            System.err.println("Width and height must each be at least 3.");
            System.exit(1);
        }

        GameOfLife game = new GameOfLife(width, height);
        game.seedRandom(System.nanoTime());

        int frameLines = height + 3;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.print(SHOW_CURSOR);
            System.out.println();
            System.out.flush();
        }));

        System.out.print(HIDE_CURSOR);
        System.out.print(game.render(0));
        System.out.flush();

        for (long generation = 1; ; generation++) {
            Thread.sleep(delayMs);
            game.step();
            // Move the cursor up to the top of the frame and redraw over it.
            System.out.print("\r" + ESC + (frameLines - 1) + "A");
            System.out.print(game.render(generation));
            System.out.flush();
        }
    }
}
```

**How it stays in place:** the frame (border, grid, and status line) is printed once, and every subsequent generation emits `\r` plus the ANSI sequence `ESC[nA` to move the cursor back to the frame's first line before overwriting it. The status line is padded to a fixed width so stale characters never linger. The cursor is hidden during the run to avoid flicker and restored by a shutdown hook on Ctrl+C.

**To build and run:**

```
mvn package
java -jar target/game-of-life-1.0.0.jar          # defaults: 40x20, 150 ms
java -jar target/game-of-life-1.0.0.jar 60 25 100
```

or simply `mvn compile exec:java`.
    </td>
    <td>
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
        </td>
      </tr>
    </table>

**To build and run:**
```bash
mvn clean package
mvn exec:java

# Or after packaging:
java -jar target/conway-game-of-life-1.0.0.jar
```

The display stays in place using ANSI escape codes (`\u001b[2J\u001b[H`), clearing the screen and moving the cursor to the top-left before each generation. Press Ctrl+C to exit.
    
</td>
</tr>
</table>

## How it works

Before code leaves the agent's hands, every checklist item holds:

```
1.  One job each          — SRP: an "and" in the description means split it
2.  Names reveal intent   — remainingRetries, not n
3.  Small functions       — ten statements max, one thought, one abstraction level
4.  Flat control flow     — guard clauses first, nesting max 2
5.  Logic exists once     — DRY, but duplication beats the wrong abstraction
6.  Simplest design       — KISS + YAGNI, no speculative generality
7.  No magic values       — every meaningful literal gets a named constant
8.  Types over checks     — make invalid states unrepresentable
9.  Immutable by default  — side effects at the edges, the core stays pure
10. Comments say why      — the code says what; doc comments carry contracts
11. House style           — match the project's formatter, linter, idiom
12. Tests prove behavior  — happy-path + edge tests against the public surface
13. Libraries over wheels — SDK first, well-maintained deps, wrapped at a seam
```

The checklist runs *after* the agent understands the problem, not instead of
it: read the code the change touches, trace the real flow, then write.

A **final gate** closes every reply: before sending code, the agent checks its
own answer for tests present in the same reply, functions over ten statements,
nesting past 2, bare literals, and mutable state a `final` field or precise
type could replace. The gate is what makes the test rule bind on smaller
models (see the benchmark below): principles get dropped, output contracts
don't.

Clean, not reckless: it never refactors code it doesn't understand, never
renames public APIs uninvited, and never removes trust-boundary validation,
data-loss handling, security, or accessibility. Deliberate deviations get a
`ubj:` comment naming the reason and cleanup trigger, harvested later by
`/uncle-bob-junior-debt`.

## Does it work? Measure it

The repo ships its own with/without benchmark: the same tasks, once bare and
once with the ruleset as system prompt, scored by deterministic
judges — code LOC, longest function, nesting depth, magic numbers, short
names, duplication, mutable fields and setters, whether tests ship, and a
functional correctness gate.
No LLM grading, no hand-picked outputs. It runs through
[promptfoo](https://promptfoo.dev), with a web UI showing both arms side by
side; the provider drives your logged-in Claude Code CLI, so no API key is
needed:

```bash
npx promptfoo@latest eval -c benchmarks/promptfooconfig.yaml
npx promptfoo@latest view
```

`/uncle-bob-junior-gain` renders the newest eval as a scoreboard. Method,
caveats, and how to read the numbers: [benchmarks/](benchmarks/).

### Latest results (28-08-2026, per-generation means)

#### Smells with hits

| task      | model             | arm                   | score | valid code | habit-hooks | oversized-function | too-many-parameters | high-complexity | unused-import | ships tests | correct |
|-----------|-------------------|-----------------------|------:|:----------:|:-----------:|-------------------:|--------------------:|----------------:|--------------:|:-----------:|:-------:|
| email     | claude-cli:haiku  | baseline (no ruleset) |  0.88 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     NO      |   YES   |
| email     | claude-cli:haiku  | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| email     | claude-cli:sonnet | baseline (no ruleset) |  0.88 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     NO      |   YES   |
| email     | claude-cli:sonnet | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| csv       | claude-cli:haiku  | baseline (no ruleset) |  0.86 |    YES     |    FAIL     |                  1 |                   0 |               0 |             0 |     NO      |   YES   |
| csv       | claude-cli:haiku  | uncle-bob-junior      |  0.98 |    YES     |    FAIL     |                  1 |                   0 |               0 |             0 |     YES     |   YES   |
| csv       | claude-cli:sonnet | baseline (no ruleset) |  0.86 |    YES     |    FAIL     |                  1 |                   0 |               0 |             0 |     NO      |   YES   |
| csv       | claude-cli:sonnet | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| retry     | claude-cli:haiku  | baseline (no ruleset) |  0.88 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     NO      |   YES   |
| retry     | claude-cli:sonnet | baseline (no ruleset) |  0.84 |    YES     |    FAIL     |                  1 |                   0 |               1 |             0 |     NO      |   YES   |
| retry     | claude-cli:haiku  | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| retry     | claude-cli:sonnet | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| ratelimit | claude-cli:haiku  | baseline (no ruleset) |  0.86 |    YES     |    FAIL     |                  0 |                   0 |               0 |             1 |     NO      |   YES   |
| ratelimit | claude-cli:haiku  | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| ratelimit | claude-cli:sonnet | baseline (no ruleset) |  0.88 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     NO      |   YES   |
| ratelimit | claude-cli:sonnet | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |
| order     | claude-cli:haiku  | baseline (no ruleset) |  0.86 |    YES     |    FAIL     |                  1 |                   0 |               0 |             0 |     NO      |   YES   |
| order     | claude-cli:haiku  | uncle-bob-junior      |  0.97 |    YES     |    FAIL     |                  1 |                   1 |               0 |             0 |     YES     |   YES   |
| order     | claude-cli:sonnet | baseline (no ruleset) |  0.86 |    YES     |    FAIL     |                  1 |                   0 |               0 |             0 |     NO      |   YES   |
| order     | claude-cli:sonnet | uncle-bob-junior      |  1.00 |    YES     |    PASS     |                  0 |                   0 |               0 |             0 |     YES     |   YES   |

## Install

The Claude Code and Codex plugins run two tiny Node.js lifecycle hooks, so `node` needs to be on your PATH (note for Nix/nvm users: it must be on the non-interactive shell's PATH). If it isn't, the skills still work, the always-on activation just stays quiet instead of erroring on every prompt.

### Claude Code

```
/plugin marketplace add coenraadhuman/uncle-bob-junior
```
```
/plugin install uncle-bob-junior@uncle-bob-junior
```
(You have to send two separate prompts for the install to work)

Same steps in the Claude Code Desktop app's Code tab: type the two `/plugin` commands above into the prompt box, or click the **+** button next to it, choose **Plugins** → **Add plugin** to browse your configured marketplaces, and manage marketplaces from **Customize** in the sidebar.

### Codex

```bash
codex plugin marketplace add coenraadhuman/uncle-bob-junior
codex plugin add uncle-bob-junior@uncle-bob-junior
```

Run `codex` and open `/hooks`, review and trust its two lifecycle hooks, and start a new thread. The same install covers the Codex desktop app: restart the app after installing.

### GitHub Copilot CLI

```bash
copilot plugin marketplace add coenraadhuman/uncle-bob-junior
copilot plugin install uncle-bob-junior@uncle-bob-junior
```

In an interactive Copilot CLI session, use the slash equivalents of the two commands above. Copilot CLI namespaces plugin commands by plugin name, e.g. `/uncle-bob-junior:uncle-bob-junior ultra`.

### Pi agent harness

```
pi install git:github.com/coenraadhuman/uncle-bob-junior
```

### OpenCode

Add to `opencode.json`:

```json
{ "plugin": ["@coenraadhuman/uncle-bob-junior"] }
```

Or run from a checkout (the plugin reuses `hooks/` and `skills/`):

```json
{ "plugin": ["./.opencode/plugins/uncle-bob-junior.mjs"] }
```

Injects the ruleset every turn at the active level and adds the `/uncle-bob-junior` commands. OpenCode also auto-loads this repo's `AGENTS.md`, so the rules hold even without the plugin; the plugin adds the `lite/full/ultra/off` levels.

### Gemini CLI

```bash
gemini extensions install https://github.com/coenraadhuman/uncle-bob-junior
```

Loads the ruleset as always-on context every session and registers the `/uncle-bob-junior` commands; the `skills/` ship too.

### Qoder

Qoder auto-loads `AGENTS.md` from the repo root, so running from a checkout works with zero setup. For per-project rules, copy [`.qoder/rules/uncle-bob-junior.md`](.qoder/rules/uncle-bob-junior.md) into your project's `.qoder/rules/`. For full plugin-tier support (mode activation + per-prompt injection), add the hooks from [`hooks/qoder-hooks.json`](hooks/qoder-hooks.json) to your `.qoder/settings.json`, replacing `UNCLE_BOB_JUNIOR_DIR` with your checkout path.

### Hermes Agent

```bash
hermes plugins install coenraadhuman/uncle-bob-junior --enable
```

Restart Hermes after installing. In shared gateways, restrict `/uncle-bob-junior` to trusted users with Hermes slash-command access controls.

### Devin CLI

```bash
devin plugins install coenraadhuman/uncle-bob-junior
```

### Grok Build

```bash
grok plugin install coenraadhuman/uncle-bob-junior --trust
```

Enable the plugin (off by default) via `/plugins` or `~/.grok/config.toml`, then start a new session.

### OpenClaw

```bash
clawhub install uncle-bob-junior
```

The review, audit, debt, gain, and help skills install the same way. Without ClawHub, copy [`.openclaw/skills/uncle-bob-junior`](.openclaw/skills/) into `~/.openclaw/skills/`.

### Instruction-only hosts

Cursor, Windsurf, Cline, GitHub Copilot Chat, Kiro, Zed, CodeWhale, Swival, Amp, Jules, JetBrains Junie, VS Code + Codex, Antigravity: copy the matching rules file ([`.cursor/rules/`](.cursor/rules/), [`.windsurf/rules/`](.windsurf/rules/), [`.clinerules/`](.clinerules/), [`.github/copilot-instructions.md`](.github/copilot-instructions.md), [`.kiro/steering/`](.kiro/steering/), [`.qoder/rules/`](.qoder/rules/), or plain [`AGENTS.md`](AGENTS.md)). Which files map to which agent: [Agent portability](docs/agent-portability.md).

Set the level for every new session with the `UNCLE_BOB_JUNIOR_DEFAULT_MODE` env var (`lite`/`full`/`ultra`/`off`), or a `defaultMode` field in `~/.config/uncle-bob-junior/config.json` (`%APPDATA%\uncle-bob-junior\config.json` on Windows). The default is `full`.

While active, the ruleset is also injected into every subagent spawned via the Agent tool. To scope that to specific agent types, set `UNCLE_BOB_JUNIOR_SUBAGENT_MATCHER` to a regex tested against the subagent's `agent_type` (unanchored, case-insensitive; unset injects into every subagent).

### Uninstall

| Host | Command |
|------|---------|
| Claude Code | `/plugin remove uncle-bob-junior` |
| Codex | `codex plugin remove uncle-bob-junior` |
| Devin CLI | `devin plugins remove uncle-bob-junior` |
| Grok Build | `grok plugin uninstall uncle-bob-junior` |
| Pi agent | `pi uninstall uncle-bob-junior` |
| Cursor / Windsurf / Cline / Qoder / etc. | Delete the copied rule file |

These remove the plugin's own files. Run `node scripts/uninstall.js` **before** the host remove command to also clean up the mode flag, `~/.config/uncle-bob-junior/config.json`, and (if you accepted the setup nudge) the statusline entry in `~/.claude/settings.json`.

## Commands

| Command | What it does |
|---------|--------------|
| `/uncle-bob-junior [lite \| full \| ultra \| off]` | Set the intensity, or turn it off. No argument reports the current level. |
| `/uncle-bob-junior-review` | Review the current diff for clean-code violations, one line per smell. |
| `/uncle-bob-junior-audit` | Audit the whole repo, ranked by change friction, hot files first. |
| `/uncle-bob-junior-debt` | Harvest the `ubj:` deviations you've deferred into a ledger. |
| `/uncle-bob-junior-gain` | Render the newest with/without promptfoo eval as a scoreboard. |
| `/uncle-bob-junior-help` | Quick reference for the commands above. |

Commands need a skill-capable host (Claude Code, Codex, Devin CLI, OpenCode, Gemini, pi, Swival, Hermes Agent, Qoder, Grok Build). In Codex they're skills, invoke with `@`. The instruction-only adapters load the always-on ruleset without the commands.

## Levels

| Level | What changes |
|-------|--------------|
| **lite** | Readability pass only: names, guard clauses, named constants on the code you touch. |
| **full** | The whole checklist enforced on new and changed code, tests included. Default. |
| **ultra** | Hard limits (≤ 10 statements, ≤ 2 nesting), every branch tested, adjacent smells in touched files cleaned too. |

## Development

When changing the compact rule text, keep the agent copies aligned:

```bash
node scripts/check-rule-copies.js
npm test
```

The OpenClaw skill package (`.openclaw/skills/`) is generated from `skills/`; rerun `node scripts/build-openclaw-skills.js` after changing a skill — the test suite fails if it is stale.

The benchmark tasks are Java: the correctness gate compiles and runs the generated email and CSV code with the local JDK (`javac` + `java` on PATH); without a JDK those checks report "skipped" and the tests skip cleanly.

## FAQ

**Doesn't clean code mean more code?**
Often, yes: named constants, extracted functions, and tests are lines. The benchmark reports it honestly — the ruleset buys readability and changeability, not brevity. If you want fewer lines above all, this is the wrong plugin.

**What if I really need the quick hack?**
Say so. It builds the hack, marks it with a `ubj:` comment naming the reason and the cleanup trigger, and `/uncle-bob-junior-debt` makes sure "later" stays on the books.

**Why "Uncle Bob Junior"?**
A junior disciple of the clean-code school — the checklist is the community's distilled folklore, not an endorsement by, or affiliation with, any actual person.

## License

[MIT](LICENSE).
