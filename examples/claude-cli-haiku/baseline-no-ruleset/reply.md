I'll provide a complete Conway's Game of Life implementation. 

**Assumptions:**
- Grid size: 50×20 (reasonable for terminal display)
- Update speed: 200ms per generation
- Initial state: random cells (30% density)
- Edges wrap around (toroidal topology)
- Uses Unicode block characters (█ and ·) for rendering

**pom.xml:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>gameoflife</artifactId>
    <version>1.0-SNAPSHOT</version>

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
                            <mainClass>Main</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Main.java:**

```java
public class Main {
    public static void main(String[] args) throws InterruptedException {
        int width = 50;
        int height = 20;
        GameOfLife game = new GameOfLife(width, height);
        
        int generation = 0;
        while (true) {
            clearScreen();
            System.out.println("Generation: " + generation);
            game.render();
            System.out.flush();
            
            Thread.sleep(200);
            game.update();
            generation++;
        }
    }

    private static void clearScreen() {
        System.out.print("\u001B[2J\u001B[H");
    }
}
```

**GameOfLife.java:**

```java
public class GameOfLife {
    private boolean[][] grid;
    private boolean[][] nextGrid;
    private int width;
    private int height;

    public GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new boolean[height][width];
        this.nextGrid = new boolean[height][width];
        initializeRandom();
    }

    private void initializeRandom() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = Math.random() < 0.3;
            }
        }
    }

    public void update() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countNeighbors(x, y);
                boolean alive = grid[y][x];
                
                if (alive && (neighbors == 2 || neighbors == 3)) {
                    nextGrid[y][x] = true;
                } else if (!alive && neighbors == 3) {
                    nextGrid[y][x] = true;
                } else {
                    nextGrid[y][x] = false;
                }
            }
        }
        
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = (x + dx + width) % width;
                int ny = (y + dy + height) % height;
                if (grid[ny][nx]) count++;
            }
        }
        return count;
    }

    public void render() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                System.out.print(grid[y][x] ? "█" : "·");
            }
            System.out.println();
        }
    }
}
```

To run: `mvn clean package && java -jar target/gameoflife-1.0-SNAPSHOT.jar`

The ANSI escape codes (`\u001B[2J\u001B[H`) clear the screen and move the cursor to the top-left, keeping the display in place while updating each generation.