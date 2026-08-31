import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

class ExchangeRates {
    private static final Map<String, BigDecimal> RATES = Map.of(
        "EUR", BigDecimal.ONE,
        "USD", new BigDecimal("0.92"),
        "GBP", new BigDecimal("1.17")
    );

    static BigDecimal convertToEur(BigDecimal amount, String currency) {
        BigDecimal rate = RATES.getOrDefault(currency.toUpperCase(), BigDecimal.ONE);
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
