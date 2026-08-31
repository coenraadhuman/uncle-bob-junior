import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SalesSum {
    public static void main(String[] args) {
        String filePath = "sales.csv";
        double total = 0;
        
        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            total = lines.skip(1)
                .mapToDouble(line -> {
                    String[] fields = line.split(",");
                    return Double.parseDouble(fields[1]);
                })
                .sum();
        } catch (Exception e) {
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }
        
        System.out.println("Total amount: " + total);
    }
}
