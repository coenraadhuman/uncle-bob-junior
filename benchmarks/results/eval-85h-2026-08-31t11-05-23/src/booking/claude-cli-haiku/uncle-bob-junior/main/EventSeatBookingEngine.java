import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class EventSeatBookingEngine {
    private final LocalDateTime eventDate;
    private final Map<String, Seat> seats;
    private final Map<String, Hold> activeHolds;
    private final Map<String, Booking> bookings;
    private final WaitingList waitingList;
    private final PriceCalculator priceCalculator;
    private final RefundCalculator refundCalculator;

    private static final int HOLD_DURATION_MINUTES = 15;

    public EventSeatBookingEngine(LocalDateTime eventDate, List<String> seatIds,
                                   Map<TicketType, BigDecimal> pricesByType) {
        this.eventDate = eventDate;
        this.seats = new HashMap<>();
        this.activeHolds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitingList = new WaitingList();
        this.priceCalculator = new PriceCalculator(pricesByType);
        this.refundCalculator = new RefundCalculator();
        
        seatIds.forEach(id -> seats.put(id, new Seat(id)));
    }

    public BookingResult holdSeats(int count, List<TicketType> ticketTypes) {
        if (ticketTypes.size() != count) {
            return BookingResult.failure("Ticket type count must equal seat count");
        }

        LocalDateTime now = LocalDateTime.now();
        expireOldHolds(now);

        List<String> available = findAvailableSeats(count);
        if (available.size() < count) {
            String entryId = UUID.randomUUID().toString();
            waitingList.add(new WaitingListEntry(entryId, count, ticketTypes));
            return BookingResult.waitlisted(entryId);
        }

        return createHold(available, ticketTypes, now);
    }

    private BookingResult createHold(List<String> seatIds, List<TicketType> ticketTypes, LocalDateTime now) {
        String holdId = UUID.randomUUID().toString();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);
        Hold hold = new Hold(holdId, seatIds, ticketTypes, expiresAt);

        seatIds.forEach(seatId -> seats.get(seatId).hold());
        activeHolds.put(holdId, hold);

        BigDecimal price = priceCalculator.calculateTotal(ticketTypes);
        return BookingResult.held(holdId, price);
    }

    public BookingResult confirmBooking(String holdId) {
        Hold hold = activeHolds.get(holdId);
        if (hold == null) {
            return BookingResult.failure("Hold not found");
        }

        LocalDateTime now = LocalDateTime.now();
        if (hold.isExpired(now)) {
            releaseHold(holdId);
            return BookingResult.failure("Hold expired");
        }

        return bookSeatsFromHold(holdId, hold);
    }

    private BookingResult bookSeatsFromHold(String holdId, Hold hold) {
        String bookingId = UUID.randomUUID().toString();
        BigDecimal totalPrice = priceCalculator.calculateTotal(hold.ticketTypes());
        Booking booking = new Booking(bookingId, hold.seatIds(), hold.ticketTypes(), totalPrice);

        hold.seatIds().forEach(seatId -> seats.get(seatId).book());
        activeHolds.remove(holdId);
        bookings.put(bookingId, booking);

        processWaitingList();
        return BookingResult.confirmed(bookingId, totalPrice);
    }

    public BookingResult releaseHold(String holdId) {
        Hold hold = activeHolds.remove(holdId);
        if (hold == null) {
            return BookingResult.failure("Hold not found");
        }

        hold.seatIds().forEach(seatId -> seats.get(seatId).release());
        processWaitingList();
        return BookingResult.released("Hold released");
    }

    public BookingResult cancelBooking(String bookingId) {
        Booking booking = bookings.remove(bookingId);
        if (booking == null) {
            return BookingResult.failure("Booking not found");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal refund = refundCalculator.calculateRefund(booking.paidAmount(), eventDate, now);

        booking.seatIds().forEach(seatId -> seats.get(seatId).release());
        processWaitingList();

        return BookingResult.cancelled(refund);
    }

    private void expireOldHolds(LocalDateTime now) {
        activeHolds.values().stream()
            .filter(hold -> hold.isExpired(now))
            .map(Hold::id)
            .collect(Collectors.toList())
            .forEach(this::releaseHold);
    }

    private List<String> findAvailableSeats(int count) {
        return seats.values().stream()
            .filter(seat -> seat.status() == SeatStatus.AVAILABLE)
            .map(Seat::id)
            .limit(count)
            .collect(Collectors.toList());
    }

    private void processWaitingList() {
        while (!waitingList.isEmpty()) {
            Optional<WaitingListEntry> entry = waitingList.next();
            if (entry.isEmpty()) break;

            WaitingListEntry waitEntry = entry.get();
            List<String> available = findAvailableSeats(waitEntry.count());

            if (available.size() < waitEntry.count()) {
                waitingList.add(waitEntry);
                break;
            }

            createHold(available, waitEntry.ticketTypes(), LocalDateTime.now());
        }
    }

    Map<String, Hold> activeHolds() { return new HashMap<>(activeHolds); }
    Map<String, Booking> bookings() { return new HashMap<>(bookings); }
    int availableSeatCount() {
        return (int) seats.values().stream().filter(s -> s.status() == SeatStatus.AVAILABLE).count();
    }
}
