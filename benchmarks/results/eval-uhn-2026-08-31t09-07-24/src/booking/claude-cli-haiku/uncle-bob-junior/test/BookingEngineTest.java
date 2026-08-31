import java.time.LocalDateTime;
import java.util.*;

public class BookingEngineTest {
    
    static void testHoldAndConfirm() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        
        SeatHold hold = engine.holdSeats(2, tiers, now);
        assert hold != null : "Hold should be created";
        assert hold.seatNumbers().size() == 2 : "Hold should have 2 seats";
        assert engine.heldSeatCount() == 2 : "Should have 2 held seats";
        
        Booking booking = engine.confirmHold(hold.id(), now);
        assert booking != null : "Booking should be created";
        assert engine.bookedSeatCount() == 2 : "Should have 2 booked seats";
        assert engine.heldSeatCount() == 0 : "Should have 0 held seats";
    }
    
    static void testHoldExpiry() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 50.0));
        SeatHold hold = engine.holdSeats(1, tiers, now);
        assert engine.heldSeatCount() == 1 : "Should have 1 held seat";
        
        LocalDateTime later = now.plusMinutes(16);
        engine.holdSeats(1, tiers, later);
        
        assert engine.heldSeatCount() == 1 : "Expired hold should be released";
        assert engine.availableSeatCount() == 99 : "First seat should be available again";
    }
    
    static void testReleaseHold() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 50.0));
        SeatHold hold = engine.holdSeats(1, tiers, now);
        assert engine.heldSeatCount() == 1 : "Should have 1 held seat";
        
        engine.releaseHold(hold.id());
        assert engine.heldSeatCount() == 0 : "Should release held seat";
        assert engine.availableSeatCount() == 100 : "Seat should be available";
    }
    
    static void testGroupDiscount() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> smallGroup = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        SeatHold smallHold = engine.holdSeats(2, smallGroup, now);
        assert smallHold.totalPrice() == 100.0 : "Small group no discount: " + smallHold.totalPrice();
        
        List<TicketTier> largeGroup = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            largeGroup.add(new TicketTier("Adult", 50.0));
        }
        SeatHold largeHold = engine.holdSeats(10, largeGroup, now);
        assert largeHold.totalPrice() == 475.0 : "Large group 5% discount: " + largeHold.totalPrice();
    }
    
    static void testMixedTierPricing() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiers = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Child", 25.0),
            new TicketTier("Senior", 30.0)
        );
        
        SeatHold hold = engine.holdSeats(3, tiers, now);
        assert hold.totalPrice() == 105.0 : "Sum mixed tiers: " + hold.totalPrice();
    }
    
    static void testCancellationRefund() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 100);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime bookingTime = LocalDateTime.of(2026, 8, 10, 10, 0);
        
        List<TicketTier> tiers = List.of(new TicketTier("Adult", 100.0));
        
        SeatHold hold = engine.holdSeats(1, tiers, bookingTime);
        Booking booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime earlyCancel = LocalDateTime.of(2026, 8, 14, 10, 0);
        double refund = engine.cancelBooking(booking.id(), earlyCancel);
        assert refund == 100.0 : "Full refund >30 days: " + refund;
        
        hold = engine.holdSeats(1, tiers, bookingTime);
        booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime midCancel = LocalDateTime.of(2026, 8, 31, 10, 0);
        refund = engine.cancelBooking(booking.id(), midCancel);
        assert refund == 50.0 : "Half refund 7-30 days: " + refund;
        
        hold = engine.holdSeats(1, tiers, bookingTime);
        booking = engine.confirmHold(hold.id(), bookingTime);
        LocalDateTime lateCancel = LocalDateTime.of(2026, 9, 10, 10, 0);
        refund = engine.cancelBooking(booking.id(), lateCancel);
        assert refund == 0.0 : "No refund <7 days: " + refund;
    }
    
    static void testWaitlistProcessing() {
        Event event = new Event("E1", LocalDateTime.of(2026, 9, 15, 19, 0), 2);
        BookingEngine engine = new BookingEngine(event);
        LocalDateTime now = LocalDateTime.of(2026, 8, 31, 10, 0);
        
        List<TicketTier> tiersSingle = List.of(new TicketTier("Adult", 50.0));
        List<TicketTier> tiersPair = List.of(
            new TicketTier("Adult", 50.0),
            new TicketTier("Adult", 50.0)
        );
        
        SeatHold hold1 = engine.holdSeats(2, tiersPair, now);
        Booking booking1 = engine.confirmHold(hold1.id(), now);
        assert engine.waitlistSize() == 0 : "No waitlist when not full";
        
        SeatHold hold2 = engine.holdSeats(1, tiersSingle, now);
        assert hold2 == null : "No hold when full";
        assert engine.waitlistSize() == 1 : "Entry added to waitlist";
        
        engine.cancelBooking(booking1.id(), now);
        assert engine.waitlistSize() == 0 : "Waitlist processed after cancellation";
        assert engine.bookedSeatCount() == 1 : "Waitlist entry booked";
    }
    
    public static void main(String[] args) {
        testHoldAndConfirm();
        testHoldExpiry();
        testReleaseHold();
        testGroupDiscount();
        testMixedTierPricing();
        testCancellationRefund();
        testWaitlistProcessing();
        System.out.println("All tests passed!");
    }
}
