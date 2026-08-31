import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

class BookingEngine {
    private final Event event;
    private final Map<Integer, Seat> seats;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    private final RefundPolicy refundPolicy;
    private final PricingEngine pricingEngine;
    
    public BookingEngine(Event event) {
        this.event = event;
        this.seats = initializeSeats(event.totalSeats());
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();
        this.refundPolicy = new RefundPolicy();
        this.pricingEngine = new PricingEngine();
    }
    
    private Map<Integer, Seat> initializeSeats(int totalSeats) {
        Map<Integer, Seat> map = new LinkedHashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            map.put(i, new Seat(i));
        }
        return map;
    }
    
    public SeatHold holdSeats(int count, List<TicketTier> tiers, LocalDateTime now) {
        expireOldHolds(now);
        List<Integer> available = findAvailableSeats(count);
        
        if (available.isEmpty()) {
            addToWaitlist(count, tiers, now);
            return null;
        }
        
        return createHold(available, tiers, now);
    }
    
    private SeatHold createHold(List<Integer> seatNumbers, List<TicketTier> tiers, LocalDateTime now) {
        String holdId = UUID.randomUUID().toString();
        LocalDateTime expiry = now.plusMinutes(15);
        double price = pricingEngine.calculateTotal(tiers);
        
        for (int seatNumber : seatNumbers) {
            seats.get(seatNumber).holdFor(holdId, expiry);
        }
        
        SeatHold hold = new SeatHold(holdId, seatNumbers, expiry, price);
        holds.put(holdId, hold);
        return hold;
    }
    
    public Booking confirmHold(String holdId, LocalDateTime now) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        if (hold.expiryTime().isBefore(now)) {
            releaseHold(holdId);
            throw new IllegalArgumentException("Hold expired: " + holdId);
        }
        
        String bookingId = UUID.randomUUID().toString();
        for (int seatNumber : hold.seatNumbers()) {
            seats.get(seatNumber).confirmHold(bookingId);
        }
        
        Booking booking = new Booking(bookingId, hold.seatNumbers(), hold.totalPrice(), now);
        bookings.put(bookingId, booking);
        holds.remove(holdId);
        
        processWaitlist(now);
        return booking;
    }
    
    public void releaseHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            return;
        }
        
        for (int seatNumber : hold.seatNumbers()) {
            seats.get(seatNumber).release();
        }
        holds.remove(holdId);
    }
    
    public double cancelBooking(String bookingId, LocalDateTime now) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        
        releaseSeatNumbers(booking.seatNumbers());
        double refund = calculateTotalRefund(booking, now);
        bookings.remove(bookingId);
        
        processWaitlist(now);
        return refund;
    }
    
    private void releaseSeatNumbers(List<Integer> seatNumbers) {
        for (int seatNumber : seatNumbers) {
            seats.get(seatNumber).release();
        }
    }
    
    private double calculateTotalRefund(Booking booking, LocalDateTime now) {
        return booking.seatNumbers().stream()
            .mapToDouble(unused -> refundPolicy.calculateRefund(
                pricePerSeat(booking),
                event.eventTime(),
                now
            ))
            .sum();
    }
    
    private double pricePerSeat(Booking booking) {
        return booking.totalPrice() / booking.seatNumbers().size();
    }
    
    private void expireOldHolds(LocalDateTime now) {
        List<String> expiredHoldIds = holds.values().stream()
            .filter(hold -> hold.expiryTime().isBefore(now))
            .map(SeatHold::id)
            .collect(Collectors.toList());
        
        expiredHoldIds.forEach(this::releaseHold);
    }
    
    private List<Integer> findAvailableSeats(int count) {
        return seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.AVAILABLE)
            .limit(count)
            .map(Seat::number)
            .collect(Collectors.toList());
    }
    
    private void addToWaitlist(int count, List<TicketTier> tiers, LocalDateTime now) {
        String entryId = UUID.randomUUID().toString();
        WaitlistEntry entry = new WaitlistEntry(entryId, count, tiers, now);
        waitlist.offer(entry);
    }
    
    private void processWaitlist(LocalDateTime now) {
        while (!waitlist.isEmpty()) {
            WaitlistEntry entry = waitlist.peek();
            List<Integer> available = findAvailableSeats(entry.seatsRequested());
            
            if (available.size() < entry.seatsRequested()) {
                break;
            }
            
            waitlist.poll();
            SeatHold hold = holdSeats(entry.seatsRequested(), entry.tiers(), now);
            if (hold != null) {
                confirmHold(hold.id(), now);
            }
        }
    }
    
    public int availableSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.AVAILABLE)
            .count();
    }
    
    public int heldSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.HELD)
            .count();
    }
    
    public int bookedSeatCount() {
        return (int) seats.values().stream()
            .filter(seat -> seat.state() == Seat.State.BOOKED)
            .count();
    }
    
    public int waitlistSize() {
        return waitlist.size();
    }
    
    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
}
