import java.time.*;
import java.util.*;
import java.util.concurrent.*;

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
