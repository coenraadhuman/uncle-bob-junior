I'll build a complete event seat booking engine with all the specified requirements. Here are my key assumptions:

- Single event focus (extendable to multiple)
- Prices in EUR: Adult €100, Child €50, Senior €75, Student €60
- Background task checks for expired holds every 30 seconds
- Thread-safe using synchronized collections
- Refunds calculated based on days until event

```java
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

public enum TicketTier {
    ADULT(100.0),
    CHILD(50.0),
    SENIOR(75.0),
    STUDENT(60.0);

    private final double basePrice;

    TicketTier(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}

public enum SeatStatus {
    AVAILABLE,
    HELD,
    BOOKED,
    RELEASED
}

public class Seat {
    private final String seatId;
    private SeatStatus status;
    private String holdId;
    private String bookingId;

    public Seat(String seatId) {
        this.seatId = seatId;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatId() { return seatId; }
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }
    public String getHoldId() { return holdId; }
    public void setHoldId(String holdId) { this.holdId = holdId; }
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
}

public class Hold {
    private final String holdId;
    private final List<String> seatIds;
    private final TicketTier ticketTier;
    private final long expiresAt;
    private boolean released;

    public Hold(String holdId, List<String> seatIds, TicketTier ticketTier) {
        this.holdId = holdId;
        this.seatIds = new ArrayList<>(seatIds);
        this.ticketTier = ticketTier;
        this.expiresAt = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutes
        this.released = false;
    }

    public String getHoldId() { return holdId; }
    public List<String> getSeatIds() { return new ArrayList<>(seatIds); }
    public TicketTier getTicketTier() { return ticketTier; }
    public boolean isExpired() { return System.currentTimeMillis() > expiresAt; }
    public boolean isReleased() { return released; }
    public void release() { this.released = true; }
}

public class Booking {
    private final String bookingId;
    private final List<String> seatIds;
    private final TicketTier ticketTier;
    private final double totalPrice;
    private final LocalDateTime bookingDate;
    private final LocalDateTime eventDate;

    public Booking(String bookingId, List<String> seatIds, TicketTier ticketTier,
                   double totalPrice, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.seatIds = new ArrayList<>(seatIds);
        this.ticketTier = ticketTier;
        this.totalPrice = totalPrice;
        this.bookingDate = LocalDateTime.now();
        this.eventDate = eventDate;
    }

    public String getBookingId() { return bookingId; }
    public List<String> getSeatIds() { return new ArrayList<>(seatIds); }
    public TicketTier getTicketTier() { return ticketTier; }
    public double getTotalPrice() { return totalPrice; }
    public LocalDateTime getBookingDate() { return bookingDate; }
    public LocalDateTime getEventDate() { return eventDate; }

    public double calculateRefund() {
        long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
        if (daysUntilEvent > 30) {
            return totalPrice; // 100% refund
        } else if (daysUntilEvent >= 7) {
            return totalPrice * 0.5; // 50% refund
        } else {
            return 0.0; // No refund
        }
    }
}

public class WaitlistEntry {
    private final String waitlistId;
    private final int seatsRequested;
    private final TicketTier ticketTier;
    private final LocalDateTime joinedAt;

    public WaitlistEntry(String waitlistId, int seatsRequested, TicketTier ticketTier) {
        this.waitlistId = waitlistId;
        this.seatsRequested = seatsRequested;
        this.ticketTier = ticketTier;
        this.joinedAt = LocalDateTime.now();
    }

    public String getWaitlistId() { return waitlistId; }
    public int getSeatsRequested() { return seatsRequested; }
    public TicketTier getTicketTier() { return ticketTier; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}

public class Event {
    private final String eventId;
    private final LocalDateTime eventDate;
    private final Map<String, Seat> seats;

    public Event(String eventId, LocalDateTime eventDate, int totalSeats) {
        this.eventId = eventId;
        this.eventDate = eventDate;
        this.seats = new ConcurrentHashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            this.seats.put("SEAT_" + i, new Seat("SEAT_" + i));
        }
    }

    public String getEventId() { return eventId; }
    public LocalDateTime getEventDate() { return eventDate; }
    public Map<String, Seat> getSeats() { return seats; }
    public int getTotalSeats() { return seats.size(); }
}

public class SeatBookingService {
    private final Event event;
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();
    private final Map<String, Booking> bookings = new ConcurrentHashMap<>();
    private final Queue<WaitlistEntry> waitlist = new ConcurrentLinkedQueue<>();
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public SeatBookingService(Event event) {
        this.event = event;
        startHoldExpirationChecker();
    }

    public Hold holdSeats(int count, TicketTier tier) throws SeatBookingException {
        if (count <= 0) {
            throw new SeatBookingException("Seat count must be positive");
        }

        List<String> availableSeats = findAvailableSeats(count);

        if (availableSeats.size() < count) {
            throw new SeatBookingException("Not enough available seats. Available: " +
                    availableSeats.size() + ", requested: " + count);
        }

        String holdId = UUID.randomUUID().toString();
        Hold hold = new Hold(holdId, availableSeats, tier);

        for (String seatId : availableSeats) {
            Seat seat = event.getSeats().get(seatId);
            seat.setStatus(SeatStatus.HELD);
            seat.setHoldId(holdId);
        }

        holds.put(holdId, hold);
        return hold;
    }

    public Booking confirmBooking(String holdId) throws SeatBookingException {
        Hold hold = holds.get(holdId);
        if (hold == null) {
            throw new SeatBookingException("Hold not found: " + holdId);
        }

        if (hold.isExpired()) {
            releaseHold(holdId);
            throw new SeatBookingException("Hold has expired");
        }

        if (hold.isReleased()) {
            throw new SeatBookingException("Hold has been released");
        }

        List<String> seatIds = hold.getSeatIds();
        double basePrice = hold.getTicketTier().getBasePrice() * seatIds.size();
        double totalPrice = basePrice;

        if (seatIds.size() >= 10) {
            totalPrice = basePrice * 0.95; // 5% group discount
        }

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, seatIds, hold.getTicketTier(),
                totalPrice, event.getEventDate());

        for (String seatId : seatIds) {
            Seat seat = event.getSeats().get(seatId);
            seat.setStatus(SeatStatus.BOOKED);
            seat.setBookingId(bookingId);
        }

        bookings.put(bookingId, booking);
        holds.remove(holdId);
        return booking;
    }

    public void releaseHold(String holdId) throws SeatBookingException {
        Hold hold = holds.get(holdId);
        if (hold == null) {
            throw new SeatBookingException("Hold not found: " + holdId);
        }

        hold.release();
        for (String seatId : hold.getSeatIds()) {
            Seat seat = event.getSeats().get(seatId);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setHoldId(null);
        }

        holds.remove(holdId);
        processWaitlist();
    }

    public double cancelBooking(String bookingId) throws SeatBookingException {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new SeatBookingException("Booking not found: " + bookingId);
        }

        double refund = booking.calculateRefund();

        for (String seatId : booking.getSeatIds()) {
            Seat seat = event.getSeats().get(seatId);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setBookingId(null);
        }

        bookings.remove(bookingId);
        processWaitlist();
        return refund;
    }

    public String addToWaitlist(int seatsRequested, TicketTier tier) throws SeatBookingException {
        if (seatsRequested <= 0) {
            throw new SeatBookingException("Seat count must be positive");
        }

        String waitlistId = UUID.randomUUID().toString();
        WaitlistEntry entry = new WaitlistEntry(waitlistId, seatsRequested, tier);
        waitlist.offer(entry);
        return waitlistId;
    }

    public int getAvailableSeatsCount() {
        return (int) event.getSeats().values().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .count();
    }

    public int getBookedSeatsCount() {
        return (int) event.getSeats().values().stream()
                .filter(s -> s.getStatus() == SeatStatus.BOOKED)
                .count();
    }

    public int getHeldSeatsCount() {
        return (int) event.getSeats().values().stream()
                .filter(s -> s.getStatus() == SeatStatus.HELD)
                .count();
    }

    private List<String> findAvailableSeats(int count) {
        return event.getSeats().values().stream()
                .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                .map(Seat::getSeatId)
                .limit(count)
                .toList();
    }

    private void startHoldExpirationChecker() {
        executor.scheduleAtFixedRate(this::expireHolds, 30, 30, TimeUnit.SECONDS);
    }

    private void expireHolds() {
        List<String> expiredHolds = new ArrayList<>();
        for (Map.Entry<String, Hold> entry : holds.entrySet()) {
            if (entry.getValue().isExpired()) {
                expiredHolds.add(entry.getKey());
            }
        }

        for (String holdId : expiredHolds) {
            try {
                releaseHold(holdId);
            } catch (SeatBookingException e) {
                // Log and continue
            }
        }
    }

    private synchronized void processWaitlist() {
        while (!waitlist.isEmpty()) {
            WaitlistEntry entry = waitlist.peek();
            List<String> availableSeats = findAvailableSeats(entry.getSeatsRequested());

            if (availableSeats.size() >= entry.getSeatsRequested()) {
                waitlist.poll();
                try {
                    holdSeats(entry.getSeatsRequested(), entry.getTicketTier());
                } catch (SeatBookingException e) {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}

public class SeatBookingException extends Exception {
    public SeatBookingException(String message) {
        super(message);
    }
}

public class SeatBookingEngineDemo {
    public static void main(String[] args) throws SeatBookingException {
        Event event = new Event("EVENT_001", LocalDateTime.now().plusDays(45), 100);
        SeatBookingService service = new SeatBookingService(event);

        System.out.println("=== Seat Booking Engine Demo ===\n");

        // Test 1: Hold seats
        System.out.println("Test 1: Hold 5 adult seats");
        Hold hold1 = service.holdSeats(5, TicketTier.ADULT);
        System.out.println("Hold ID: " + hold1.getHoldId());
        System.out.println("Available seats: " + service.getAvailableSeatsCount());
        System.out.println("Held seats: " + service.getHeldSeatsCount() + "\n");

        // Test 2: Confirm booking with group discount
        System.out.println("Test 2: Confirm hold for 10+ seats (with discount)");
        Hold hold2 = service.holdSeats(12, TicketTier.CHILD);
        Booking booking1 = service.confirmBooking(hold2.getHoldId());
        System.out.println("Booking ID: " + booking1.getBookingId());
        System.out.println("Price: €" + String.format("%.2f", booking1.getTotalPrice()));
        System.out.println("Expected (with 5% discount): €" + String.format("%.2f", 50 * 12 * 0.95));
        System.out.println("Booked seats: " + service.getBookedSeatsCount() + "\n");

        // Test 3: Release hold
        System.out.println("Test 3: Release the 5-seat hold");
        service.releaseHold(hold1.getHoldId());
        System.out.println("Available seats after release: " + service.getAvailableSeatsCount() + "\n");

        // Test 4: Cancel booking with refund
        System.out.println("Test 4: Cancel booking (45 days before event = 100% refund)");
        double refund = service.cancelBooking(booking1.getBookingId());
        System.out.println("Refund amount: €" + String.format("%.2f", refund));
        System.out.println("Available seats: " + service.getAvailableSeatsCount() + "\n");

        // Test 5: Sold out and waitlist
        System.out.println("Test 5: Fill remaining seats and use waitlist");
        int remaining = service.getAvailableSeatsCount();
        Hold hold3 = service.holdSeats(remaining, TicketTier.SENIOR);
        Booking booking2 = service.confirmBooking(hold3.getHoldId());
        System.out.println("Event is now sold out");
        System.out.println("Available seats: " + service.getAvailableSeatsCount());

        String waitlistId = service.addToWaitlist(5, TicketTier.STUDENT);
        System.out.println("Added 5 students to waitlist: " + waitlistId);
        System.out.println("Waitlist size: 1\n");

        service.shutdown();
        System.out.println("Demo completed!");
    }
}
```

**Key design decisions:**

- **Thread safety:** ConcurrentHashMap/Queue for concurrent access
- **Hold expiration:** Background scheduler checks every 30 seconds
- **Pricing:** Base price × quantity × discount factor (5% for 10+)
- **Refunds:** Progressive based on days until event (>30=100%, 7-30=50%, <7=0%)
- **Waitlist:** FIFO queue automatically processed when seats free up
- **Status tracking:** Seats progress through AVAILABLE → HELD → BOOKED/RELEASED
- **Error handling:** Custom exceptions for invalid operations