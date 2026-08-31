import java.time.*;
import java.util.*;

public class Booking {
    private final String bookingId;
    private final String email;
    private final List<Seat> seats;
    private final List<TicketType> ticketTypes;
    private final double totalPrice;
    private final LocalDateTime eventDate;
    
    public Booking(String bookingId, String email, List<Seat> seats,
                   List<TicketType> ticketTypes, double totalPrice, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.email = email;
        this.seats = List.copyOf(seats);
        this.ticketTypes = List.copyOf(ticketTypes);
        this.totalPrice = totalPrice;
        this.eventDate = eventDate;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public List<Seat> getSeats() {
        return seats;
    }
    
    public List<TicketType> getTicketTypes() {
        return ticketTypes;
    }
    
    public double getTotalPrice() {
        return totalPrice;
    }
    
    public LocalDateTime getEventDate() {
        return eventDate;
    }
}
