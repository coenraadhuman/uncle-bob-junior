I'll build a complete event seat booking engine with all the specified features. Here are my assumptions:

- Single event with configurable capacity and date
- Seat IDs are auto-generated (S1, S2, etc.)
- Hold/booking IDs are short UUIDs
- Waiting list is FIFO
- Customers can have only one active hold at a time
- Not thread-safe (can add synchronization if needed)
- Refund percentages: >30 days = 100%, 7-30 days = 50%, <7 days = 0%

```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EventSeatBookingEngine {
    
    enum TicketTier { ADULT, CHILD, SENIOR, STUDENT }
    enum SeatState { AVAILABLE, HELD, BOOKED }
    
    static class Seat {
        String seatId;
        SeatState state;
        
        Seat(String seatId) {
            this.seatId = seatId;
            this.state = SeatState.AVAILABLE;
        }
    }
    
    static class SeatHold {
        String holdId;
        List<String> seatIds;
        List<TicketTier> tiers;
        LocalDateTime expiresAt;
        String customerId;
        
        SeatHold(String holdId, List<String> seatIds, List<TicketTier> tiers,
                LocalDateTime expiresAt, String customerId) {
            this.holdId = holdId;
            this.seatIds = new ArrayList<>(seatIds);
            this.tiers = new ArrayList<>(tiers);
            this.expiresAt = expiresAt;
            this.customerId = customerId;
        }
        
        boolean isExpired(LocalDateTime now) {
            return now.isAfter(expiresAt);
        }
    }
    
    static class Booking {
        String bookingId;
        List<String> seatIds;
        String customerId;
        double originalPrice;
        double paidPrice;
        LocalDateTime bookedAt;
        LocalDateTime eventDate;
        boolean cancelled;
        double refundAmount;
        
        Booking(String bookingId, List<String> seatIds, String customerId, double price,
                LocalDateTime bookedAt, LocalDateTime eventDate) {
            this.bookingId = bookingId;
            this.seatIds = new ArrayList<>(seatIds);
            this.customerId = customerId;
            this.originalPrice = price;
            this.paidPrice = price;
            this.bookedAt = bookedAt;
            this.eventDate = eventDate;
            this.cancelled = false;
            this.refundAmount = 0;
        }
    }
    
    static class WaitingListEntry {
        String customerId;
        List<TicketTier> tiers;
        LocalDateTime addedAt;
        
        WaitingListEntry(String customerId, List<TicketTier> tiers) {
            this.customerId = customerId;
            this.tiers = new ArrayList<>(tiers);
            this.addedAt = LocalDateTime.now();
        }
    }
    
    private Map<String, Seat> seats;
    private Map<String, SeatHold> holds;
    private Map<String, Booking> bookings;
    private Queue<WaitingListEntry> waitingList;
    private Map<TicketTier, Double> tierPrices;
    private LocalDateTime eventDate;
    
    public EventSeatBookingEngine(int seatCapacity, LocalDateTime eventDate, 
                                  Map<TicketTier, Double> tierPrices) {
        this.eventDate = eventDate;
        this.tierPrices = new HashMap<>(tierPrices);
        this.seats = new LinkedHashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitingList = new LinkedList<>();
        
        for (int i = 1; i <= seatCapacity; i++) {
            seats.put("S" + i, new Seat("S" + i));
        }
    }
    
    public String holdSeats(String customerId, List<TicketTier> tiers, LocalDateTime now) {
        releaseExpiredHolds(now);
        
        int numSeats = tiers.size();
        
        // Check if customer already has an active hold
        for (SeatHold hold : holds.values()) {
            if (hold.customerId.equals(customerId)) {
                return null;
            }
        }
        
        // Find available seats
        List<String> availableSeats = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.state == SeatState.AVAILABLE) {
                availableSeats.add(seat.seatId);
                if (availableSeats.size() == numSeats) break;
            }
        }
        
        if (availableSeats.size() < numSeats) {
            // Add to waiting list
            waitingList.offer(new WaitingListEntry(customerId, tiers));
            return null;
        }
        
        // Create hold
        String holdId = "H" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        LocalDateTime expiresAt = now.plusMinutes(15);
        SeatHold hold = new SeatHold(holdId, availableSeats, tiers, expiresAt, customerId);
        holds.put(holdId, hold);
        
        // Mark seats as held
        for (String seatId : availableSeats) {
            seats.get(seatId).state = SeatState.HELD;
        }
        
        return holdId;
    }
    
    public String confirmBooking(String holdId, String customerId, LocalDateTime now) {
        releaseExpiredHolds(now);
        
        SeatHold hold = holds.get(holdId);
        if (hold == null || !hold.customerId.equals(customerId)) {
            return null;
        }
        
        // Calculate price
        double totalPrice = 0;
        for (TicketTier tier : hold.tiers) {
            totalPrice += tierPrices.getOrDefault(tier, 0.0);
        }
        
        // Apply 5% group discount for 10+ seats
        if (hold.seatIds.size() >= 10) {
            totalPrice *= 0.95;
        }
        
        // Create booking
        String bookingId = "B" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Booking booking = new Booking(bookingId, hold.seatIds, customerId, totalPrice, now, eventDate);
        bookings.put(bookingId, booking);
        
        // Mark seats as booked
        for (String seatId : hold.seatIds) {
            seats.get(seatId).state = SeatState.BOOKED;
        }
        
        // Remove hold
        holds.remove(holdId);
        
        // Process waiting list
        processWaitingList(now);
        
        return bookingId;
    }
    
    public boolean cancelBooking(String bookingId, LocalDateTime now) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.cancelled) {
            return false;
        }
        
        long daysUntilEvent = ChronoUnit.DAYS.between(
            now.toLocalDate(), booking.eventDate.toLocalDate());
        
        if (daysUntilEvent > 30) {
            booking.refundAmount = booking.paidPrice;
        } else if (daysUntilEvent >= 7) {
            booking.refundAmount = booking.paidPrice * 0.5;
        } else {
            booking.refundAmount = 0;
        }
        
        booking.cancelled = true;
        
        // Free seats
        for (String seatId : booking.seatIds) {
            seats.get(seatId).state = SeatState.AVAILABLE;
        }
        
        // Process waiting list
        processWaitingList(now);
        
        return true;
    }
    
    public boolean releaseHold(String holdId, String customerId, LocalDateTime now) {
        SeatHold hold = holds.get(holdId);
        if (hold == null || !hold.customerId.equals(customerId)) {
            return false;
        }
        
        holds.remove(holdId);
        
        // Mark seats as available
        for (String seatId : hold.seatIds) {
            seats.get(seatId).state = SeatState.AVAILABLE;
        }
        
        // Process waiting list
        processWaitingList(now);
        
        return true;
    }
    
    private void releaseExpiredHolds(LocalDateTime now) {
        List<String> expiredHoldIds = new ArrayList<>();
        for (SeatHold hold : holds.values()) {
            if (hold.isExpired(now)) {
                expiredHoldIds.add(hold.holdId);
            }
        }
        
        for (String holdId : expiredHoldIds) {
            SeatHold hold = holds.remove(holdId);
            for (String seatId : hold.seatIds) {
                seats.get(seatId).state = SeatState.AVAILABLE;
            }
        }
    }
    
    private void processWaitingList(LocalDateTime now) {
        while (!waitingList.isEmpty()) {
            WaitingListEntry entry = waitingList.peek();
            int numSeats = entry.tiers.size();
            
            int availableCount = (int) seats.values().stream()
                .filter(s -> s.state == SeatState.AVAILABLE)
                .count();
            
            if (availableCount < numSeats) {
                break;
            }
            
            // Remove from waiting list and try to hold seats
            waitingList.poll();
            holdSeats(entry.customerId, entry.tiers, now);
        }
    }
    
    public int getAvailableSeats(LocalDateTime now) {
        releaseExpiredHolds(now);
        return (int) seats.values().stream()
            .filter(s -> s.state == SeatState.AVAILABLE)
            .count();
    }
    
    public int getBookedSeats() {
        return (int) seats.values().stream()
            .filter(s -> s.state == SeatState.BOOKED)
            .count();
    }
    
    public int getHeldSeats(LocalDateTime now) {
        releaseExpiredHolds(now);
        return (int) seats.values().stream()
            .filter(s -> s.state == SeatState.HELD)
            .count();
    }
    
    public int getWaitingListSize() {
        return waitingList.size();
    }
    
    public SeatHold getHold(String holdId) {
        return holds.get(holdId);
    }
    
    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }
    
    public static void main(String[] args) {
        Map<TicketTier, Double> prices = new HashMap<>();
        prices.put(TicketTier.ADULT, 50.0);
        prices.put(TicketTier.CHILD, 25.0);
        prices.put(TicketTier.SENIOR, 35.0);
        prices.put(TicketTier.STUDENT, 30.0);
        
        LocalDateTime eventDate = LocalDateTime.of(2026, 9, 15, 19, 0);
        EventSeatBookingEngine engine = new EventSeatBookingEngine(15, eventDate, prices);
        
        LocalDateTime now = LocalDateTime.of(2026, 8, 29, 10, 0);
        
        // Scenario 1: Hold and confirm booking
        System.out.println("=== Test 1: Hold and Confirm ===");
        String hold1 = engine.holdSeats("customer1", Arrays.asList(
            TicketTier.ADULT, TicketTier.CHILD), now);
        System.out.println("Hold 1: " + hold1);
        System.out.println("Available: " + engine.getAvailableSeats(now) + ", Held: " + 
            engine.getHeldSeats(now));
        
        String booking1 = engine.confirmBooking(hold1, "customer1", now);
        System.out.println("Booking 1: " + booking1);
        System.out.println("Available: " + engine.getAvailableSeats(now) + ", Booked: " + 
            engine.getBookedSeats());
        
        // Scenario 2: Group discount (10+ seats)
        System.out.println("\n=== Test 2: Group Discount ===");
        List<TicketTier> groupTiers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            groupTiers.add(TicketTier.STUDENT);
        }
        String hold2 = engine.holdSeats("customer2", groupTiers, now);
        String booking2 = engine.confirmBooking(hold2, "customer2", now);
        Booking b2 = engine.getBooking(booking2);
        System.out.println("Booking 2 price (10 seats, 5% discount): " + b2.paidPrice);
        
        // Scenario 3: Waiting list
        System.out.println("\n=== Test 3: Waiting List ===");
        String hold3 = engine.holdSeats("customer3", Arrays.asList(TicketTier.ADULT), now);
        System.out.println("Hold 3 (no seats): " + hold3);
        System.out.println("Waiting list size: " + engine.getWaitingListSize());
        
        // Scenario 4: Cancel and refund
        System.out.println("\n=== Test 4: Cancellation Refunds ===");
        LocalDateTime laterDate = now.plusDays(20);
        engine.cancelBooking(booking1, laterDate);
        Booking b1 = engine.getBooking(booking1);
        System.out.println("Cancelled booking refund (20 days): " + b1.refundAmount + " of " + 
            b1.paidPrice);
        System.out.println("Available after cancel: " + engine.getAvailableSeats(laterDate));
    }
}
```

**Key features implemented:**

1. **Seat holds**: Auto-expire after 15 minutes, track custom tier combinations
2. **Ticket tiers**: ADULT, CHILD, SENIOR, STUDENT with configurable prices
3. **Group discount**: 5% off for 10+ seat bookings
4. **Cancellation refunds**: 100% (>30 days), 50% (7-30 days), 0% (<7 days)
5. **Waiting list**: FIFO queue, automatically processes as seats free up
6. **State management**: Tracks AVAILABLE, HELD, and BOOKED seat states
7. **Helper methods**: Query available/booked/held seats and waiting list status