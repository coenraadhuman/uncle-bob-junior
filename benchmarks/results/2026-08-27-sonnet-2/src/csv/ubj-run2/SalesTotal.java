// SalesTotal.java
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class SalesTotal {

    private static final String SALES_FILE_PATH = "sales.csv";

    public static void main(String[] args) throws IOException {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(SALES_FILE_PATH))) {
            double totalAmount = SalesAmountSummer.sumAmountColumn(csvReader);
            System.out.println(totalAmount);
        }
    }
}
