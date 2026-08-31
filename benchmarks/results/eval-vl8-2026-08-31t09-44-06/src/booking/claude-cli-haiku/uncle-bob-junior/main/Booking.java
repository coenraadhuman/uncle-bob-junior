import java.math.BigDecimal;
import java.time.*;
import java.util.*;

class Booking {
    private final String id;
    private final String customerId;
    private final LocalDate eventDate;
    private final List<Seat> seats;
    private final Money totalPrice;
    
    Booking(String id, String customerId, LocalDate eventDate, List<Seat> seats, Money totalPrice) {
        this.id = id;
        this.customerId = customerId;
        this.eventDate = eventDate;
        this.seats = List.copyOf(seats);
        this.totalPrice = totalPrice;
    }
    
    Money calculateRefund() {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDate.now(), eventDate);
        
        if (daysUntilEvent > 30) return totalPrice;
        if (daysUntilEvent >= 7) return totalPrice.applyPartialRefund();
        return new Money(BigDecimal.ZERO);
    }
    
    String id() { return id; }
    String customerId() { return customerId; }
    List<Seat> seats() { return seats; }
    Money totalPrice() { return totalPrice; }
}
