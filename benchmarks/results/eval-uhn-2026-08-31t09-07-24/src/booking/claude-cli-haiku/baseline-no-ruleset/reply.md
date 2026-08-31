I'll build a complete event seat booking engine with all the requirements. Here are my assumptions: seat selection is automatic (lowest available numbers), holds expire automatically every minute, refund policy uses day count calculation, and waitlist is served FIFO.

```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SeatBookingEngine {
    
    enum SeatStatus { AVAILABLE, HELD, BOOKED }
    enum TicketTier { ADULT, CHILD, SENIOR, STUDENT }
    
    static class Seat {
        int seatNumber;
        SeatStatus status;
        
        Seat(int seatNumber) {
            this.seatNumber = seatNumber;
            this.status = SeatStatus.AVAILABLE;
        }
    }
    
    static class SeatHold {
        String holdId;
        List<Integer> seatNumbers;
        LocalDateTime expiresAt;
        TicketTier tier;
        
        SeatHold(String holdId, List<Integer> seatNumbers, TicketTier tier, LocalDateTime expiresAt) {
            this.holdId = holdId;
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.tier = tier;
            this.expiresAt = expiresAt;
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
    }
    
    static class Booking {
        String bookingId;
        List<Integer> seatNumbers;
        TicketTier tier;
        LocalDateTime eventDate;
        double totalPrice;
        
        Booking(String bookingId, List<Integer> seatNumbers, TicketTier tier, 
                LocalDateTime eventDate, double totalPrice) {
            this.bookingId = bookingId;
            this.seatNumbers = new ArrayList<>(seatNumbers);
            this.tier = tier;
            this.eventDate = eventDate;
            this.totalPrice = totalPrice;
        }
        
        double getRefundAmount() {
            long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
            if (daysUntilEvent > 30) return totalPrice;
            if (daysUntilEvent >= 7) return totalPrice * 0.5;
            return 0;
        }
    }
    
    static class WaitlistEntry {
        String entryId;
        int requestedSeats;
        TicketTier tier;
        LocalDateTime requestedAt;
        
        WaitlistEntry(String entryId, int requestedSeats, TicketTier tier) {
            this.entryId = entryId;
            this.requestedSeats = requestedSeats;
            this.tier = tier;
            this.requestedAt = LocalDateTime.now();
        }
    }
    
    static class Event {
        String eventId;
        LocalDateTime eventDate;
        int totalSeats;
        Map<TicketTier, Double> priceTiers;
        
        Event(String eventId, LocalDateTime eventDate, int totalSeats, 
              Map<TicketTier, Double> priceTiers) {
            this.eventId = eventId;
            this.eventDate = eventDate;
            this.totalSeats = totalSeats;
            this.priceTiers = new HashMap<>(priceTiers);
        }
    }
    
    private final Map<Integer, Seat> seats;
    private final Map<String, SeatHold> activeHolds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitlistEntry> waitlist;
    private final Event event;
    private final ReentrantReadWriteLock lock;
    private final ScheduledExecutorService executor;
    
    public SeatBookingEngine(Event event) {
        this.event = event;
        this.seats = new ConcurrentHashMap<>();
        this.activeHolds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitlist = new ConcurrentLinkedQueue<>();
        this.lock = new ReentrantReadWriteLock();
        this.executor = Executors.newScheduledThreadPool(1);
        
        for (int i = 1; i <= event.totalSeats; i++) {
            seats.put(i, new Seat(i));
        }
        
        executor.scheduleAtFixedRate(this::cleanExpiredHolds, 1, 1, TimeUnit.MINUTES);
    }
    
    public String holdSeats(int numSeats, TicketTier tier) {
        lock.writeLock().lock();
        try {
            cleanExpiredHolds();
            
            List<Integer> availableSeats = getAvailableSeatList(numSeats);
            
            if (availableSeats.size() < numSeats) {
                String entryId = "WL_" + UUID.randomUUID().toString();
                waitlist.add(new WaitlistEntry(entryId, numSeats, tier));
                return entryId;
            }
            
            for (int seatNumber : availableSeats) {
                seats.get(seatNumber).status = SeatStatus.HELD;
            }
            
            String holdId = "HLD_" + UUID.randomUUID().toString();
            LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
            activeHolds.put(holdId, new SeatHold(holdId, availableSeats, tier, expiresAt));
            
            return holdId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public String confirmBooking(String holdId) {
        lock.writeLock().lock();
        try {
            SeatHold hold = activeHolds.get(holdId);
            if (hold == null || hold.isExpired()) {
                return null;
            }
            
            for (int seatNumber : hold.seatNumbers) {
                seats.get(seatNumber).status = SeatStatus.BOOKED;
            }
            
            double price = hold.seatNumbers.size() * event.priceTiers.get(hold.tier);
            
            if (hold.seatNumbers.size() >= 10) {
                price *= 0.95;
            }
            
            String bookingId = "BK_" + UUID.randomUUID().toString();
            bookings.put(bookingId, new Booking(bookingId, hold.seatNumbers, hold.tier, 
                                                event.eventDate, price));
            
            activeHolds.remove(holdId);
            
            return bookingId;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public boolean releaseHold(String holdId) {
        lock.writeLock().lock();
        try {
            SeatHold hold = activeHolds.remove(holdId);
            if (hold == null) return false;
            
            for (int seatNumber : hold.seatNumbers) {
                seats.get(seatNumber).status = SeatStatus.AVAILABLE;
            }
            
            processWaitlist();
            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public double cancelBooking(String bookingId) {
        lock.writeLock().lock();
        try {
            Booking booking = bookings.remove(bookingId);
            if (booking == null) return -1;
            
            for (int seatNumber : booking.seatNumbers) {
                seats.get(seatNumber).status = SeatStatus.AVAILABLE;
            }
            
            double refund = booking.getRefundAmount();
            processWaitlist();
            
            return refund;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    public int getAvailableSeats() {
        lock.readLock().lock();
        try {
            return (int) seats.values().stream()
                .filter(s -> s.status == SeatStatus.AVAILABLE)
                .count();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public int getWaitlistPosition(String entryId) {
        lock.readLock().lock();
        try {
            int position = 1;
            for (WaitlistEntry entry : waitlist) {
                if (entry.entryId.equals(entryId)) return position;
                position++;
            }
            return -1;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private List<Integer> getAvailableSeatList(int numSeats) {
        List<Integer> available = new ArrayList<>();
        for (int i = 1; i <= event.totalSeats && available.size() < numSeats; i++) {
            if (seats.get(i).status == SeatStatus.AVAILABLE) {
                available.add(i);
            }
        }
        return available;
    }
    
    private void cleanExpiredHolds() {
        lock.writeLock().lock();
        try {
            activeHolds.entrySet().removeIf(entry -> {
                if (entry.getValue().isExpired()) {
                    for (int seatNumber : entry.getValue().seatNumbers) {
                        seats.get(seatNumber).status = SeatStatus.AVAILABLE;
                    }
                    processWaitlist();
                    return true;
                }
                return false;
            });
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private void processWaitlist() {
        while (!waitlist.isEmpty()) {
            int available = (int) seats.values().stream()
                .filter(s -> s.status == SeatStatus.AVAILABLE)
                .count();
            
            if (available == 0) break;
            
            WaitlistEntry entry = waitlist.peek();
            if (entry.requestedSeats <= available) {
                waitlist.poll();
                
                List<Integer> heldSeats = getAvailableSeatList(entry.requestedSeats);
                for (int seatNumber : heldSeats) {
                    seats.get(seatNumber).status = SeatStatus.HELD;
                }
                
                String holdId = "HLD_" + UUID.randomUUID().toString();
                LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
                activeHolds.put(holdId, new SeatHold(holdId, heldSeats, entry.tier, expiresAt));
            } else {
                break;
            }
        }
    }
    
    public void shutdown() {
        executor.shutdown();
    }
    
    public static void main(String[] args) throws InterruptedException {
        Map<TicketTier, Double> priceTiers = new HashMap<>();
        priceTiers.put(TicketTier.ADULT, 50.0);
        priceTiers.put(TicketTier.CHILD, 25.0);
        priceTiers.put(TicketTier.SENIOR, 35.0);
        priceTiers.put(TicketTier.STUDENT, 30.0);
        
        Event event = new Event("EVT001", LocalDateTime.now().plusDays(45), 100, priceTiers);
        SeatBookingEngine engine = new SeatBookingEngine(event);
        
        System.out.println("=== Seat Booking Engine Demo ===");
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        // Hold and confirm 5 adult seats
        String hold1 = engine.holdSeats(5, TicketTier.ADULT);
        System.out.println("\nHold 1 created: " + hold1);
        String booking1 = engine.confirmBooking(hold1);
        System.out.println("Booking 1 confirmed: " + booking1);
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        // Hold and confirm 12 student seats (group discount applied)
        String hold2 = engine.holdSeats(12, TicketTier.STUDENT);
        System.out.println("\nHold 2 created (12 seats for group discount): " + hold2);
        String booking2 = engine.confirmBooking(hold2);
        System.out.println("Booking 2 confirmed (5% group discount applied): " + booking2);
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        // Cancel first booking (refund should be 100% - more than 30 days before)
        double refund1 = engine.cancelBooking(booking1);
        System.out.println("\nBooking 1 cancelled. Refund: $" + refund1);
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        // Fill remaining seats to test waitlist
        for (int i = 0; i < 8; i++) {
            String hold = engine.holdSeats(10, TicketTier.ADULT);
            engine.confirmBooking(hold);
        }
        System.out.println("\nFilled most seats with group bookings");
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        // Try to book when sold out - should join waitlist
        String waitlistEntry = engine.holdSeats(5, TicketTier.ADULT);
        System.out.println("\nSold out - request joins waitlist: " + waitlistEntry);
        System.out.println("Waitlist position: " + engine.getWaitlistPosition(waitlistEntry));
        
        // Release a hold to free up seats for waitlist
        String hold3 = engine.holdSeats(3, TicketTier.CHILD);
        System.out.println("\nHold 3 created: " + hold3);
        engine.releaseHold(hold3);
        System.out.println("Hold 3 released - waitlist should be processed");
        System.out.println("Waitlist position for entry: " + engine.getWaitlistPosition(waitlistEntry));
        System.out.println("Available seats: " + engine.getAvailableSeats());
        
        engine.shutdown();
    }
}
```

**Key design decisions:**
- **Hold IDs** prefixed with `HLD_`, **Waitlist IDs** with `WL_` for easy identification
- **Automatic expiration** checked every minute via `ScheduledExecutorService`
- **Thread-safe** with `ReentrantReadWriteLock` for concurrent bookings
- **Group discount** applied at confirmation time (5% for 10+ seats)
- **Refund calculation** uses day boundary (≥7 days = 50%, >30 days = 100%)
- **Waitlist processing** triggered when seats are released or holds expire
- **Seat selection** automatic (lowest numbered available seats)