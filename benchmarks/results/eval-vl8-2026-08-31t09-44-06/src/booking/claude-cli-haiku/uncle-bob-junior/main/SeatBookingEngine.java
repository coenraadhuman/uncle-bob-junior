public class SeatBookingEngine {
    private static final int HOLD_DURATION_MILLIS = 15 * 60 * 1000;
    private static final int GROUP_DISCOUNT_THRESHOLD = 10;
    private static final double GROUP_DISCOUNT_PERCENT = 5.0;

    private final String eventId;
    private final long eventTimeMillis;
    private final Map<String, Seat> seats = new HashMap<>();
    private final Map<String, SeatHold> holds = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();
    private final Queue<WaitingListEntry> waitingList = new LinkedList<>();
    private final RefundPolicy refundPolicy = new RefundPolicy();

    public SeatBookingEngine(String eventId, long eventTimeMillis, List<String> seatIds) {
        this.eventId = eventId;
        this.eventTimeMillis = eventTimeMillis;
        for (String seatId : seatIds) {
            seats.put(seatId, new Seat(seatId));
        }
    }

    public SeatHoldResult hold(List<TicketType> ticketTypes) {
        cleanExpiredHolds();

        List<String> availableSeats = findAvailableSeats(ticketTypes.size());

        if (availableSeats.size() < ticketTypes.size()) {
            waitingList.offer(new WaitingListEntry(ticketTypes, System.currentTimeMillis()));
            return SeatHoldResult.waitingList();
        }

        String holdId = UUID.randomUUID().toString();
        long expiryTime = System.currentTimeMillis() + HOLD_DURATION_MILLIS;

        for (String seatId : availableSeats) {
            seats.get(seatId).hold();
            holds.put(seatId, new SeatHold(holdId, seatId, expiryTime));
        }

        return SeatHoldResult.held(holdId, availableSeats, expiryTime);
    }

    public BookingResult confirm(String holdId, List<TicketType> ticketTypes) {
        List<String> heldSeats = findSeatsForHold(holdId);

        if (heldSeats.isEmpty()) {
            return BookingResult.error("Hold not found: " + holdId);
        }

        if (heldSeats.size() != ticketTypes.size()) {
            return BookingResult.error("Ticket count mismatch");
        }

        String bookingId = UUID.randomUUID().toString();
        Money totalPrice = calculateTotalPrice(ticketTypes);

        for (int i = 0; i < heldSeats.size(); i++) {
            String seatId = heldSeats.get(i);
            Seat seat = seats.get(seatId);
            seat.book();

            Money seatPrice = calculateSeatPrice(ticketTypes.get(i), ticketTypes.size());
            bookings.put(seatId, new Booking(bookingId, seatId, ticketTypes.get(i), seatPrice, System.currentTimeMillis()));
            holds.remove(seatId);
        }

        return BookingResult.confirmed(bookingId, totalPrice);
    }

    public void release(String holdId) {
        List<String> heldSeats = findSeatsForHold(holdId);

        for (String seatId : heldSeats) {
            seats.get(seatId).release();
            holds.remove(seatId);
        }

        processWaitingList();
    }

    public RefundResult cancel(String seatId) {
        Booking booking = bookings.get(seatId);

        if (booking == null) {
            return RefundResult.error("Seat not booked: " + seatId);
        }

        double refundPercent = refundPolicy.refundPercent(eventTimeMillis, System.currentTimeMillis());
        Money refundAmount = booking.seatPrice().applyPercent(refundPercent);

        seats.get(seatId).release();
        bookings.remove(seatId);

        processWaitingList();

        return RefundResult.refunded(refundAmount, refundPercent);
    }

    private Money calculateTotalPrice(List<TicketType> ticketTypes) {
        Money total = new Money(0);
        for (TicketType type : ticketTypes) {
            total = total.plus(new Money(type.priceInCents()));
        }

        if (ticketTypes.size() >= GROUP_DISCOUNT_THRESHOLD) {
            Money discount = total.applyPercent(GROUP_DISCOUNT_PERCENT);
            total = new Money(total.cents() - discount.cents());
        }

        return total;
    }

    private Money calculateSeatPrice(TicketType type, int totalTickets) {
        Money basePrice = new Money(type.priceInCents());

        if (totalTickets >= GROUP_DISCOUNT_THRESHOLD) {
            Money discount = basePrice.applyPercent(GROUP_DISCOUNT_PERCENT);
            return new Money(basePrice.cents() - discount.cents());
        }

        return basePrice;
    }

    private List<String> findAvailableSeats(int count) {
        return seats.values().stream()
            .filter(s -> s.status() == SeatStatus.AVAILABLE)
            .limit(count)
            .map(Seat::id)
            .toList();
    }

    private List<String> findSeatsForHold(String holdId) {
        return holds.values().stream()
            .filter(h -> h.holdId().equals(holdId))
            .map(SeatHold::seatId)
            .toList();
    }

    private void cleanExpiredHolds() {
        long now = System.currentTimeMillis();
        List<String> expired = holds.values().stream()
            .filter(h -> h.isExpired(now))
            .map(SeatHold::seatId)
            .toList();

        for (String seatId : expired) {
            seats.get(seatId).release();
            holds.remove(seatId);
        }
    }

    private void processWaitingList() {
        while (!waitingList.isEmpty()) {
            WaitingListEntry entry = waitingList.peek();
            List<String> available = findAvailableSeats(entry.ticketTypes().size());

            if (available.size() < entry.ticketTypes().size()) {
                break;
            }

            waitingList.poll();

            String holdId = UUID.randomUUID().toString();
            long expiryTime = System.currentTimeMillis() + HOLD_DURATION_MILLIS;

            for (String seatId : available) {
                seats.get(seatId).hold();
                holds.put(seatId, new SeatHold(holdId, seatId, expiryTime));
            }
        }
    }
}
