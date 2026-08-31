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
