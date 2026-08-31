import java.time.LocalDateTime;
import java.util.*;

class Booking {
    private final String bookingId;
    private final List<Seat> seats;
    private final LocalDateTime bookingTime;
    private final LocalDateTime eventDateTime;
    private final Map<TicketType, Integer> ticketCounts;
    private final double totalPrice;
    private BookingStatus status;
    
    enum BookingStatus {
        CONFIRMED, CANCELLED
    }
    
    public Booking(String bookingId, List<Seat> seats, LocalDateTime eventDateTime, 
                   Map<TicketType, Integer> ticketCounts, double totalPrice) {
        this.bookingId = bookingId;
        this.seats = seats;
        this.bookingTime = LocalDateTime.now();
        this.eventDateTime = eventDateTime;
        this.ticketCounts = new HashMap<>(ticketCounts);
        this.totalPrice = totalPrice;
        this.status = BookingStatus.CONFIRMED;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public Map<TicketType, Integer> getTicketCounts() {
        return ticketCounts;
    }
}
