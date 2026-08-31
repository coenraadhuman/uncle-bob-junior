import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

class EventBookingEngine {
    private static final Duration HOLD_DURATION = Duration.ofMinutes(15);
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_RATE = 0.05;
    
    private final Event event;
    private final Map<String, Seat> seats;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitListEntry> waitList;
    private final Clock clock;
    
    EventBookingEngine(Event event, Clock clock) {
        this.event = event;
        this.clock = clock;
        this.seats = new LinkedHashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitList = new LinkedList<>();
        
        for (int i = 0; i < event.getTotalSeats(); i++) {
            seats.put("SEAT-" + i, new Seat("SEAT-" + i));
        }
    }
    
    EventBookingEngine(Event event) {
        this(event, Clock.systemDefaultZone());
    }
    
    synchronized String holdSeats(String customerId, List<TicketTier> tierList) {
        expireOldHolds();
        
        int numSeats = tierList.size();
        List<Seat> availableSeats = seats.values().stream()
            .filter(s -> s.getState() == Seat.State.AVAILABLE)
            .limit(numSeats)
            .collect(Collectors.toList());
        
        if (availableSeats.size() < numSeats) {
            waitList.offer(new WaitListEntry(customerId, numSeats, tierList, now()));
            return null;
        }
        
        Map<Seat, TicketTier> seatTiers = new HashMap<>();
        for (int i = 0; i < availableSeats.size(); i++) {
            seatTiers.put(availableSeats.get(i), tierList.get(i));
        }
        
        String holdId = "HOLD-" + UUID.randomUUID();
        LocalDateTime expiresAt = now().plus(HOLD_DURATION);
        SeatHold hold = new SeatHold(holdId, availableSeats, seatTiers, expiresAt);
        
        availableSeats.forEach(s -> s.setState(Seat.State.HELD));
        holds.put(holdId, hold);
        
        return holdId;
    }
    
    synchronized String confirmHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) return null;
        
        if (hold.isExpired(now())) {
            releaseHold(holdId);
            return null;
        }
        
        Map<Seat, TicketTier> seatTiers = hold.getSeatTiers();
        double totalPrice = calculatePrice(seatTiers);
        
        String bookingId = "BOOK-" + UUID.randomUUID();
        Booking booking = new Booking(bookingId, hold.getSeats(), seatTiers, totalPrice, event.getEventTime());
        
        hold.getSeats().forEach(s -> s.setState(Seat.State.BOOKED));
        bookings.put(bookingId, booking);
        holds.remove(holdId);
        
        return bookingId;
    }
    
    synchronized boolean releaseHold(String holdId) {
        SeatHold hold = holds.remove(holdId);
        if (hold == null) return false;
        
        hold.getSeats().forEach(s -> s.setState(Seat.State.AVAILABLE));
        processWaitList();
        return true;
    }
    
    synchronized double cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.isCancelled()) return -1;
        
        double refund = booking.calculateRefund(now());
        booking.setCancelled(true);
        booking.getSeats().forEach(s -> s.setState(Seat.State.AVAILABLE));
        processWaitList();
        
        return refund;
    }
    
    synchronized int getAvailableSeatsCount() {
        expireOldHolds();
        return (int) seats.values().stream()
            .filter(s -> s.getState() == Seat.State.AVAILABLE)
            .count();
    }
    
    synchronized int getBookedSeatsCount() {
        return (int) seats.values().stream()
            .filter(s -> s.getState() == Seat.State.BOOKED)
            .count();
    }
    
    synchronized int getWaitListSize() {
        return waitList.size();
    }
    
    synchronized Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
    
    private void expireOldHolds() {
        holds.entrySet().stream()
            .filter(e -> e.getValue().isExpired(now()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList())
            .forEach(this::releaseHold);
    }
    
    private void processWaitList() {
        while (!waitList.isEmpty() && getAvailableSeatsCount() > 0) {
            WaitListEntry entry = waitList.peek();
            if (holdSeats(entry.getCustomerId(), entry.getTiers()) != null) {
                waitList.poll();
            } else {
                break;
            }
        }
    }
    
    private double calculatePrice(Map<Seat, TicketTier> seatTiers) {
        double basePrice = seatTiers.values().stream()
            .mapToDouble(TicketTier::getBasePrice)
            .sum();
        
        if (seatTiers.size() >= GROUP_DISCOUNT_THRESHOLD) {
            basePrice *= (1 - GROUP_DISCOUNT_RATE);
        }
        
        return basePrice;
    }
    
    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
