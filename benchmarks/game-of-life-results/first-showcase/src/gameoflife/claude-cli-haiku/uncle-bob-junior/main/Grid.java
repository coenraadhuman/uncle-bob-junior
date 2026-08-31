// Grid.java
public class Grid {
    private boolean[][] cells;
    private int width;
    private int height;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new boolean[height][width];
    }

    public void setCell(int row, int col, boolean alive) {
        cells[row][col] = alive;
    }

    public boolean isAlive(int row, int col) {
        if (row < 0 || row >= height || col < 0 || col >= width) {
            return false;
        }
        return cells[row][col];
    }

    public int countLiveNeighbors(int row, int col) {
        int count = 0;
        for (int dRow = -1; dRow <= 1; dRow++) {
            for (int dCol = -1; dCol <= 1; dCol++) {
                if (dRow == 0 && dCol == 0) continue;
                if (isAlive(row + dRow, col + dCol)) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Grid copy() {
        Grid copy = new Grid(width, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                copy.setCell(row, col, cells[row][col]);
            }
        }
        return copy;
    }
}
