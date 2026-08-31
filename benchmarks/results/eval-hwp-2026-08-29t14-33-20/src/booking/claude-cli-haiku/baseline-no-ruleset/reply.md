I'll build a complete event seat booking engine in Java with all specified features.

**Key Assumptions:**
- Ticket prices: Adult $50, Child $25, Senior $40, Student $35
- Seats identified by integer IDs (1 to capacity)
- Holds/bookings use UUID identifiers
- Thread-safe using locks and concurrent collections
- Waiting list processes FIFO when seats become available

```java
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class EventSeatBookingEngine {
    
    enum SeatStatus { AVAILABLE, HELD, BOOKED }
    
    enum TicketTier {
        ADULT(50.0), CHILD(25.0), SENIOR(40.0), STUDENT(35.0);
        private final double price;
        TicketTier(double price) { this.price = price; }
        public double getPrice() { return price; }
    }
    
    static class Seat {
        private final int seatId;
        private SeatStatus status;
        private String holdId;
        private String bookingId;
        
        Seat(int seatId) {
            this.seatId = seatId;
            this.status = SeatStatus.AVAILABLE;
        }
        
        int getSeatId() { return seatId; }
        SeatStatus getStatus() { return status; }
        void setStatus(SeatStatus status) { this.status = status; }
        String getHoldId() { return holdId; }
        void setHoldId(String holdId) { this.holdId = holdId; }
        String getBookingId() { return bookingId; }
        void setBookingId(String bookingId) { this.bookingId = bookingId; }
    }
    
    static class SeatHold {
        private final String holdId;
        private final String customerEmail;
        private final List<Integer> seatIds;
        private final List<TicketTier> ticketTiers;
        private final LocalDateTime expiresAt;
        private boolean confirmed;
        
        SeatHold(String holdId, String customerEmail, List<Integer> seatIds, 
                 List<TicketTier> ticketTiers, LocalDateTime expiresAt) {
            this.holdId = holdId;
            this.customerEmail = customerEmail;
            this.seatIds = new ArrayList<>(seatIds);
            this.ticketTiers = new ArrayList<>(ticketTiers);
            this.expiresAt = expiresAt;
        }
        
        boolean isExpired(LocalDateTime now) { return now.isAfter(expiresAt); }
        String getHoldId() { return holdId; }
        String getCustomerEmail() { return customerEmail; }
        List<Integer> getSeatIds() { return new ArrayList<>(seatIds); }
        List<TicketTier> getTicketTiers() { return new ArrayList<>(ticketTiers); }
        boolean isConfirmed() { return confirmed; }
        void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
    }
    
    static class Booking {
        private final String bookingId;
        private final String customerEmail;
        private final List<Integer> seatIds;
        private final List<TicketTier> ticketTiers;
        private final double totalPrice;
        private final LocalDate eventDate;
        
        Booking(String bookingId, String customerEmail, List<Integer> seatIds, 
                List<TicketTier> ticketTiers, double totalPrice, LocalDate eventDate) {
            this.bookingId = bookingId;
            this.customerEmail = customerEmail;
            this.seatIds = new ArrayList<>(seatIds);
            this.ticketTiers = new ArrayList<>(ticketTiers);
            this.totalPrice = totalPrice;
            this.eventDate = eventDate;
        }
        
        String getBookingId() { return bookingId; }
        String getCustomerEmail() { return customerEmail; }
        List<Integer> getSeatIds() { return new ArrayList<>(seatIds); }
        double getTotalPrice() { return totalPrice; }
        LocalDate getEventDate() { return eventDate; }
    }
    
    static class WaitingListEntry {
        private final String customerEmail;
        private final int numSeats;
        private final List<TicketTier> tiers;
        
        WaitingListEntry(String customerEmail, int numSeats, List<TicketTier> tiers) {
            this.customerEmail = customerEmail;
            this.numSeats = numSeats;
            this.tiers = new ArrayList<>(tiers);
        }
        
        String getCustomerEmail() { return customerEmail; }
        int getNumSeats() { return numSeats; }
        List<TicketTier> getTiers() { return new ArrayList<>(tiers); }
    }
    
    private final int capacity;
    private final LocalDate eventDate;
    private final Map<Integer, Seat> seats;
    private final Map<String, SeatHold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitingListEntry> waitingList;
    private final ReadWriteLock lock;
    
    public EventSeatBookingEngine(int capacity, LocalDate eventDate) {
        this.capacity = capacity;
        this.eventDate = eventDate;
        this.seats = new ConcurrentHashMap<>();
        this.holds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitingList = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantReadWriteLock();
        
        for (int i = 1; i <= capacity; i++) {
            seats.put(i, new Seat(i));
        }
    }
    
    public String findAndHoldSeats(int numSeats, String customerEmail, List<TicketTier> ticketTiers) {
        lock.writeLock().lock();
        try {
            expireOldHolds();
            
            List<Integer> available = findAvailableSeats(numSeats);
            if (available.isEmpty()) {
                waitingList.add(new WaitingListEntry(customerEmail, numSeats, ticketTiers));
                return null;
            }
            
            String holdId = UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            SeatHold hold = new SeatHold(holdId, customerEmail, available, ticketTiers, expiresAt);
            
            for (int seatId : available) {
                Seat seat = seats.get(seatId);
                seat.setStatus(SeatStatus.HELD);
                seat.setHoldId(holdId);
            }
            
            holds.put(holdId, hold);
            return holdId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public String reserveSeats(String seatHoldId) {
        lock.writeLock().lock();
        try {
            SeatHold hold = holds.get(seatHoldId);
            if (hold == null || hold.isExpired(LocalDateTime.now())) {
                return null;
            }
            
            List<Integer> seatIds = hold.getSeatIds();
            List<TicketTier> tiers = hold.getTicketTiers();
            double totalPrice = calculatePrice(seatIds.size(), tiers);
            
            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(bookingId, hold.getCustomerEmail(), seatIds, tiers, totalPrice, eventDate);
            
            for (int seatId : seatIds) {
                Seat seat = seats.get(seatId);
                seat.setStatus(SeatStatus.BOOKED);
                seat.setBookingId(bookingId);
                seat.setHoldId(null);
            }
            
            hold.setConfirmed(true);
            bookings.put(bookingId, booking);
            return bookingId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public void releaseHold(String seatHoldId) {
        lock.writeLock().lock();
        try {
            SeatHold hold = holds.remove(seatHoldId);
            if (hold != null) {
                for (int seatId : hold.getSeatIds()) {
                    Seat seat = seats.get(seatId);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHoldId(null);
                }
                processWaitingList();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public Double cancelBooking(String bookingId) {
        lock.writeLock().lock();
        try {
            Booking booking = bookings.remove(bookingId);
            if (booking == null) {
                return null;
            }
            
            for (int seatId : booking.getSeatIds()) {
                Seat seat = seats.get(seatId);
                seat.setStatus(SeatStatus.AVAILABLE);
                seat.setBookingId(null);
            }
            
            double refund = calculateRefund(booking);
            processWaitingList();
            return refund;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int getAvailableSeats() {
        lock.readLock().lock();
        try {
            return (int) seats.values().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .count();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private List<Integer> findAvailableSeats(int numSeats) {
        List<Integer> available = new ArrayList<>();
        for (Seat seat : seats.values()) {
            if (seat.getStatus() == SeatStatus.AVAILABLE) {
                available.add(seat.getSeatId());
                if (available.size() == numSeats) {
                    return available;
                }
            }
        }
        return available.size() == numSeats ? available : Collections.emptyList();
    }
    
    private void expireOldHolds() {
        LocalDateTime now = LocalDateTime.now();
        holds.entrySet().removeIf(entry -> {
            SeatHold hold = entry.getValue();
            if (hold.isExpired(now) && !hold.isConfirmed()) {
                for (int seatId : hold.getSeatIds()) {
                    Seat seat = seats.get(seatId);
                    seat.setStatus(SeatStatus.AVAILABLE);
                    seat.setHoldId(null);
                }
                return true;
            }
            return false;
        });
    }
    
    private double calculatePrice(int numSeats, List<TicketTier> tiers) {
        double basePrice = tiers.stream().mapToDouble(TicketTier::getPrice).sum();
        return numSeats >= 10 ? basePrice * 0.95 : basePrice;
    }
    
    private double calculateRefund(Booking booking) {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDate.now(), booking.getEventDate());
        if (daysUntilEvent > 30) {
            return booking.getTotalPrice();
        } else if (daysUntilEvent >= 7) {
            return booking.getTotalPrice() * 0.5;
        }
        return 0.0;
    }
    
    private void processWaitingList() {
        while (!waitingList.isEmpty()) {
            WaitingListEntry entry = waitingList.peek();
            List<Integer> available = findAvailableSeats(entry.getNumSeats());
            
            if (available.size() == entry.getNumSeats()) {
                waitingList.poll();
                String holdId = UUID.randomUUID().toString();
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
                SeatHold hold = new SeatHold(holdId, entry.getCustomerEmail(), available, entry.getTiers(), expiresAt);
                
                for (int seatId : available) {
                    Seat seat = seats.get(seatId);
                    seat.setStatus(SeatStatus.HELD);
                    seat.setHoldId(holdId);
                }
                
                holds.put(holdId, hold);
            } else {
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        LocalDate eventDate = LocalDate.of(2026, 9, 15);
        EventSeatBookingEngine engine = new EventSeatBookingEngine(100, eventDate);
        
        System.out.println("=== Event Booking Engine Demo ===\n");
        
        // Test basic hold and reservation
        List<TicketTier> tiers = Arrays.asList(TicketTier.ADULT, TicketTier.CHILD);
        String holdId = engine.findAndHoldSeats(2, "alice@example.com", tiers);
        System.out.println("Hold 2 seats: " + holdId);
        System.out.println("Available: " + engine.getAvailableSeats());
        
        String bookingId = engine.reserveSeats(holdId);
        System.out.println("Confirmed booking: " + bookingId + "\n");
        
        // Test group discount
        List<TicketTier> groupTiers = new ArrayList<>();
        for (int i = 0; i < 10; i++) groupTiers.add(TicketTier.ADULT);
        String groupHold = engine.findAndHoldSeats(10, "bob@example.com", groupTiers);
        String groupBooking = engine.reserveSeats(groupHold);
        System.out.println("Group booking (10 adults, 5% discount): " + groupBooking);
        System.out.println("Available: " + engine.getAvailableSeats() + "\n");
        
        // Test cancellation refund (30+ days before)
        Double refund = engine.cancelBooking(bookingId);
        System.out.println("Cancelled early booking, refund: $" + String.format("%.2f", refund));
        System.out.println("Available: " + engine.getAvailableSeats() + "\n");
        
        // Test waiting list
        int toFill = engine.getAvailableSeats() - 1;
        List<TicketTier> fillTiers = new ArrayList<>();
        for (int i = 0; i < toFill; i++) fillTiers.add(TicketTier.STUDENT);
        String fill = engine.findAndHoldSeats(toFill, "charlie@example.com", fillTiers);
        engine.reserveSeats(fill);
        System.out.println("Filled " + toFill + " seats, available: " + engine.getAvailableSeats());
        
        String waitHold = engine.findAndHoldSeats(2, "diana@example.com", 
            Arrays.asList(TicketTier.SENIOR, TicketTier.SENIOR));
        System.out.println("Requested 2 more seats (event full): " + (waitHold == null ? "Added to waiting list" : waitHold));
    }
}
```

**Features implemented:**
- 15-minute hold expiration with automatic cleanup
- Four ticket tiers with configurable pricing
- 5% discount applied automatically for 10+ seat orders
- Refund calculation: 100% (>30 days), 50% (7-30 days), 0% (<7 days)
- FIFO waiting list that automatically processes when seats free up
- Thread-safe operations using ReadWriteLock and concurrent collections
- Complete demo showing hold, booking, cancellation, group discount, and waiting list