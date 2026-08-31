Here's a complete, self-contained Conway's Game of Life implementation. Assumptions made:

- Random initial population (25% alive) on a 60×30 toroidal (wrap-around) grid.
- Runs indefinitely until you press `Ctrl+C`; optional CLI args let you override width, height, delay, and generation count.
- Uses ANSI escape codes to move the cursor back to the top-left and overwrite the previous frame in place (no new lines, no screen clearing per frame, cursor hidden while running).

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
        <finalName>game-of-life</finalName>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
            </plugin>
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

```java
package com.example.gameoflife;

import java.util.Random;

public final class Board {

    private final int width;
    private final int height;
    private boolean[][] cells;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
    }

    public void randomize(double aliveProbability, long seed) {
        Random random = new Random(seed);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells[y][x] = random.nextDouble() < aliveProbability;
            }
        }
    }

    public boolean isAlive(int x, int y) {
        return cells[y][x];
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int countLiveNeighbours(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                int nx = Math.floorMod(x + dx, width);
                int ny = Math.floorMod(y + dy, height);
                if (cells[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    public void step() {
        boolean[][] next = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbours = countLiveNeighbours(x, y);
                boolean alive = cells[y][x];
                next[y][x] = alive
                        ? (neighbours == 2 || neighbours == 3)
                        : (neighbours == 3);
            }
        }
        cells = next;
    }
}
```

```java
package com.example.gameoflife;

public final class Renderer {

    private static final String CURSOR_HOME = "\u001b[H";
    private static final String CLEAR_SCREEN = "\u001b[2J";
    private static final String HIDE_CURSOR = "\u001b[?25l";
    private static final String SHOW_CURSOR = "\u001b[?25h";

    private final char aliveChar;
    private final char deadChar;
    private boolean firstFrame = true;

    public Renderer(char aliveChar, char deadChar) {
        this.aliveChar = aliveChar;
        this.deadChar = deadChar;
    }

    public void hideCursor() {
        System.out.print(HIDE_CURSOR);
        System.out.flush();
    }

    public void showCursor() {
        System.out.print(SHOW_CURSOR);
        System.out.flush();
    }

    public void render(Board board, long generation) {
        StringBuilder sb = new StringBuilder();

        if (firstFrame) {
            sb.append(CLEAR_SCREEN);
            firstFrame = false;
        }
        sb.append(CURSOR_HOME);

        sb.append("Conway's Game of Life  |  generation: ")
          .append(generation)
          .append("  (Ctrl+C to exit)")
          .append("\n\n");

        for (int y = 0; y < board.getHeight(); y++) {
            for (int x = 0; x < board.getWidth(); x++) {
                sb.append(board.isAlive(x, y) ? aliveChar : deadChar);
            }
            sb.append("\n");
        }

        System.out.print(sb);
        System.out.flush();
    }
}
```

```java
package com.example.gameoflife;

public final class GameOfLife {

    private static final int DEFAULT_WIDTH = 60;
    private static final int DEFAULT_HEIGHT = 30;
    private static final int DEFAULT_DELAY_MS = 150;
    private static final long DEFAULT_MAX_GENERATIONS = 0; // 0 = run forever
    private static final double ALIVE_PROBABILITY = 0.25;

    public static void main(String[] args) throws InterruptedException {
        int width = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_WIDTH;
        int height = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_HEIGHT;
        int delayMs = args.length > 2 ? Integer.parseInt(args[2]) : DEFAULT_DELAY_MS;
        long maxGenerations = args.length > 3 ? Long.parseLong(args[3]) : DEFAULT_MAX_GENERATIONS;

        Board board = new Board(width, height);
        board.randomize(ALIVE_PROBABILITY, System.nanoTime());

        Renderer renderer = new Renderer('#', '.');
        renderer.hideCursor();
        Runtime.getRuntime().addShutdownHook(new Thread(renderer::showCursor));

        long generation = 0;
        try {
            while (maxGenerations == 0 || generation < maxGenerations) {
                renderer.render(board, generation);
                board.step();
                generation++;
                Thread.sleep(delayMs);
            }
        } finally {
            renderer.showCursor();
        }
    }
}
```

Build and run:
```
mvn package
java -jar target/game-of-life.jar
```
or during development: `mvn compile exec:java`

Optional args: `java -jar target/game-of-life.jar <width> <height> <delayMs> <maxGenerations>` (e.g. `80 40 100 500`).