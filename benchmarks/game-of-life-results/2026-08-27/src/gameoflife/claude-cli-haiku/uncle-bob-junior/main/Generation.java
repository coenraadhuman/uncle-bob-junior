public class Generation {
    private boolean[][] grid;
    private final int width;
    private final int height;

    public Generation(boolean[][] initialGrid) {
        this.height = initialGrid.length;
        this.width = initialGrid[0].length;
        this.grid = copyGrid(initialGrid);
    }

    public static Generation randomInitial(int width, int height, double livingRatio) {
        boolean[][] grid = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = Math.random() < livingRatio;
            }
        }
        return new Generation(grid);
    }

    public void advance() {
        boolean[][] next = new boolean[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int neighbors = countNeighbors(x, y);
                next[y][x] = willSurvive(grid[y][x], neighbors);
            }
        }

        this.grid = next;
    }

    private int countNeighbors(int x, int y) {
        int count = 0;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                int nx = (x + dx + width) % width;
                int ny = (y + dy + height) % height;

                if (grid[ny][nx]) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean willSurvive(boolean alive, int neighbors) {
        if (alive) {
            return neighbors == 2 || neighbors == 3;
        }
        return neighbors == 3;
    }

    public boolean[][] getGrid() {
        return copyGrid(grid);
    }

    private static boolean[][] copyGrid(boolean[][] original) {
        boolean[][] copy = new boolean[original.length][original[0].length];
        for (int i = 0; i < original.length; i++) {
            System.arraycopy(original[i], 0, copy[i], 0, original[i].length);
        }
        return copy;
    }
}
