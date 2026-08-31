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