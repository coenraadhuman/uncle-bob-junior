public class SalesReader {
    private static final String CSV_FILE = "sales.csv";
    
    public static void main(String[] args) {
        try {
            double total = new SalesCalculator().calculateTotal(CSV_FILE);
            System.out.println("Total: " + total);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
