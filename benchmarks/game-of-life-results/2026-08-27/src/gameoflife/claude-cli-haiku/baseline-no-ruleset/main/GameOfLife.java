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
