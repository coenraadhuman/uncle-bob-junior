import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvAmountSummarizerTest {

    @Test
    void sumsAmountColumnAcrossRows() {
        List<String> lines = List.of(
                "id,amount,region",
                "1,10.50,NL",
                "2,4.25,UK"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("14.75"), total);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("3.00"), total);
    }

    @Test
    void returnsZeroWhenNoDataRows() {
        List<String> lines = List.of("id,amount");

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void returnsZeroForEmptyFile() {
        BigDecimal total = CsvAmountSummarizer.sumColumn(List.of(), "amount");

        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void skipsBlankLines() {
        List<String> lines = List.of(
                "id,amount",
                "1,5.00",
                "",
                "2,2.00"
        );

        BigDecimal total = CsvAmountSummarizer.sumColumn(lines, "amount");

        assertEquals(new BigDecimal("7.00"), total);
    }

    @Test
    void throwsWhenColumnMissing() {
        List<String> lines = List.of("id,total");

        assertThrows(IllegalArgumentException.class,
                () -> CsvAmountSummarizer.sumColumn(lines, "amount"));
    }
}
