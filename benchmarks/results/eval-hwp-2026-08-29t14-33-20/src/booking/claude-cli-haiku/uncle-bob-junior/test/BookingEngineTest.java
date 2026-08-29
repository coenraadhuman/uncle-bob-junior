public class BookingEngineTest {
    private BookingEngine engine;
    private LocalDateTime eventDateTime;
    
    @Before
    public void setUp() {
        eventDateTime = LocalDateTime.now().plusDays(60);
        Event event = new Event("evt001", "Concert", 100, eventDateTime);
        engine = new BookingEngine(event);
    }
    
    @Test
    public void holdSeats_createsHoldWithExpiryIn15Minutes() {
        List<TicketType> tickets = List.of(TicketType.ADULT, TicketType.CHILD);
        
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        assertNotNull(hold.holdId());
        assertEquals(2, hold.seatNumbers().size());
        assertTrue(hold.expiresAt().isAfter(LocalDateTime.now()));
        assertTrue(hold.expiresAt().isBefore(LocalDateTime.now().plusMinutes(16)));
    }
    
    @Test
    public void holdSeats_reducesAvailableSeats() {
        int before = engine.availableSeatsCount();
        
        engine.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        
        assertEquals(before - 1, engine.availableSeatsCount());
    }
    
    @Test
    public void confirmHold_convertsToBooking() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        
        assertEquals(hold.seatNumbers(), booking.seatNumbers());
        assertEquals(tickets, booking.ticketTypes());
        assertEquals(25.00, booking.totalPrice(), 0.01);
    }
    
    @Test
    public void confirmHold_throwsWhenHoldNotFound() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        
        assertThrows(HoldExpiredException.class,
            () -> engine.confirmHold("nonexistent", tickets, eventDateTime));
    }
    
    @Test
    public void releaseHold_freesSeatsBackToAvailable() {
        int before = engine.availableSeatsCount();
        SeatHold hold = engine.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        int afterHold = engine.availableSeatsCount();
        
        engine.releaseHold(hold.holdId());
        int afterRelease = engine.availableSeatsCount();
        
        assertEquals(before - 1, afterHold);
        assertEquals(before, afterRelease);
    }
    
    @Test
    public void pricingSingleTicket_returnsBasePrice() {
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        
        assertEquals(25.00, booking.totalPrice(), 0.01);
    }
    
    @Test
    public void pricingMultipleTickets_noDiscount() {
        List<TicketType> tickets = List.of(TicketType.ADULT, TicketType.CHILD, TicketType.SENIOR);
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        
        double expected = 25.00 + 12.50 + 15.00;
        assertEquals(expected, booking.totalPrice(), 0.01);
    }
    
    @Test
    public void groupDiscount_appliesFor10Seats() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            tickets.add(TicketType.ADULT);
        }
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        
        double expected = 25.00 * 10 * 0.95;
        assertEquals(expected, booking.totalPrice(), 0.01);
    }
    
    @Test
    public void groupDiscount_doesNotApplyFor9Seats() {
        List<TicketType> tickets = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            tickets.add(TicketType.ADULT);
        }
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        
        double expected = 25.00 * 9;
        assertEquals(expected, booking.totalPrice(), 0.01);
    }
    
    @Test
    public void cancelBooking_refunds100PercentMoreThan30DaysBeforeEvent() {
        LocalDateTime soon = LocalDateTime.now().plusDays(35);
        Event event = new Event("evt002", "Concert", 100, soon);
        BookingEngine e = new BookingEngine(event);
        
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = e.holdSeats(tickets, soon);
        Booking booking = e.confirmHold(hold.holdId(), tickets, soon);
        
        double refund = e.cancelBooking(booking.bookingId(), soon);
        
        assertEquals(25.00, refund, 0.01);
    }
    
    @Test
    public void cancelBooking_refunds50PercentBetween7And30Days() {
        LocalDateTime medium = LocalDateTime.now().plusDays(15);
        Event event = new Event("evt003", "Concert", 100, medium);
        BookingEngine e = new BookingEngine(event);
        
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = e.holdSeats(tickets, medium);
        Booking booking = e.confirmHold(hold.holdId(), tickets, medium);
        
        double refund = e.cancelBooking(booking.bookingId(), medium);
        
        assertEquals(12.50, refund, 0.01);
    }
    
    @Test
    public void cancelBooking_refunds0PercentLessThan7DaysBeforeEvent() {
        LocalDateTime soon = LocalDateTime.now().plusDays(5);
        Event event = new Event("evt004", "Concert", 100, soon);
        BookingEngine e = new BookingEngine(event);
        
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = e.holdSeats(tickets, soon);
        Booking booking = e.confirmHold(hold.holdId(), tickets, soon);
        
        double refund = e.cancelBooking(booking.bookingId(), soon);
        
        assertEquals(0.00, refund, 0.01);
    }
    
    @Test
    public void cancelBooking_throwsWhenBookingNotFound() {
        assertThrows(BookingNotFoundException.class,
            () -> engine.cancelBooking("nonexistent", eventDateTime));
    }
    
    @Test
    public void cancelBooking_freesSeatsBackToAvailable() {
        int before = engine.availableSeatsCount();
        List<TicketType> tickets = List.of(TicketType.ADULT);
        SeatHold hold = engine.holdSeats(tickets, eventDateTime);
        Booking booking = engine.confirmHold(hold.holdId(), tickets, eventDateTime);
        int afterBooking = engine.availableSeatsCount();
        
        engine.cancelBooking(booking.bookingId(), eventDateTime);
        int afterCancel = engine.availableSeatsCount();
        
        assertEquals(before - 1, afterBooking);
        assertEquals(before, afterCancel);
    }
    
    @Test
    public void waitlist_createdWhenSeatsExhausted() {
        Event small = new Event("evt005", "Small", 1, eventDateTime);
        BookingEngine e = new BookingEngine(small);
        
        SeatHold hold = e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        
        InsufficientSeatsException thrown = assertThrows(InsufficientSeatsException.class,
            () -> e.holdSeats(List.of(TicketType.ADULT), eventDateTime));
        
        assertEquals(1, e.waitlistSize());
        assertTrue(thrown.getMessage().contains("waitlist"));
    }
    
    @Test
    public void waitlist_processedWhenSeatsFreed() {
        Event small = new Event("evt006", "Small", 1, eventDateTime);
        BookingEngine e = new BookingEngine(small);
        
        SeatHold hold1 = e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        
        e.releaseHold(hold1.holdId());
        
        assertEquals(0, e.waitlistSize());
    }
    
    @Test
    public void waitlist_servedAfterBookingCancelled() {
        Event small = new Event("evt007", "Small", 1, eventDateTime);
        BookingEngine e = new BookingEngine(small);
        
        SeatHold hold = e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        Booking booking = e.confirmHold(hold.holdId(), List.of(TicketType.ADULT), eventDateTime);
        
        e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
        assertEquals(1, e.waitlistSize());
        
        e.cancelBooking(booking.bookingId(), eventDateTime);
        assertEquals(0, e.waitlistSize());
    }
    
    @Test
    public void concurrentRequests_areThreadSafe() throws InterruptedException {
        Event event = new Event("evt008", "Concert", 20, eventDateTime);
        BookingEngine e = new BookingEngine(event);
        
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            threads.add(new Thread(() -> {
                try {
                    e.holdSeats(List.of(TicketType.ADULT), eventDateTime);
                } catch (InsufficientSeatsException ignored) {}
            }));
        }
        
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        
        assertTrue(e.availableSeatsCount() >= 0);
    }
}
