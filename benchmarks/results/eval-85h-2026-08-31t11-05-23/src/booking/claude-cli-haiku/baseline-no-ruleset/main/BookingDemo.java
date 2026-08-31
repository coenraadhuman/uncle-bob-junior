import java.time.*;
import java.util.*;

// Example usage
public class BookingDemo {
    public static void main(String[] args) {
        LocalDateTime eventTime = LocalDateTime.now().plusDays(60);
        Event event = new Event("EVT-001", eventTime, 50);
        EventBookingEngine engine = new EventBookingEngine(event);
        
        // Book 12 seats (triggers group discount)
        String hold1 = engine.holdSeats("CUST-001", List.of(
            TicketTier.ADULT, TicketTier.ADULT, TicketTier.CHILD, TicketTier.SENIOR,
            TicketTier.STUDENT, TicketTier.ADULT, TicketTier.ADULT, TicketTier.CHILD,
            TicketTier.STUDENT, TicketTier.SENIOR, TicketTier.ADULT, TicketTier.ADULT
        ));
        String booking1 = engine.confirmHold(hold1);
        Booking b1 = engine.getBooking(booking1);
        System.out.println("Booking 1 (12 seats with 5% group discount): £" + String.format("%.2f", b1.getTotalPrice()));
        System.out.println("Available seats: " + engine.getAvailableSeatsCount());
        
        // Book 5 seats
        String hold2 = engine.holdSeats("CUST-002", List.of(
            TicketTier.ADULT, TicketTier.ADULT, TicketTier.CHILD, TicketTier.ADULT, TicketTier.STUDENT
        ));
        String booking2 = engine.confirmHold(hold2);
        Booking b2 = engine.getBooking(booking2);
        System.out.println("Booking 2 (5 seats): £" + String.format("%.2f", b2.getTotalPrice()));
        System.out.println("Available seats: " + engine.getAvailableSeatsCount());
        
        // Test cancellation refund
        double refund = engine.cancelBooking(booking2);
        System.out.println("Cancelled booking 2, refund (>30 days): £" + String.format("%.2f", refund));
        System.out.println("Available seats after cancellation: " + engine.getAvailableSeatsCount());
        
        // Waiting list test
        for (int i = 0; i < 8; i++) {
            String hold = engine.holdSeats("CUST-" + (100 + i), 
                List.of(TicketTier.ADULT, TicketTier.ADULT, TicketTier.ADULT, TicketTier.ADULT,
                        TicketTier.ADULT, TicketTier.ADULT));
            if (hold != null) engine.confirmHold(hold);
        }
        System.out.println("Available seats: " + engine.getAvailableSeatsCount());
        
        String hold3 = engine.holdSeats("CUST-200", List.of(TicketTier.ADULT, TicketTier.ADULT));
        System.out.println("Hold 3 result (should be null - on waiting list): " + hold3);
        System.out.println("Wait list size: " + engine.getWaitListSize());
    }
}
