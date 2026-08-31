import java.math.BigDecimal;
import java.util.*;

// ============ PRICING LOGIC ============

class PriceCalculator {
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private final Map<SeatType, Money> pricing;
    
    PriceCalculator(Map<SeatType, Money> pricing) {
        this.pricing = Map.copyOf(pricing);
    }
    
    Money calculateTotal(List<Seat> seats) {
        Money subtotal = seats.stream()
            .map(seat -> pricing.get(seat.type()))
            .reduce(new Money(BigDecimal.ZERO), Money::add);
        
        return seats.size() >= GROUP_DISCOUNT_THRESHOLD
            ? subtotal.applyGroupDiscount()
            : subtotal;
    }
}
