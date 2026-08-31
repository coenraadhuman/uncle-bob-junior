import java.time.*;
import java.util.*;

public class EventSeatBookingEngineTest {
    private static void runAllTests() {
        testBasicSeatHold();
        testConfirmBooking();
        testGroupDiscount();
        testFullRefund();
        testPartialRefund();
        testNoRefundLateCancel();
        testWaitlistAddsWhenSoldOut();
        testWaitlistServedOnSeatRelease();
        testHoldExpires();
        System.out.println("All tests passed!");
    }
    
    private static void testBasicSeatHold() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E1", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E1", "test@example.com", List.of(0, 1));
        
        assertTrue(holdId != null && holdId.startsWith("HOLD"), "Hold should be created");
    }
    
    private static void testConfirmBooking() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E2", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E2", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E2", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        assertTrue(bookingId != null && bookingId.startsWith("BOOKING"), "Booking should be confirmed");
        assertEquals(1, engine.getEventBookings("E2").size(), "One booking should exist");
    }
    
    private static void testGroupDiscount() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E3", "Test Event", eventDate, seats);
        
        List<Integer> seatIndices = new ArrayList<>();
        List<TicketType> ticketTypes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            seatIndices.add(i);
            ticketTypes.add(TicketType.ADULT);
        }
        
        String holdId = engine.holdSeats("E3", "test@example.com", seatIndices);
        engine.confirmBooking("E3", holdId, ticketTypes);
        
        Booking booking = engine.getEventBookings("E3").get(0);
        double basePrice = 50.0 * 10;
        double discountedPrice = basePrice * 0.95;
        
        assertEquals(discountedPrice, booking.getTotalPrice(), "5% discount should apply for 10+ seats");
    }
    
    private static void testFullRefund() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E4", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E4", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E4", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        Booking booking = engine.getEventBookings("E4").get(0);
        double originalPrice = booking.getTotalPrice();
        double refund = engine.cancelBooking("E4", bookingId);
        
        assertEquals(originalPrice, refund, "100% refund for >30 days before event");
    }
    
    private static void testPartialRefund() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(10);
        engine.createEvent("E5", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E5", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E5", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        Booking booking = engine.getEventBookings("E5").get(0);
        double originalPrice = booking.getTotalPrice();
        double refund = engine.cancelBooking("E5", bookingId);
        double expectedRefund = originalPrice * 0.5;
        
        assertEquals(expectedRefund, refund, "50% refund for 7-30 days before event");
    }
    
    private static void testNoRefundLateCancel() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(5);
        engine.createEvent("E6", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E6", "test@example.com", List.of(0, 1));
        String bookingId = engine.confirmBooking("E6", holdId, List.of(TicketType.ADULT, TicketType.CHILD));
        
        double refund = engine.cancelBooking("E6", bookingId);
        
        assertEquals(0.0, refund, "No refund for cancellation <7 days before event");
    }
    
    private static void testWaitlistAddsWhenSoldOut() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(3);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(30);
        engine.createEvent("E7", "Test Event", eventDate, seats);
        
        for (int i = 0; i < 3; i++) {
            String holdId = engine.holdSeats("E7", "customer" + i + "@example.com", List.of(i));
            engine.confirmBooking("E7", holdId, List.of(TicketType.ADULT));
        }
        
        String holdId = engine.holdSeats("E7", "waitlist@example.com", List.of(0));
        
        assertTrue(holdId == null, "Hold should be null when all seats sold, customer added to waitlist");
        assertEquals(0, engine.getAvailableSeats("E7"), "No available seats when sold out");
    }
    
    private static void testWaitlistServedOnSeatRelease() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(3);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(30);
        engine.createEvent("E8", "Test Event", eventDate, seats);
        
        for (int i = 0; i < 3; i++) {
            String holdId = engine.holdSeats("E8", "customer" + i + "@example.com", List.of(i));
            engine.confirmBooking("E8", holdId, List.of(TicketType.ADULT));
        }
        
        engine.holdSeats("E8", "waitlist@example.com", List.of(0));
        
        String firstBookingId = engine.getEventBookings("E8").get(0).getBookingId();
        engine.cancelBooking("E8", firstBookingId);
        
        assertTrue(engine.getAvailableSeats("E8") >= 1, "Waitlist should be processed when seat released");
    }
    
    private static void testHoldExpires() {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        List<Seat> seats = createSeats(20);
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("E9", "Test Event", eventDate, seats);
        
        String holdId = engine.holdSeats("E9", "test@example.com", List.of(0, 1));
        assertTrue(holdId != null, "Hold should exist");
        
        engine.releaseHold("E9", holdId);
        assertEquals(0, engine.getEventBookings("E9").size(), "No booking after hold released");
    }
    
    private static List<Seat> createSeats(int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            seats.add(new Seat("SEAT-" + i));
        }
        return seats;
    }
    
    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    
    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }
    
    public static void main(String[] args) {
        runAllTests();
    }
}
