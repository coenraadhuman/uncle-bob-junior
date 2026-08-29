// MoneyFormat.java
import java.math.RoundingMode;

/** Shared rounding rules for money amounts, kept in one place. */
final class MoneyFormat {

    static final int SCALE = 2;
    static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyFormat() {
    }
}
