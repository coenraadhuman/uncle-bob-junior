import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Booking {
    private final String id;
    private final List<Seat> seats;
    private final Map<Seat, TicketTier> tierMap;
    private final LocalDateTime createdAt;
    private final LocalDateTime holdExpiresAt;
    private final LocalDateTime eventDate;
    private BookingStatus status;
    
    public Booking(String id, List<Seat> seats, Map<Seat, TicketTier> tierMap,
                   LocalDateTime createdAt, LocalDateTime holdExpiresAt, LocalDateTime eventDate) {
        this.id = id;
        this.seats = List.copyOf(seats);
        this.tierMap = Map.copyOf(tierMap);
        this.createdAt = createdAt;
        this.holdExpiresAt = holdExpiresAt;
        this.eventDate = eventDate;
        this.status = BookingStatus.HELD;
    }
    
    public String getId() {
        return id;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getHoldExpiresAt() {
        return holdExpiresAt;
    }
    
    public void confirm() {
        if (status != BookingStatus.HELD) {
            throw new IllegalStateException("Only held bookings can be confirmed");
        }
        this.status = BookingStatus.CONFIRMED;
    }
    
    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }
    
    public void expire() {
        this.status = BookingStatus.EXPIRED;
    }
    
    public boolean isHoldExpired(LocalDateTime now) {
        return status == BookingStatus.HELD && now.isAfter(holdExpiresAt);
    }
    
    public int getTotalPrice() {
        int basePrice = tierMap.values().stream()
            .mapToInt(TicketTier::getPriceInCents)
            .sum();
        if (hasGroupDiscount()) {
            return (int) (basePrice * 0.95);
        }
        return basePrice;
    }
    
    public int getRefund(LocalDateTime cancellationTime) {
        long daysUntil = ChronoUnit.DAYS.between(cancellationTime, eventDate);
        int price = getTotalPrice();
        
        if (daysUntil > 30) return price;
        if (daysUntil >= 7) return price / 2;
        return 0;
    }
    
    private boolean hasGroupDiscount() {
        return seats.size() >= 10;
    }
}
