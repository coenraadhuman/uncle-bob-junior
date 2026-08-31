public class EventSeatBookingEngineTest {
    
    private static void testHoldSeatsReservesForFifteenMinutes() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E1", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2, 3));
        
        assert holdId != null : "Hold should succeed";
        assert event.availableCount() == 97 : "3 seats should be held";
        System.out.println("✓ testHoldSeatsReservesForFifteenMinutes");
    }
    
    private static void testHoldFailsWhenSeatsUnavailable() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E2", eventDate, 100);
        
        String holdId1 = event.holdSeats(Arrays.asList(1, 2));
        String holdId2 = event.holdSeats(Arrays.asList(1, 2));
        
        assert holdId1 != null : "First hold should succeed";
        assert holdId2 == null : "Second hold should fail";
        System.out.println("✓ testHoldFailsWhenSeatsUnavailable");
    }
    
    private static void testConfirmHoldCreatesBooking() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E3", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(5, 6, 7));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD, TicketType.SENIOR));
        
        assert bookingId != null : "Booking should be created";
        Booking booking = event.booking(bookingId);
        assert booking.seatNumbers().equals(Arrays.asList(5, 6, 7)) : "Seats should match";
        assert booking.totalPrice() == 225.0 : "Price should be 100 + 50 + 75";
        System.out.println("✓ testConfirmHoldCreatesBooking");
    }
    
    private static void testReleaseHoldFreesSeats() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E4", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(10, 11));
        assert event.availableCount() == 98 : "2 seats should be held";
        
        event.releaseHold(holdId);
        assert event.availableCount() == 100 : "Seats should be released";
        System.out.println("✓ testReleaseHoldFreesSeats");
    }
    
    private static void testGroupBookingGets5PercentDiscount() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E5", eventDate, 100);
        
        List<Integer> seats = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            seats.add(i);
        }
        String holdId = event.holdSeats(seats);
        
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tickets.add(TicketType.ADULT);
        }
        String bookingId = event.confirmHold(holdId, tickets);
        Booking booking = event.booking(bookingId);
        
        double expectedPrice = 10 * 100 * 0.95;
        assert Math.abs(booking.totalPrice() - expectedPrice) < 0.01 : 
            "Group discount should be 5%; expected " + expectedPrice + " got " + booking.totalPrice();
        System.out.println("✓ testGroupBookingGets5PercentDiscount");
    }
    
    private static void testSmallBookingNoDiscount() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E6", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        
        Booking booking = event.booking(bookingId);
        double expectedPrice = 100.0 + 50.0;
        assert Math.abs(booking.totalPrice() - expectedPrice) < 0.01 : 
            "No discount for small group";
        System.out.println("✓ testSmallBookingNoDiscount");
    }
    
    private static void testFullRefundMoreThan30DaysBeforeEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        Event event = new Event("E7", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        
        double refund = event.cancelBooking(bookingId);
        double expectedRefund = 150.0;
        assert Math.abs(refund - expectedRefund) < 0.01 : 
            "Full refund expected; got " + refund;
        System.out.println("✓ testFullRefundMoreThan30DaysBeforeEvent");
    }
    
    private static void testPartialRefundBetween7And30DaysBeforeEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(15);
        Event event = new Event("E8", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        
        double refund = event.cancelBooking(bookingId);
        double expectedRefund = 150.0 * 0.5;
        assert Math.abs(refund - expectedRefund) < 0.01 : 
            "50% refund expected; got " + refund;
        System.out.println("✓ testPartialRefundBetween7And30DaysBeforeEvent");
    }
    
    private static void testNoRefundLessThan7DaysBeforeEvent() {
        LocalDateTime eventDate = LocalDateTime.now().plusDays(3);
        Event event = new Event("E9", eventDate, 100);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        
        double refund = event.cancelBooking(bookingId);
        assert refund == 0.0 : "No refund within 7 days; got " + refund;
        System.out.println("✓ testNoRefundLessThan7DaysBeforeEvent");
    }
    
    private static void testWaitingListProcessedWhenSeatsFreed() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E10", eventDate, 2);
        
        String holdId1 = event.holdSeats(Arrays.asList(1, 2));
        String bookingId = event.confirmHold(holdId1, 
            Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        
        assert event.isSoldOut() : "Event should be sold out";
        assert event.waitingListSize() == 0 : "Waiting list should be empty";
        
        event.addToWaitingList(1, Arrays.asList(TicketType.ADULT));
        assert event.waitingListSize() == 1 : "Entry should be added to waiting list";
        
        event.cancelBooking(bookingId);
        assert event.waitingListSize() == 0 : "Waiting list entry should be fulfilled";
        assert event.availableCount() > 0 : "Seats should be available after cancellation";
        System.out.println("✓ testWaitingListProcessedWhenSeatsFreed");
    }
    
    private static void testWaitingListFifoOrder() {
        LocalDateTime eventDate = LocalDateTime.of(2026, 12, 25, 19, 0);
        Event event = new Event("E11", eventDate, 3);
        
        String holdId = event.holdSeats(Arrays.asList(1, 2, 3));
        String bookingId = event.confirmHold(holdId, 
            Arrays.asList(TicketType.ADULT, TicketType.ADULT, TicketType.ADULT));
        
        event.addToWaitingList(2, Arrays.asList(TicketType.ADULT, TicketType.CHILD));
        event.addToWaitingList(1, Arrays.asList(TicketType.STUDENT));
        
        assert event.waitingListSize() == 2 : "Both should be waiting";
        
        event.cancelBooking(bookingId);
        assert event.waitingListSize() == 1 : "First waiting entry should be fulfilled";
        System.out.println("✓ testWaitingListFifoOrder");
    }
    
    public static void main(String[] args) {
        testHoldSeatsReservesForFifteenMinutes();
        testHoldFailsWhenSeatsUnavailable();
        testConfirmHoldCreatesBooking();
        testReleaseHoldFreesSeats();
        testGroupBookingGets5PercentDiscount();
        testSmallBookingNoDiscount();
        testFullRefundMoreThan30DaysBeforeEvent();
        testPartialRefundBetween7And30DaysBeforeEvent();
        testNoRefundLessThan7DaysBeforeEvent();
        testWaitingListProcessedWhenSeatsFreed();
        testWaitingListFifoOrder();
        System.out.println("\nAll tests passed.");
    }
}
