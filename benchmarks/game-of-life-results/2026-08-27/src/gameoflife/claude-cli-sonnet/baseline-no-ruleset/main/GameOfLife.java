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
