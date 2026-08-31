import java.util.*;

class PricingEngine {
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_RATE = 0.05;
    
    public double calculateTotal(List<TicketTier> tiers) {
        double subtotal = tiers.stream()
            .mapToDouble(TicketTier::price)
            .sum();
        
        if (tiers.size() >= GROUP_DISCOUNT_THRESHOLD) {
            return subtotal * (1 - GROUP_DISCOUNT_RATE);
        }
        return subtotal;
    }
}
