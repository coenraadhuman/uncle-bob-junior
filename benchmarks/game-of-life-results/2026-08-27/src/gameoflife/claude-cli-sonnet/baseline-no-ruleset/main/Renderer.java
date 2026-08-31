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
