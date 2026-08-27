import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SalesAmountSummerTest {

    @Test
    void sumsAmountsAcrossRows() {
        List<String> lines = List.of(
                "id,amount",
                "1,10.50",
                "2,5.25"
        );
        assertEquals(15.75, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void returnsZeroForHeaderOnlyInput() {
        List<String> lines = List.of("id,amount");
        assertEquals(0.0, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void returnsZeroForEmptyInput() {
        assertEquals(0.0, SalesAmountSummer.sumAmounts(List.of()), 0.0001);
    }

    @Test
    void findsAmountColumnRegardlessOfPosition() {
        List<String> lines = List.of(
                "amount,id",
                "3.00,1"
        );
        assertEquals(3.00, SalesAmountSummer.sumAmounts(lines), 0.0001);
    }

    @Test
    void throwsWhenAmountColumnMissing() {
        List<String> lines = List.of(
                "id,total",
                "1,10.00"
        );
        assertThrows(IllegalArgumentException.class, () -> SalesAmountSummer.sumAmounts(lines));
    }

    @Test
    void throwsOnInvalidAmountValue() {
        List<String> lines = List.of(
                "id,amount",
                "1,not-a-number"
        );
        assertThrows(NumberFormatException.class, () -> SalesAmountSummer.sumAmounts(lines));
    }
}
