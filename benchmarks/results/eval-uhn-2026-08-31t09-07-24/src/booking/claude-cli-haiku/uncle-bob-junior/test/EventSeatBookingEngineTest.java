class EventSeatBookingEngineTest {
    private EventSeatBookingEngine engine;
    private Event event;
    private static final String EVENT_ID = "evt_001";
    private static final int TOTAL_SEATS = 100;
    private static final LocalDate EVENT_DATE = LocalDate.now().plusDays(60);
    
    void setUp() {
        engine = new EventSeatBookingEngine();
        TicketPrice pricing = new TicketPrice(50.0, 25.0, 35.0, 30.0);
        event = new Event(EVENT_ID, EVENT_DATE, TOTAL_SEATS, pricing);
        engine.addEvent(event);
    }
    
    void testHoldAndConfirm() {
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 2, TicketTier.CHILD, 1);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        assert holdResult instanceof BookingResult.Success;
        assert engine.availableSeatsFor(EVENT_ID) == 97;
        
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        BookingResult confirmResult = engine.confirmHold(EVENT_ID, holdSuccess.id());
        assert confirmResult instanceof BookingResult.Success;
        assert engine.availableSeatsFor(EVENT_ID) == 97;
    }
    
    void testReleaseHold() {
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 3);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        
        engine.releaseHold(EVENT_ID, holdSuccess.id());
        assert engine.availableSeatsFor(EVENT_ID) == TOTAL_SEATS;
    }
    
    void testGroupDiscount() {
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 10);
        BookingResult result = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success success = (BookingResult.Success) result;
        
        double expectedPrice = 50.0 * 10 * 0.95;
        assert Math.abs(success.totalPrice() - expectedPrice) < 0.01;
    }
    
    void testNoDiscountBelowThreshold() {
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 9);
        BookingResult result = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success success = (BookingResult.Success) result;
        
        double expectedPrice = 50.0 * 9;
        assert Math.abs(success.totalPrice() - expectedPrice) < 0.01;
    }
    
    void testRefundFullMoreThan30Days() {
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 1);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        
        BookingResult confirmResult = engine.confirmHold(EVENT_ID, holdSuccess.id());
        BookingResult.Success confirmSuccess = (BookingResult.Success) confirmResult;
        
        BookingResult cancelResult = engine.cancelBooking(confirmSuccess.id());
        BookingResult.Success cancelSuccess = (BookingResult.Success) cancelResult;
        
        assert Math.abs(cancelSuccess.totalPrice() - 50.0) < 0.01;
    }
    
    void testRefundHalfWithin30Days() {
        LocalDate nearEvent = LocalDate.now().plusDays(15);
        event = new Event(EVENT_ID, nearEvent, TOTAL_SEATS, new TicketPrice(50.0, 25.0, 35.0, 30.0));
        engine.addEvent(event);
        
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 1);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        
        BookingResult confirmResult = engine.confirmHold(EVENT_ID, holdSuccess.id());
        BookingResult.Success confirmSuccess = (BookingResult.Success) confirmResult;
        
        BookingResult cancelResult = engine.cancelBooking(confirmSuccess.id());
        BookingResult.Success cancelSuccess = (BookingResult.Success) cancelResult;
        
        assert Math.abs(cancelSuccess.totalPrice() - 25.0) < 0.01;
    }
    
    void testRefundNoneWithinWeek() {
        LocalDate veryNearEvent = LocalDate.now().plusDays(3);
        event = new Event(EVENT_ID, veryNearEvent, TOTAL_SEATS, new TicketPrice(50.0, 25.0, 35.0, 30.0));
        engine.addEvent(event);
        
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 1);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        
        BookingResult confirmResult = engine.confirmHold(EVENT_ID, holdSuccess.id());
        BookingResult.Success confirmSuccess = (BookingResult.Success) confirmResult;
        
        BookingResult cancelResult = engine.cancelBooking(confirmSuccess.id());
        BookingResult.Success cancelSuccess = (BookingResult.Success) cancelResult;
        
        assert Math.abs(cancelSuccess.totalPrice()) < 0.01;
    }
    
    void testWaitListWhenSoldOut() {
        event = new Event(EVENT_ID, EVENT_DATE, 5, new TicketPrice(50.0, 25.0, 35.0, 30.0));
        engine.addEvent(event);
        
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 5);
        engine.holdSeats(EVENT_ID, tierCounts);
        
        BookingResult waitListResult = engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 2));
        assert waitListResult instanceof BookingResult.Failure;
        assert engine.waitListSizeFor(EVENT_ID) == 1;
    }
    
    void testWaitListServedInOrder() {
        event = new Event(EVENT_ID, EVENT_DATE, 3, new TicketPrice(50.0, 25.0, 35.0, 30.0));
        engine.addEvent(event);
        
        Map<TicketTier, Integer> tierCounts = Map.of(TicketTier.ADULT, 3);
        BookingResult holdResult = engine.holdSeats(EVENT_ID, tierCounts);
        BookingResult.Success holdSuccess = (BookingResult.Success) holdResult;
        
        engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 2));
        engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 1));
        
        assert engine.waitListSizeFor(EVENT_ID) == 2;
        
        engine.releaseHold(EVENT_ID, holdSuccess.id());
        
        assert engine.waitListSizeFor(EVENT_ID) == 1;
        assert engine.availableSeatsFor(EVENT_ID) == 0;
    }
    
    void testWaitListConvertToBookings() {
        event = new Event(EVENT_ID, EVENT_DATE, 5, new TicketPrice(50.0, 25.0, 35.0, 30.0));
        engine.addEvent(event);
        
        BookingResult hold1 = engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 3));
        engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 2));
        engine.holdSeats(EVENT_ID, Map.of(TicketTier.ADULT, 2));
        
        assert engine.waitListSizeFor(EVENT_ID) == 1;
        
        engine.releaseHold(EVENT_ID, ((BookingResult.Success) hold1).id());
        
        assert engine.waitListSizeFor(EVENT_ID) == 0;
        assert engine.availableSeatsFor(EVENT_ID) == 1;
    }
    
    public static void main(String[] args) {
        EventSeatBookingEngineTest test = new EventSeatBookingEngineTest();
        
        test.setUp();
        test.testHoldAndConfirm();
        System.out.println("✓ testHoldAndConfirm");
        
        test.setUp();
        test.testReleaseHold();
        System.out.println("✓ testReleaseHold");
        
        test.setUp();
        test.testGroupDiscount();
        System.out.println("✓ testGroupDiscount");
        
        test.setUp();
        test.testNoDiscountBelowThreshold();
        System.out.println("✓ testNoDiscountBelowThreshold");
        
        test.setUp();
        test.testRefundFullMoreThan30Days();
        System.out.println("✓ testRefundFullMoreThan30Days");
        
        test.testRefundHalfWithin30Days();
        System.out.println("✓ testRefundHalfWithin30Days");
        
        test.testRefundNoneWithinWeek();
        System.out.println("✓ testRefundNoneWithinWeek");
        
        test.setUp();
        test.testWaitListWhenSoldOut();
        System.out.println("✓ testWaitListWhenSoldOut");
        
        test.setUp();
        test.testWaitListServedInOrder();
        System.out.println("✓ testWaitListServedInOrder");
        
        test.setUp();
        test.testWaitListConvertToBookings();
        System.out.println("✓ testWaitListConvertToBookings");
        
        System.out.println("\nAll tests passed!");
    }
}
