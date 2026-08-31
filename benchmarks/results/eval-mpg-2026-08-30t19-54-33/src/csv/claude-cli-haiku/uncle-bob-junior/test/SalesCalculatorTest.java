import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class SalesCalculatorTest {
    @TempDir
    Path tempDir;

    @Test
    void sumsSalesAmounts() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "amount\n10.50\n20.75\n30.25");
        assertEquals(61.50, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void handlesMultipleColumns() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "name,amount,date\nWidget,100.00,2026-01-01\nGadget,50.50,2026-01-02");
        assertEquals(150.50, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void caseInsensitiveColumnName() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "Amount\n100\n200");
        assertEquals(300.0, SalesCalculator.sumAmountsFromFile(csv), 0.01);
    }

    @Test
    void throwsWhenColumnNotFound() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "value\n100");
        assertThrows(IllegalArgumentException.class, () -> SalesCalculator.sumAmountsFromFile(csv));
    }

    @Test
    void throwsOnEmptyFile() throws IOException {
        Path csv = tempDir.resolve("test.csv");
        Files.writeString(csv, "");
        assertThrows(IllegalArgumentException.class, () -> SalesCalculator.sumAmountsFromFile(csv));
    }
}
