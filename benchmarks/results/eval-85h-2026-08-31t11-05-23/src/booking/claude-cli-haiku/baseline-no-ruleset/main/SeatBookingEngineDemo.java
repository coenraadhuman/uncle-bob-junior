import java.time.LocalDateTime;

public class SeatBookingEngineDemo {
    public static void main(String[] args) throws SeatBookingException {
        Event event = new Event("EVENT_001", LocalDateTime.now().plusDays(45), 100);
        SeatBookingService service = new SeatBookingService(event);

        System.out.println("=== Seat Booking Engine Demo ===\n");

        // Test 1: Hold seats
        System.out.println("Test 1: Hold 5 adult seats");
        Hold hold1 = service.holdSeats(5, TicketTier.ADULT);
        System.out.println("Hold ID: " + hold1.getHoldId());
        System.out.println("Available seats: " + service.getAvailableSeatsCount());
        System.out.println("Held seats: " + service.getHeldSeatsCount() + "\n");

        // Test 2: Confirm booking with group discount
        System.out.println("Test 2: Confirm hold for 10+ seats (with discount)");
        Hold hold2 = service.holdSeats(12, TicketTier.CHILD);
        Booking booking1 = service.confirmBooking(hold2.getHoldId());
        System.out.println("Booking ID: " + booking1.getBookingId());
        System.out.println("Price: €" + String.format("%.2f", booking1.getTotalPrice()));
        System.out.println("Expected (with 5% discount): €" + String.format("%.2f", 50 * 12 * 0.95));
        System.out.println("Booked seats: " + service.getBookedSeatsCount() + "\n");

        // Test 3: Release hold
        System.out.println("Test 3: Release the 5-seat hold");
        service.releaseHold(hold1.getHoldId());
        System.out.println("Available seats after release: " + service.getAvailableSeatsCount() + "\n");

        // Test 4: Cancel booking with refund
        System.out.println("Test 4: Cancel booking (45 days before event = 100% refund)");
        double refund = service.cancelBooking(booking1.getBookingId());
        System.out.println("Refund amount: €" + String.format("%.2f", refund));
        System.out.println("Available seats: " + service.getAvailableSeatsCount() + "\n");

        // Test 5: Sold out and waitlist
        System.out.println("Test 5: Fill remaining seats and use waitlist");
        int remaining = service.getAvailableSeatsCount();
        Hold hold3 = service.holdSeats(remaining, TicketTier.SENIOR);
        Booking booking2 = service.confirmBooking(hold3.getHoldId());
        System.out.println("Event is now sold out");
        System.out.println("Available seats: " + service.getAvailableSeatsCount());

        String waitlistId = service.addToWaitlist(5, TicketTier.STUDENT);
        System.out.println("Added 5 students to waitlist: " + waitlistId);
        System.out.println("Waitlist size: 1\n");

        service.shutdown();
        System.out.println("Demo completed!");
    }
}
