import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Booking {
    private final String bookingId;
    private final List<String> seatIds;
    private final TicketTier ticketTier;
    private final double totalPrice;
    private final LocalDateTime bookingDate;
    private final LocalDateTime eventDate;

    public Booking(String bookingId, List<String> seatIds, TicketTier ticketTier,
                   double totalPrice, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.seatIds = new ArrayList<>(seatIds);
        this.ticketTier = ticketTier;
        this.totalPrice = totalPrice;
        this.bookingDate = LocalDateTime.now();
        this.eventDate = eventDate;
    }

    public String getBookingId() { return bookingId; }
    public List<String> getSeatIds() { return new ArrayList<>(seatIds); }
    public TicketTier getTicketTier() { return ticketTier; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public LocalDateTime getEventDate() { return eventDate; }

    public double calculateRefund() {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
        if (daysUntilEvent > 30) {
            return totalPrice; // 100% refund
        } else if (daysUntilEvent >= 7) {
            return totalPrice * 0.5; // 50% refund
        } else {
            return 0.0; // No refund
        }
    }
}
