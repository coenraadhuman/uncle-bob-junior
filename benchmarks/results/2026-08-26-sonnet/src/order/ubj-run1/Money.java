import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

final class Money {

    private static final int SCALE = 2;

    private Money() {
    }

    static BigDecimal round(BigDecimal amount) {
        return amount.setScale(SCALE, RoundingMode.HALF_UP);
    }

    static String format(BigDecimal amount) {
        return String.format(Locale.US, "€%.2f", amount);
    }
}
