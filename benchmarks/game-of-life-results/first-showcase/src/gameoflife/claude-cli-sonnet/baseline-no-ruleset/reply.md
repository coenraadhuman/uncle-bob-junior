Assumptions: fixed 80x24 grid with toroidal (wrap-around) edges, ~25% random initial live cells, runs indefinitely at ~150ms per generation until interrupted (Ctrl+C), and relies on ANSI escape codes for cursor positioning (works in standard Unix-like terminals; Windows users need a terminal with ANSI/VT support, e.g. Windows Terminal).

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
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.7.1</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.gameoflife.GameOfLife</mainClass>
                        </manifest>
                    </archive>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

```java
package com.example.gameoflife;

import java.util.Random;

public final class GameOfLife {

    private static final int WIDTH = 80;
    private static final int HEIGHT = 24;
    private static final long FRAME_DELAY_MS = 150L;
    private static final double INITIAL_ALIVE_PROBABILITY = 0.25;
    private static final char ALIVE_CHAR = '#';
    private static final char DEAD_CHAR = ' ';

    private boolean[][] grid;

    public GameOfLife(long seed) {
        this.grid = randomGrid(seed);
    }

    private boolean[][] randomGrid(long seed) {
        Random random = new Random(seed);
        boolean[][] result = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                result[y][x] = random.nextDouble() < INITIAL_ALIVE_PROBABILITY;
            }
        }
        return result;
    }

    private int countLiveNeighbours(int row, int col) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dy == 0 && dx == 0) {
                    continue;
                }
                int ny = (row + dy + HEIGHT) % HEIGHT;
                int nx = (col + dx + WIDTH) % WIDTH;
                if (grid[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void step() {
        boolean[][] next = new boolean[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int neighbours = countLiveNeighbours(y, x);
                boolean alive = grid[y][x];
                next[y][x] = alive
                        ? (neighbours == 2 || neighbours == 3)
                        : (neighbours == 3);
            }
        }
        grid = next;
    }

    private String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("\033[H");
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                sb.append(grid[y][x] ? ALIVE_CHAR : DEAD_CHAR);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public void run() throws InterruptedException {
        System.out.print("\033[2J\033[H");
        System.out.flush();
        while (true) {
            System.out.print(render());
            System.out.flush();
            step();
            Thread.sleep(FRAME_DELAY_MS);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        long seed = System.nanoTime();
        new GameOfLife(seed).run();
    }
}
```

Build and run with:
```
mvn package
java -jar target/game-of-life.jar
```