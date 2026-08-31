public class GameOfLife {
    private static final int GRID_WIDTH = 50;
    private static final int GRID_HEIGHT = 20;
    private static final long GENERATION_DELAY_MS = 200;
    private static final double INITIAL_ALIVE_RATIO = 0.3;

    private final Generation generation;
    private final int width;
    private final int height;

    public GameOfLife(int width, int height) {
        this.width = width;
        this.height = height;
        this.generation = Generation.randomInitial(width, height, INITIAL_ALIVE_RATIO);
    }

    public void run(long delayMs) throws InterruptedException {
        while (true) {
            render();
            Thread.sleep(delayMs);
            generation.advance();
        }
    }

    private void render() {
        System.out.print("\u001b[2J\u001b[H");
        System.out.flush();

        StringBuilder sb = new StringBuilder();
        boolean[][] grid = generation.getGrid();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                sb.append(grid[y][x] ? '█' : '·');
            }
            sb.append('\n');
        }

        System.out.print(sb.toString());
        System.out.flush();
    }

    public static void main(String[] args) throws InterruptedException {
        GameOfLife game = new GameOfLife(GRID_WIDTH, GRID_HEIGHT);
        game.run(GENERATION_DELAY_MS);
    }
}
