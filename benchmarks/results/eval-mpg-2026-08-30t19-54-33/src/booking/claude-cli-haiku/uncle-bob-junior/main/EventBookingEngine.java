import java.time.LocalDateTime;
import java.util.*;

public class EventBookingEngine {
    private static final int TOTAL_SEATS = 100;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final long GROUP_DISCOUNT_PERCENT = 5;
    private static final long REFUND_FULL_DAYS_BEFORE = 30;
    private static final long REFUND_HALF_DAYS_BEFORE = 7;
    
    private final LocalDateTime eventDate;
    private int availableSeats;
    private final Map<String, SeatHold> holds;
    private final List<Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    
    public EventBookingEngine(LocalDateTime eventDate) {
        this.eventDate = eventDate;
        this.availableSeats = TOTAL_SEATS;
        this.holds = new HashMap<>();
        this.bookings = new ArrayList<>();
        this.waitlist = new LinkedList<>();
    }
    
    public String holdSeats(int quantity, TicketTier tier, LocalDateTime now) {
        expireOldHolds(now);
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        if (!seatsAvailable(quantity)) {
            waitlist.offer(new WaitlistEntry(quantity, tier));
            throw new IllegalStateException("Insufficient seats, added to waitlist");
        }
        
        SeatHold hold = new SeatHold(quantity, now);
        holds.put(hold.getId(), hold);
        availableSeats -= quantity;
        
        return hold.getId();
    }
    
    public Booking confirmHold(String holdId, LocalDateTime now) {
        expireOldHolds(now);
        
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        if (hold.isExpired(now)) {
            hold.expire();
            availableSeats += hold.getQuantity();
            throw new IllegalStateException("Hold has expired");
        }
        
        if (hold.getStatus() != HoldStatus.ACTIVE) {
            throw new IllegalStateException("Hold is not active");
        }
        
        hold.confirm();
        
        long price = calculatePrice(hold.getQuantity());
        Booking booking = new Booking(hold.getQuantity(), TicketTier.ADULT, 
                                     price, eventDate, now);
        bookings.add(booking);
        
        return booking;
    }
    
    public void releaseHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) {
            throw new IllegalArgumentException("Hold not found: " + holdId);
        }
        
        if (hold.getStatus() == HoldStatus.ACTIVE) {
            hold.release();
            availableSeats += hold.getQuantity();
            processWaitlist();
        }
    }
    
    public long cancelBooking(String bookingId, LocalDateTime cancellationDate) {
        Booking booking = bookings.stream()
            .filter(b -> b.getId().equals(bookingId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
        
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Booking is not active");
        }
        
        long refund = booking.getRefundAmount(cancellationDate);
        booking.cancel();
        availableSeats += booking.getQuantity();
        processWaitlist();
        
        return refund;
    }
    
    public int getAvailableSeats() {
        return availableSeats;
    }
    
    public int getWaitlistSize() {
        return waitlist.size();
    }
    
    public List<Booking> getConfirmedBookings() {
        return bookings.stream()
            .filter(b -> b.getStatus() == BookingStatus.CONFIRMED)
            .toList();
    }
    
    private void expireOldHolds(LocalDateTime now) {
        for (SeatHold hold : holds.values()) {
            if (hold.isExpired(now)) {
                hold.expire();
                availableSeats += hold.getQuantity();
            }
        }
    }
    
    private void processWaitlist() {
        while (!waitlist.isEmpty() && availableSeats > 0) {
            WaitlistEntry entry = waitlist.peek();
            
            if (seatsAvailable(entry.getQuantity())) {
                waitlist.poll();
                availableSeats -= entry.getQuantity();
            } else {
                break;
            }
        }
    }
    
    private boolean seatsAvailable(int quantity) {
        return quantity <= availableSeats;
    }
    
    private long calculatePrice(int quantity) {
        long basePrice = TicketTier.ADULT.getBasePriceInCents() * quantity;
        
        if (quantity >= GROUP_DISCOUNT_THRESHOLD) {
            return basePrice - (basePrice * GROUP_DISCOUNT_PERCENT / 100);
        }
        
        return basePrice;
    }
}
