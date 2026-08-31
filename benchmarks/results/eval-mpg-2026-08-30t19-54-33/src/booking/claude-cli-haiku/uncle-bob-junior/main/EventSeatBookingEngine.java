import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EventSeatBookingEngine {
    private static final int HOLD_DURATION_MINUTES = 15;
    private static final double GROUP_DISCOUNT_RATE = 0.05;
    private static final int GROUP_MINIMUM_SEATS = 10;
    private static final int FULL_REFUND_DAYS_BEFORE = 30;
    private static final int PARTIAL_REFUND_DAYS_BEFORE = 7;
    private static final double PARTIAL_REFUND_RATE = 0.50;
    
    public enum TicketTier {
        ADULT(1.0),
        CHILD(0.5),
        SENIOR(0.75),
        STUDENT(0.6);
        
        private final double priceMultiplier;
        
        TicketTier(double priceMultiplier) {
            this.priceMultiplier = priceMultiplier;
        }
        
        public double getPriceMultiplier() {
            return priceMultiplier;
        }
    }
    
    public static class Event {
        private final String id;
        private final String name;
        private final LocalDateTime eventDateTime;
        private final int totalSeats;
        private final Map<TicketTier, BigDecimal> basePrices;
        
        public Event(String name, LocalDateTime eventDateTime, int totalSeats, 
                    Map<TicketTier, BigDecimal> basePrices) {
            this.id = UUID.randomUUID().toString();
            this.name = name;
            this.eventDateTime = eventDateTime;
            this.totalSeats = totalSeats;
            this.basePrices = new EnumMap<>(basePrices);
        }
        
        public String getId() { return id; }
        public LocalDateTime getEventDateTime() { return eventDateTime; }
        public int getTotalSeats() { return totalSeats; }
        public BigDecimal getBasePrice(TicketTier tier) { return basePrices.get(tier); }
    }
    
    public static class SeatHold {
        private final String id;
        private final List<Integer> seatNumbers;
        private final TicketTier tier;
        private final LocalDateTime heldAt;
        private final LocalDateTime expiresAt;
        
        SeatHold(List<Integer> seatNumbers, TicketTier tier, LocalDateTime heldAt) {
            this.id = UUID.randomUUID().toString();
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.tier = tier;
            this.heldAt = heldAt;
            this.expiresAt = heldAt.plusMinutes(HOLD_DURATION_MINUTES);
        }
        
        boolean hasExpired(LocalDateTime now) {
            return now.isAfter(expiresAt);
        }
        
        public String getId() { return id; }
        public List<Integer> getSeatNumbers() { return new ArrayList<>(seatNumbers); }
        public TicketTier getTier() { return tier; }
    }
    
    public static class Booking {
        private final String id;
        private final List<Integer> seatNumbers;
        private final TicketTier tier;
        private final BigDecimal totalPrice;
        private final LocalDateTime bookedAt;
        private BookingStatus status;
        
        public enum BookingStatus {
            ACTIVE, CANCELLED
        }
        
        Booking(List<Integer> seatNumbers, TicketTier tier, BigDecimal totalPrice, 
               LocalDateTime bookedAt) {
            this.id = UUID.randomUUID().toString();
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.tier = tier;
            this.totalPrice = totalPrice;
            this.bookedAt = bookedAt;
            this.status = BookingStatus.ACTIVE;
        }
        
        public String getId() { return id; }
        public List<Integer> getSeatNumbers() { return new ArrayList<>(seatNumbers); }
        public int getSeatCount() { return seatNumbers.size(); }
        public TicketTier getTier() { return tier; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public BookingStatus getStatus() { return status; }
        
        void markCancelled() {
            this.status = BookingStatus.CANCELLED;
        }
    }
    
    public static class WaitlistEntry {
        private final String id;
        private final int requestedSeats;
        private final TicketTier tier;
        
        WaitlistEntry(int requestedSeats, TicketTier tier) {
            this.id = UUID.randomUUID().toString();
            this.requestedSeats = requestedSeats;
            this.tier = tier;
        }
        
        public String getId() { return id; }
        public int getRequestedSeats() { return requestedSeats; }
        public TicketTier getTier() { return tier; }
    }
    
    public static class HoldResult {
        private final String holdId;
        private final BigDecimal totalPrice;
        
        HoldResult(String holdId, BigDecimal totalPrice) {
            this.holdId = holdId;
            this.totalPrice = totalPrice;
        }
        
        public String getHoldId() { return holdId; }
        public BigDecimal getTotalPrice() { return totalPrice; }
    }
    
    public static class WaitlistResult {
        private final String waitlistEntryId;
        private final int queuePosition;
        
        WaitlistResult(String waitlistEntryId, int queuePosition) {
            this.waitlistEntryId = waitlistEntryId;
            this.queuePosition = queuePosition;
        }
        
        public String getWaitlistEntryId() { return waitlistEntryId; }
        public int getQueuePosition() { return queuePosition; }
    }
    
    public static class CancellationResult {
        private final BigDecimal refundAmount;
        private final String reason;
        
        CancellationResult(BigDecimal refundAmount, String reason) {
            this.refundAmount = refundAmount;
            this.reason = reason;
        }
        
        public BigDecimal getRefundAmount() { return refundAmount; }
        public String getReason() { return reason; }
    }
    
    private final Event event;
    private final Set<Integer> availableSeats;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Deque<WaitlistEntry> waitlist;
    private Clock clock;
    
    public EventSeatBookingEngine(Event event) {
        this.event = event;
        this.availableSeats = new HashSet<>();
        for (int i = 1; i <= event.getTotalSeats(); i++) {
            availableSeats.add(i);
        }
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();
        this.clock = Clock.systemDefaultZone();
    }
    
    public void setClock(Clock clock) {
        this.clock = clock;
    }
    
    public Object requestSeats(int seatCount, TicketTier tier) {
        expireHolds();
        
        if (availableSeats.size() >= seatCount) {
            return createHold(seatCount, tier);
        }
        
        return addToWaitlist(seatCount, tier);
    }
    
    public String confirmHold(String holdId) {
        SeatHold hold = holds.remove(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        BigDecimal totalPrice = calculatePrice(hold.getSeatNumbers().size(), hold.getTier());
        Booking booking = new Booking(hold.getSeatNumbers(), hold.getTier(), 
                                     totalPrice, LocalDateTime.now(clock));
        bookings.put(booking.getId(), booking);
        availableSeats.removeAll(hold.getSeatNumbers());
        return booking.getId();
    }
    
    public void releaseHold(String holdId) {
        SeatHold hold = holds.remove(holdId);
        if (hold != null) {
            availableSeats.addAll(hold.getSeatNumbers());
            processWaitlist();
        }
    }
    
    public CancellationResult cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        
        BigDecimal refund = calculateRefund(booking);
        booking.markCancelled();
        availableSeats.addAll(booking.getSeatNumbers());
        processWaitlist();
        
        String reason = formatRefundReason(refund, booking);
        return new CancellationResult(refund, reason);
    }
    
    public void expireHolds() {
        LocalDateTime now = LocalDateTime.now(clock);
        List<String> expiredIds = new ArrayList<>();
        
        for (Map.Entry<String, SeatHold> entry : holds.entrySet()) {
            if (entry.getValue().hasExpired(now)) {
                expiredIds.add(entry.getKey());
            }
        }
        
        expiredIds.forEach(this::releaseHold);
    }
    
    public int getAvailableSeatCount() {
        expireHolds();
        return availableSeats.size();
    }
    
    public int getWaitlistSize() {
        return waitlist.size();
    }
    
    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
    
    private HoldResult createHold(int seatCount, TicketTier tier) {
        List<Integer> selectedSeats = selectSeats(seatCount);
        SeatHold hold = new SeatHold(selectedSeats, tier, LocalDateTime.now(clock));
        holds.put(hold.getId(), hold);
        availableSeats.removeAll(selectedSeats);
        
        BigDecimal totalPrice = calculatePrice(seatCount, tier);
        return new HoldResult(hold.getId(), totalPrice);
    }
    
    private WaitlistResult addToWaitlist(int seatCount, TicketTier tier) {
        WaitlistEntry entry = new WaitlistEntry(seatCount, tier);
        waitlist.add(entry);
        return new WaitlistResult(entry.getId(), waitlist.size());
    }
    
    private void processWaitlist() {
        while (!waitlist.isEmpty()) {
            WaitlistEntry entry = waitlist.peekFirst();
            expireHolds();
            
            if (availableSeats.size() >= entry.getRequestedSeats()) {
                waitlist.removeFirst();
                createHold(entry.getRequestedSeats(), entry.getTier());
            } else {
                break;
            }
        }
    }
    
    private List<Integer> selectSeats(int count) {
        List<Integer> sorted = new ArrayList<>(availableSeats);
        Collections.sort(sorted);
        return sorted.subList(0, Math.min(count, sorted.size()));
    }
    
    private BigDecimal calculatePrice(int seatCount, TicketTier tier) {
        BigDecimal basePrice = event.getBasePrice(tier);
        BigDecimal pricePerSeat = basePrice.multiply(
            BigDecimal.valueOf(tier.getPriceMultiplier()));
        BigDecimal subtotal = pricePerSeat.multiply(BigDecimal.valueOf(seatCount));
        
        if (seatCount >= GROUP_MINIMUM_SEATS) {
            return subtotal.multiply(BigDecimal.valueOf(1 - GROUP_DISCOUNT_RATE));
        }
        
        return subtotal;
    }
    
    private BigDecimal calculateRefund(Booking booking) {
        long daysBefore = ChronoUnit.DAYS.between(LocalDateTime.now(clock), 
                                                   event.getEventDateTime());
        
        if (daysBefore > FULL_REFUND_DAYS_BEFORE) {
            return booking.getTotalPrice();
        }
        
        if (daysBefore > PARTIAL_REFUND_DAYS_BEFORE) {
            return booking.getTotalPrice().multiply(
                BigDecimal.valueOf(PARTIAL_REFUND_RATE));
        }
        
        return BigDecimal.ZERO;
    }
    
    private String formatRefundReason(BigDecimal refund, Booking booking) {
        long daysBefore = ChronoUnit.DAYS.between(LocalDateTime.now(clock), 
                                                   event.getEventDateTime());
        
        if (daysBefore > FULL_REFUND_DAYS_BEFORE) {
            return "Full refund: more than 30 days before event";
        }
        
        if (daysBefore > PARTIAL_REFUND_DAYS_BEFORE) {
            return "50% refund: 7-30 days before event";
        }
        
        return "No refund: within 7 days of event";
    }
}
