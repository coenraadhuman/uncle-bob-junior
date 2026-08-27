// filename: PricingRules.java
import java.math.BigDecimal;

final class PricingRules {

    static final BigDecimal VAT_RATE = new BigDecimal("0.21");
    static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.10");
    static final BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("100.00");
    static final int MONEY_SCALE = 2;

    private PricingRules() {
    }
}
