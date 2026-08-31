import java.time.LocalDateTime;

// Tests
class EventBookingEngineTest {
    private EventBookingEngine engine;
    private LocalDateTime eventDate;
    private LocalDateTime now;
    
    void setUp() {
        eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        now = LocalDateTime.of(2026, 8, 30, 10, 0);
        engine = new EventBookingEngine(eventDate);
    }
    
    void testHoldSeatsAndConfirm() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        assert holdId != null;
        assert engine.getAvailableSeats() == 95;
        
        Booking booking = engine.confirmHold(holdId, now);
        assert booking != null;
        assert booking.getQuantity() == 5;
        assert engine.getAvailableSeats() == 95;
    }
    
    void testHoldExpiration() {
        setUp();
        String holdId = engine.holdSeats(3, TicketTier.CHILD, now);
        assert engine.getAvailableSeats() == 97;
        
        LocalDateTime expiredTime = now.plusMinutes(16);
        try {
            engine.confirmHold(holdId, expiredTime);
            assert false : "Should throw for expired hold";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("expired");
        }
        
        assert engine.getAvailableSeats() == 100;
    }
    
    void testReleaseHold() {
        setUp();
        String holdId = engine.holdSeats(4, TicketTier.SENIOR, now);
        assert engine.getAvailableSeats() == 96;
        
        engine.releaseHold(holdId);
        assert engine.getAvailableSeats() == 100;
    }
    
    void testGroupDiscountAt10Seats() {
        setUp();
        String holdId = engine.holdSeats(10, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        long basePrice = TicketTier.ADULT.getBasePriceInCents() * 10;
        long discountedPrice = basePrice - (basePrice * 5 / 100);
        assert booking.getTotalPriceInCents() == discountedPrice;
    }
    
    void testNoDiscountBelow10Seats() {
        setUp();
        String holdId = engine.holdSeats(9, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        long basePrice = TicketTier.ADULT.getBasePriceInCents() * 9;
        assert booking.getTotalPriceInCents() == basePrice;
    }
    
    void testFullRefund30DaysBeforeEvent() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        LocalDateTime cancelDate = eventDate.minusDays(31);
        long refund = engine.cancelBooking(booking.getId(), cancelDate);
        assert refund == booking.getTotalPriceInCents();
    }
    
    void testFullRefundExactly30DaysBeforeEvent() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        LocalDateTime cancelDate = eventDate.minusDays(30);
        long refund = engine.cancelBooking(booking.getId(), cancelDate);
        assert refund == booking.getTotalPriceInCents();
    }
    
    void testHalfRefundBetween7And30Days() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        LocalDateTime cancelDate = eventDate.minusDays(15);
        long refund = engine.cancelBooking(booking.getId(), cancelDate);
        assert refund == booking.getTotalPriceInCents() / 2;
    }
    
    void testHalfRefundExactly7DaysBeforeEvent() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        LocalDateTime cancelDate = eventDate.minusDays(7);
        long refund = engine.cancelBooking(booking.getId(), cancelDate);
        assert refund == booking.getTotalPriceInCents() / 2;
    }
    
    void testNoRefundLessThan7DaysBeforeEvent() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        
        LocalDateTime cancelDate = eventDate.minusDays(6);
        long refund = engine.cancelBooking(booking.getId(), cancelDate);
        assert refund == 0;
    }
    
    void testCancellationReleasesSeatBackToInventory() {
        setUp();
        String holdId = engine.holdSeats(5, TicketTier.ADULT, now);
        Booking booking = engine.confirmHold(holdId, now);
        assert engine.getAvailableSeats() == 95;
        
        engine.cancelBooking(booking.getId(), now);
        assert engine.getAvailableSeats() == 100;
    }
    
    void testWaitlistWhenSoldOut() {
        setUp();
        for (int i = 0; i < 10; i++) {
            String holdId = engine.holdSeats(10, TicketTier.ADULT, now);
            engine.confirmHold(holdId, now);
        }
        
        assert engine.getAvailableSeats() == 0;
        
        try {
            engine.holdSeats(5, TicketTier.ADULT, now);
            assert false : "Should throw when sold out";
        } catch (IllegalStateException e) {
            assert e.getMessage().contains("waitlist");
        }
        
        assert engine.getWaitlistSize() == 1;
    }
    
    void testWaitlistProcessingWhenSeatsRelease() {
        setUp();
        for (int i = 0; i < 10; i++) {
            String holdId = engine.holdSeats(10, TicketTier.ADULT, now);
            engine.confirmHold(holdId, now);
        }
        
        try {
            engine.holdSeats(5, TicketTier.ADULT, now);
        } catch (IllegalStateException e) {
            // Expected
        }
        
        assert engine.getWaitlistSize() == 1;
        
        Booking firstBooking = engine.getConfirmedBookings().get(0);
        engine.cancelBooking(firstBooking.getId(), eventDate.minusDays(15));
        
        assert engine.getAvailableSeats() == 5;
        assert engine.getWaitlistSize() == 0;
    }
    
    void testMultipleBookingsWithDifferentTiers() {
        setUp();
        String adultsHold = engine.holdSeats(3, TicketTier.ADULT, now);
        String childrenHold = engine.holdSeats(2, TicketTier.CHILD, now);
        
        Booking adultBooking = engine.confirmHold(adultsHold, now);
        Booking childBooking = engine.confirmHold(childrenHold, now);
        
        assert adultBooking.getTier() == TicketTier.ADULT;
        assert childBooking.getTier() == TicketTier.CHILD;
        assert engine.getConfirmedBookings().size() == 2;
    }
    
    void runAllTests() {
        testHoldSeatsAndConfirm();
        testHoldExpiration();
        testReleaseHold();
        testGroupDiscountAt10Seats();
        testNoDiscountBelow10Seats();
        testFullRefund30DaysBeforeEvent();
        testFullRefundExactly30DaysBeforeEvent();
        testHalfRefundBetween7And30Days();
        testHalfRefundExactly7DaysBeforeEvent();
        testNoRefundLessThan7DaysBeforeEvent();
        testCancellationReleasesSeatBackToInventory();
        testWaitlistWhenSoldOut();
        testWaitlistProcessingWhenSeatsRelease();
        testMultipleBookingsWithDifferentTiers();
        System.out.println("All tests passed");
    }
    
    public static void main(String[] args) {
        new EventBookingEngineTest().runAllTests();
    }
}
