// SeatBookingEngine.java
import java.util.*;
import java.util.stream.*;

public final class SeatBookingEngine {
    private static final int HOLD_DURATION_MINUTES = 15;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_PERCENT = 0.05;

    private final java.util.Map<String, Event> events;
    private final java.util.Map<String, SeatHold> holds;
    private final java.util.Map<String, Booking> bookings;
    private final RefundPolicy refundPolicy;

    public SeatBookingEngine() {
        this.events = new HashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.refundPolicy = new RefundPolicy();
    }

    public void createEvent(String eventId, String name, long eventDateMillis, int capacity) {
        events.put(eventId, new Event(eventId, name, eventDateMillis, capacity));
    }

    public SeatHold holdSeats(String eventId, java.util.List<TicketType> ticketTypes, String customerId) {
        Event event = findEventOrThrow(eventId);
        expireOldHolds(event);
        if (event.isSoldOut()) {
            addToWaitList(event, customerId, ticketTypes);
            throw new IllegalStateException("Event is sold out");
        }

        java.util.List<String> seatsToHold = findAvailableSeats(event, ticketTypes.size());
        if (seatsToHold.size() < ticketTypes.size()) {
            addToWaitList(event, customerId, ticketTypes);
            throw new IllegalStateException("Not enough available seats");
        }

        String holdId = generateId("HOLD");
        long expiresAt = System.currentTimeMillis() + (HOLD_DURATION_MINUTES * 60 * 1000L);
        SeatHold hold = new SeatHold(holdId, eventId, seatsToHold, expiresAt, customerId);

        markSeatsAsHeld(event, seatsToHold);
        holds.put(holdId, hold);
        return hold;
    }

    public Booking confirmHold(String holdId, String customerId) {
        SeatHold hold = findHoldOrThrow(holdId);
        validateCustomer(hold, customerId);
        Event event = findEventOrThrow(hold.eventId());

        if (hold.isExpired(System.currentTimeMillis())) {
            releaseHold(holdId);
            throw new IllegalStateException("Hold has expired");
        }

        Money totalPrice = calculatePrice(hold.seatCount());
        String bookingId = generateId("BOOKING");
        Booking booking = new Booking(bookingId, event.id(), hold.seatIds(),
                                      createTicketTypes(hold.seatCount()),
                                      totalPrice, customerId, System.currentTimeMillis());

        markSeatsAsBooked(event, hold.seatIds());
        bookings.put(bookingId, booking);
        holds.remove(holdId);
        notifyWaitList(event);

        return booking;
    }

    public void releaseHold(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) return;

        Event event = events.get(hold.eventId());
        if (event != null) {
            markSeatsAsAvailable(event, hold.seatIds());
        }
        holds.remove(holdId);
    }

    public Money cancelBooking(String bookingId) {
        Booking booking = findBookingOrThrow(bookingId);
        Event event = findEventOrThrow(booking.eventId());

        Money refund = refundPolicy.calculateRefund(booking.totalPrice(),
                                                     event.eventDateMillis(),
                                                     System.currentTimeMillis());

        markSeatsAsAvailable(event, booking.seatIds());
        bookings.remove(bookingId);
        notifyWaitList(event);

        return refund;
    }

    private void expireOldHolds(Event event) {
        long now = System.currentTimeMillis();
        holds.values().stream()
            .filter(h -> h.eventId().equals(event.id()) && h.isExpired(now))
            .map(SeatHold::id)
            .collect(Collectors.toList())
            .forEach(this::releaseHold);
    }

    private void notifyWaitList(Event event) {
        while (!event.isSoldOut()) {
            WaitListEntry entry = event.pollWaitList();
            if (entry == null) break;

            List<String> available = findAvailableSeats(event, entry.requestedSeats());
            if (available.size() < entry.requestedSeats()) {
                event.addToWaitList(entry);
                break;
            }

            autoBookWaitListEntry(event, entry, available);
        }
    }

    private void autoBookWaitListEntry(Event event, WaitListEntry entry, List<String> seats) {
        String bookingId = generateId("BOOKING");
        Money price = calculatePrice(entry.requestedSeats());
        Booking booking = new Booking(bookingId, event.id(), seats,
                                      createTicketTypes(entry.requestedSeats()),
                                      price, entry.customerId(), System.currentTimeMillis());
        markSeatsAsBooked(event, seats);
        bookings.put(bookingId, booking);
    }

    private List<String> findAvailableSeats(Event event, int count) {
        return event.seats().values().stream()
            .filter(Seat::isAvailable)
            .map(Seat::id)
            .limit(count)
            .collect(Collectors.toList());
    }

    private void markSeatsAsHeld(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.HELD)));
    }

    private void markSeatsAsBooked(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.BOOKED)));
    }

    private void markSeatsAsAvailable(Event event, List<String> seatIds) {
        seatIds.forEach(id -> event.updateSeat(id, event.seats().get(id).withState(SeatState.AVAILABLE)));
    }

    private Money calculatePrice(int seatCount) {
        Money basePrice = new Money(TicketType.ADULT.priceInCents() * seatCount);
        if (seatCount >= GROUP_DISCOUNT_THRESHOLD) {
            return basePrice.multiply(1.0 - GROUP_DISCOUNT_PERCENT);
        }
        return basePrice;
    }

    private List<TicketType> createTicketTypes(int count) {
        List<TicketType> types = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            types.add(TicketType.ADULT);
        }
        return types;
    }

    private void addToWaitList(Event event, String customerId, List<TicketType> ticketTypes) {
        WaitListEntry entry = new WaitListEntry(customerId, ticketTypes.size(), ticketTypes,
                                                 System.currentTimeMillis());
        event.addToWaitList(entry);
    }

    private Event findEventOrThrow(String eventId) {
        Event event = events.get(eventId);
        if (event == null) throw new IllegalArgumentException("Event not found: " + eventId);
        return event;
    }

    private SeatHold findHoldOrThrow(String holdId) {
        SeatHold hold = holds.get(holdId);
        if (hold == null) throw new IllegalArgumentException("Hold not found: " + holdId);
        return hold;
    }

    private Booking findBookingOrThrow(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) throw new IllegalArgumentException("Booking not found: " + bookingId);
        return booking;
    }

    private void validateCustomer(SeatHold hold, String customerId) {
        if (!hold.customerId().equals(customerId)) {
            throw new IllegalArgumentException("Customer mismatch");
        }
    }

    private String generateId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    public Booking getBooking(String bookingId) {
        return bookings.get(bookingId);
    }

    public Event getEvent(String eventId) {
        return events.get(eventId);
    }
}
