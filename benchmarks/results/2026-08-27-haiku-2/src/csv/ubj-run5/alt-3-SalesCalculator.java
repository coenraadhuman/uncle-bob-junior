import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class SalesCalculator {
    public static void main(String[] args) {
        try (FileReader reader = new FileReader("sales.csv");
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            
            double sum = 0.0;
            for (CSVRecord record : csvParser) {
                sum += Double.parseDouble(record.get("amount"));
            }
            System.out.println("Total sales: " + sum);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
