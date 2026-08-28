I'll provide a complete Conway's Game of Life implementation with Maven build configuration.

**Assumptions:**
- Grid: 80×25 cells (standard terminal)
- Initial density: 30% random population
- Delay: 100ms between generations
- Toroidal topology (wraps at edges)
- Runs indefinitely until manually stopped

Here's the pom.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.gameoflife</groupId>
    <artifactId>conway-game-of-life</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.2.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>GameOfLife</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

And here's the main Java implementation:

```java
import java.util.Random;

public class GameOfLife {
    private final int width;
    private final int height;
    private boolean[][] grid;
    private boolean[][] nextGrid;
    private int generation;

    public GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new boolean[height][width];
        this.nextGrid = new boolean[height][width];
        this.generation = 0;
        initializeRandom();
    }

    private void initializeRandom() {
        Random random = new Random();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = random.nextDouble() < 0.3;
            }
        }
    }

    private int countLiveNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = (x + dx + width) % width;
                int ny = (y + dy + height) % height;
                if (grid[ny][nx]) count++;
            }
        }
        return count;
    }

    public void step() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int liveNeighbors = countLiveNeighbors(x, y);
                boolean alive = grid[y][x];
                
                if (alive) {
                    nextGrid[y][x] = liveNeighbors == 2 || liveNeighbors == 3;
                } else {
                    nextGrid[y][x] = liveNeighbors == 3;
                }
            }
        }
        
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;
        generation++;
    }

    public void render() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x] ? "█" : " ");
            }
            System.out.println();
        }
        System.out.printf("Generation: %d%n", generation);
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        GameOfLife game = new GameOfLife(80, 24);
        
        game.render();
        
        while (true) {
            Thread.sleep(100);
            game.step();
            game.render();
        }
    }
}
```

**Build and run:**
```bash
mvn clean package
java -cp target/conway-game-of-life-1.0.0.jar GameOfLife
```

The simulation updates in-place using ANSI escape codes (`\033[H\033[2J` clears the screen and moves cursor to home), preventing scrolling. Press Ctrl+C to stop.