import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SalesAnalyzerTest {
    @Test
    void sumAmountColumn_withValidData_returnTotal(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("id,amount,date\n");
            writer.write("1,100.50,2026-01-01\n");
            writer.write("2,200.75,2026-01-02\n");
            writer.write("3,150.25,2026-01-03\n");
        }

        double result = SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath());
        assertEquals(451.50, result, 0.01);
    }

    @Test
    void sumAmountColumn_withInvalidAmounts_skipAndSum(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("amount\n");
            writer.write("100\n");
            writer.write("invalid\n");
            writer.write("200\n");
        }

        double result = SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath());
        assertEquals(300.0, result, 0.01);
    }

    @Test
    void sumAmountColumn_missingColumn_throwsException(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.write("id,value\n");
            writer.write("1,100\n");
        }

        assertThrows(IllegalArgumentException.class,
            () -> SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath()));
    }

    @Test
    void sumAmountColumn_emptyFile_throwsException(@TempDir File tempDir) throws IOException {
        File csvFile = new File(tempDir, "sales.csv");
        csvFile.createNewFile();

        assertThrows(IllegalArgumentException.class,
            () -> SalesAnalyzer.sumAmountColumn(csvFile.getAbsolutePath()));
    }
}
