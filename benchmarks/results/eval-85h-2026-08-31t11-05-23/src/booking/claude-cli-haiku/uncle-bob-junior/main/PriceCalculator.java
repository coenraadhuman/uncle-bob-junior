import java.math.BigDecimal;
import java.util.*;

class PriceCalculator {
    private final Map<TicketType, BigDecimal> pricesByType;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final BigDecimal GROUP_DISCOUNT_RATE = new BigDecimal("0.05");

    PriceCalculator(Map<TicketType, BigDecimal> pricesByType) {
        this.pricesByType = new HashMap<>(pricesByType);
    }

    BigDecimal calculateTotal(List<TicketType> ticketTypes) {
        BigDecimal subtotal = ticketTypes.stream()
            .map(pricesByType::get)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (hasGroupDiscount(ticketTypes.size())) {
            return subtotal.multiply(BigDecimal.ONE.subtract(GROUP_DISCOUNT_RATE));
        }
        return subtotal;
    }

    private boolean hasGroupDiscount(int seatCount) {
        return seatCount >= GROUP_DISCOUNT_THRESHOLD;
    }
}
