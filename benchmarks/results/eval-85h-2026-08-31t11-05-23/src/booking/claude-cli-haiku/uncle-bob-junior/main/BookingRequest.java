import java.util.*;

public class BookingRequest {
    private final Map<TicketTier, Integer> quantities;
    
    public BookingRequest(Map<TicketTier, Integer> quantities) {
        if (quantities.isEmpty()) {
            throw new IllegalArgumentException("At least one ticket tier required");
        }
        this.quantities = Map.copyOf(quantities);
    }
    
    public Map<TicketTier, Integer> getQuantities() {
        return quantities;
    }
    
    public int getTotalSeats() {
        return quantities.values().stream().mapToInt(Integer::intValue).sum();
    }
}
