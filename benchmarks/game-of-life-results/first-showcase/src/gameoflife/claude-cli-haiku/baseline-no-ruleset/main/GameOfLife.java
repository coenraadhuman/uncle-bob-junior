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
