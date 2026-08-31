I'll build a complete event seat booking engine with all the specified features. Here are my assumptions:

- Seat selection prioritizes matching ticket types requested
- Holds and bookings use UUIDs for tracking
- The engine uses concurrent collections for thread safety
- Price tiers are: Adult €15, Child €8, Senior €10, Student €12 (reasonable defaults)
- Waiting list is processed FIFO as seats become available

```java
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

enum TicketType {
    ADULT(15.0),
    CHILD(8.0),
    SENIOR(10.0),
    STUDENT(12.0);

    private final double basePrice;

    TicketType(double basePrice) {
        this.basePrice = basePrice;
    }

    public double getBasePrice() {
        return basePrice;
    }
}

class Seat {
    private final String seatNumber;
    private final TicketType ticketType;
    private SeatStatus status;

    enum SeatStatus {
        AVAILABLE, HELD, CONFIRMED
    }

    public Seat(String seatNumber, TicketType ticketType) {
        this.seatNumber = seatNumber;
        this.ticketType = ticketType;
        this.status = SeatStatus.AVAILABLE;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public SeatStatus getStatus() {
        return status;
    }

    public void setStatus(SeatStatus status) {
        this.status = status;
    }
}

class Hold {
    private final String holdId;
    private final String customerId;
    private final List<Seat> seats;
    private final LocalDateTime expiresAt;
    private final double totalPrice;
    private HoldStatus status;

    enum HoldStatus {
        ACTIVE, EXPIRED, CONFIRMED, RELEASED
    }

    public Hold(String holdId, String customerId, List<Seat> seats, LocalDateTime expiresAt, double totalPrice) {
        this.holdId = holdId;
        this.customerId = customerId;
        this.seats = new ArrayList<>(seats);
        this.expiresAt = expiresAt;
        this.totalPrice = totalPrice;
        this.status = HoldStatus.ACTIVE;
    }

    public String getHoldId() {
        return holdId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public HoldStatus getStatus() {
        return status;
    }

    public void setStatus(HoldStatus status) {
        this.status = status;
    }

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt) && status == HoldStatus.ACTIVE;
    }
}

class Booking {
    private final String bookingId;
    private final String customerId;
    private final List<Seat> seats;
    private final double totalPrice;
    private final LocalDateTime bookedAt;
    private final LocalDateTime eventDate;
    private BookingStatus status;

    enum BookingStatus {
        CONFIRMED, CANCELLED
    }

    public Booking(String bookingId, String customerId, List<Seat> seats, double totalPrice, LocalDateTime bookedAt, LocalDateTime eventDate) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.seats = new ArrayList<>(seats);
        this.totalPrice = totalPrice;
        this.bookedAt = bookedAt;
        this.eventDate = eventDate;
        this.status = BookingStatus.CONFIRMED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public double calculateRefund(LocalDateTime cancelDate) {
        long daysUntilEvent = ChronoUnit.DAYS.between(cancelDate, eventDate);
        if (daysUntilEvent > 30) {
            return totalPrice;
        } else if (daysUntilEvent >= 7) {
            return totalPrice * 0.5;
        }
        return 0;
    }
}

class WaitListEntry {
    private final String waitListId;
    private final String customerId;
    private final List<TicketType> ticketTypes;
    private final LocalDateTime requestedAt;

    public WaitListEntry(String waitListId, String customerId, List<TicketType> ticketTypes, LocalDateTime requestedAt) {
        this.waitListId = waitListId;
        this.customerId = customerId;
        this.ticketTypes = new ArrayList<>(ticketTypes);
        this.requestedAt = requestedAt;
    }

    public String getWaitListId() {
        return waitListId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<TicketType> getTicketTypes() {
        return new ArrayList<>(ticketTypes);
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}

class Event {
    private final String eventId;
    private final String eventName;
    private final LocalDateTime eventDate;
    private final Map<String, Seat> seats;

    public Event(String eventId, String eventName, LocalDateTime eventDate) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.seats = new ConcurrentHashMap<>();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public Map<String, Seat> getSeats() {
        return new HashMap<>(seats);
    }

    public void addSeat(Seat seat) {
        seats.put(seat.getSeatNumber(), seat);
    }

    public int getAvailableSeatsCount() {
        return (int) seats.values().stream()
                .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE)
                .count();
    }

    public boolean isSoldOut() {
        return getAvailableSeatsCount() == 0;
    }
}

class BookingEngine {
    private final Event event;
    private final Map<String, Hold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitListEntry> waitList;
    private final ScheduledExecutorService executor;
    private static final int HOLD_DURATION_MINUTES = 15;
    private static final double GROUP_DISCOUNT_RATE = 0.05;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;

    public BookingEngine(Event event) {
        this.event = event;
        this.holds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitList = new ConcurrentLinkedQueue<>();
        this.executor = Executors.newScheduledThreadPool(1);
        startHoldExpirationChecker();
    }

    public synchronized Hold holdSeats(String customerId, List<TicketType> ticketTypes) {
        LocalDateTime now = LocalDateTime.now();
        expireHolds(now);

        if (ticketTypes.isEmpty() || ticketTypes.size() > event.getAvailableSeatsCount()) {
            return null;
        }

        List<Seat> selectedSeats = new ArrayList<>();
        for (TicketType type : ticketTypes) {
            Seat seat = event.getSeats().values().stream()
                    .filter(s -> s.getStatus() == Seat.SeatStatus.AVAILABLE && s.getTicketType() == type)
                    .findFirst()
                    .orElse(null);
            if (seat == null) {
                return null;
            }
            selectedSeats.add(seat);
        }

        selectedSeats.forEach(s -> s.setStatus(Seat.SeatStatus.HELD));

        double totalPrice = calculatePrice(selectedSeats, ticketTypes.size());
        LocalDateTime expiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);
        String holdId = UUID.randomUUID().toString();

        Hold hold = new Hold(holdId, customerId, selectedSeats, expiresAt, totalPrice);
        holds.put(holdId, hold);

        return hold;
    }

    public synchronized Booking confirmHold(String holdId) {
        Hold hold = holds.get(holdId);
        if (hold == null || hold.getStatus() != Hold.HoldStatus.ACTIVE) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        if (hold.isExpired(now)) {
            releaseHold(holdId);
            return null;
        }

        hold.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.CONFIRMED));

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, hold.getCustomerId(), hold.getSeats(), 
                hold.getTotalPrice(), now, event.getEventDate());
        bookings.put(bookingId, booking);

        hold.setStatus(Hold.HoldStatus.CONFIRMED);
        holds.remove(holdId);

        return booking;
    }

    public synchronized void releaseHold(String holdId) {
        Hold hold = holds.get(holdId);
        if (hold == null) {
            return;
        }

        hold.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
        hold.setStatus(Hold.HoldStatus.RELEASED);
        holds.remove(holdId);

        processWaitList();
    }

    public synchronized double cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        double refund = booking.calculateRefund(now);

        booking.getSeats().forEach(s -> s.setStatus(Seat.SeatStatus.AVAILABLE));
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        processWaitList();

        return refund;
    }

    public synchronized WaitListEntry joinWaitList(String customerId, List<TicketType> ticketTypes) {
        if (!event.isSoldOut()) {
            return null;
        }

        String waitListId = UUID.randomUUID().toString();
        WaitListEntry entry = new WaitListEntry(waitListId, customerId, ticketTypes, LocalDateTime.now());
        waitList.add(entry);

        return entry;
    }

    private synchronized void processWaitList() {
        while (!waitList.isEmpty() && !event.isSoldOut()) {
            WaitListEntry entry = waitList.poll();
            Hold hold = holdSeats(entry.getCustomerId(), entry.getTicketTypes());
            if (hold == null) {
                waitList.add(entry);
                break;
            }
        }
    }

    private synchronized void expireHolds(LocalDateTime now) {
        List<String> expiredHoldIds = holds.entrySet().stream()
                .filter(e -> e.getValue().isExpired(now))
                .map(Map.Entry::getKey)
                .toList();
        expiredHoldIds.forEach(this::releaseHold);
    }

    private void startHoldExpirationChecker() {
        executor.scheduleAtFixedRate(() -> expireHolds(LocalDateTime.now()), 
                HOLD_DURATION_MINUTES, 1, TimeUnit.MINUTES);
    }

    private double calculatePrice(List<Seat> seats, int seatCount) {
        double totalPrice = seats.stream()
                .mapToDouble(s -> s.getTicketType().getBasePrice())
                .sum();

        if (seatCount >= GROUP_DISCOUNT_THRESHOLD) {
            totalPrice *= (1 - GROUP_DISCOUNT_RATE);
        }

        return totalPrice;
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    public Hold getHold(String holdId) {
        return holds.get(holdId);
    }

    public int getWaitListSize() {
        return waitList.size();
    }

    public int getAvailableSeatsCount() {
        return event.getAvailableSeatsCount();
    }

    public void shutdown() {
        executor.shutdown();
    }
}
```

Key design decisions:
- Thread-safe with synchronized methods and concurrent collections
- Scheduled background task checks for hold expiration every minute
- Waiting list processes automatically when holds expire or bookings cancel
- Group discount applied at purchase time, included in hold price
- Refund logic: >30 days=100%, 7-30 days=50%, <7 days=0%