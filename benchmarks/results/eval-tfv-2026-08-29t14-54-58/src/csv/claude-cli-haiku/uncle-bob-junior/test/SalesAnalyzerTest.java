import java.io.*;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class SalesAnalyzerTest {
    private static final String TEST_FILE = "test_sales.csv";

    @Test
    public void testSumValidAmounts() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n2,Product B,200.75\n");
        assertEquals(301.25, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testSumWithEmptyLines() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n\n2,Product B,50.00\n");
        assertEquals(150.50, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testIgnoresInvalidAmountValues() throws IOException {
        writeCsv("id,name,amount\n1,Product A,100.50\n2,Product B,invalid\n3,Product C,50.00\n");
        assertEquals(150.50, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    @Test
    public void testEmptyFileReturnsZero() throws IOException {
        writeCsv("");
        assertEquals(0.0, SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testHeaderOnlyReturnsZero() throws IOException {
        writeCsv("id,name,amount\n");
        assertEquals(0.0, SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testMissingAmountColumnThrows() throws IOException {
        writeCsv("id,name,price\n1,Product A,100.50\n");
        assertThrows(IllegalArgumentException.class, 
                     () -> SalesAnalyzer.calculateTotalAmount(TEST_FILE));
    }

    @Test
    public void testCaseInsensitiveColumnName() throws IOException {
        writeCsv("id,name,AMOUNT\n1,Product A,75.25\n");
        assertEquals(75.25, SalesAnalyzer.calculateTotalAmount(TEST_FILE), 0.01);
    }

    private void writeCsv(String content) throws IOException {
        Files.write(Paths.get(TEST_FILE), content.getBytes());
    }
}
