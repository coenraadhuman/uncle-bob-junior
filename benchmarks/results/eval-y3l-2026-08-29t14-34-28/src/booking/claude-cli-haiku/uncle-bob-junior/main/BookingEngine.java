import java.time.*;
import java.util.*;

class BookingEngine {
    private final Map<String, Seat> seats;
    private final Map<String, Hold> holds;
    private final Map<String, Booking> bookings;
    private final Queue<WaitingListEntry> waitlist;
    private final LocalDateTime eventDate;
    private final Clock clock;
    private final IdSource ids;

    BookingEngine(LocalDateTime eventDate, List<Seat> seatList, Clock clock, IdSource ids) {
        this.eventDate = eventDate;
        this.clock = clock;
        this.ids = ids;
        this.seats = new HashMap<>();
        this.holds = new HashMap<>();
        this.bookings = new HashMap<>();
        this.waitlist = new LinkedList<>();

        for (Seat s : seatList) {
            seats.put(s.id(), s);
        }
    }

    HoldResult hold(List<String> seatIds) {
        LocalDateTime now = LocalDateTime.now(clock);
        expireOldHolds(now);

        List<Seat> toHold = lookupSeats(seatIds);
        if (!allAvailable(toHold)) {
            if (event().isSoldOut()) {
                return HoldResult.soldOut();
            }
            return HoldResult.unavailable();
        }

        String holdId = ids.next();
        LocalDateTime expiresAt = now.plusMinutes(15);

        for (Seat s : toHold) {
            s.setStatus(SeatStatus.HELD);
            holds.put(holdId + ":" + s.id(), new Hold(holdId, s, expiresAt));
        }

        return HoldResult.held(holdId, expiresAt);
    }

    ConfirmResult confirm(String holdId, TicketType type) {
        LocalDateTime now = LocalDateTime.now(clock);
        expireOldHolds(now);

        List<Seat> heldSeats = seatsInHold(holdId);
        if (heldSeats.isEmpty()) {
            return ConfirmResult.notFound();
        }

        Money price = calculatePrice(heldSeats.size(), type);
        String bookingId = ids.next();
        Booking booking = new Booking(bookingId, heldSeats, type, price, now, eventDate);
        bookings.put(bookingId, booking);

        for (Seat s : heldSeats) {
            s.setStatus(SeatStatus.BOOKED);
        }

        removeHold(holdId);
        offerWaitlist(now);

        return ConfirmResult.confirmed(bookingId, price);
    }

    CancelResult cancel(String bookingId) {
        LocalDateTime now = LocalDateTime.now(clock);

        Booking booking = bookings.get(bookingId);
        if (booking == null || !booking.isConfirmed()) {
            return CancelResult.notFound();
        }

        Money refund = booking.refundAmount(now);
        booking.cancel();

        for (Seat s : booking.seats()) {
            s.setStatus(SeatStatus.AVAILABLE);
        }

        offerWaitlist(now);
        return CancelResult.refunded(refund);
    }

    WaitlistResult waitlist(int count, TicketType type) {
        LocalDateTime now = LocalDateTime.now(clock);
        String entryId = ids.next();
        this.waitlist.offer(new WaitingListEntry(entryId, count, type, now));
        return WaitlistResult.queued(entryId, this.waitlist.size());
    }

    int availableSeats() {
        return (int) seats.values().stream().filter(Seat::isAvailable).count();
    }

    int waitlistSize() {
        return waitlist.size();
    }

    Map<String, Booking> allBookings() {
        return new HashMap<>(bookings);
    }

    private EventSnapshot event() {
        return new EventSnapshot(availableSeats(), bookings.size(), waitlist.size());
    }

    private List<Seat> lookupSeats(List<String> seatIds) {
        return seatIds.stream()
                .map(seats::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean allAvailable(List<Seat> toCheck) {
        return toCheck.stream().allMatch(Seat::isAvailable);
    }

    private List<Seat> seatsInHold(String holdId) {
        return holds.entrySet().stream()
                .filter(e -> e.getKey().startsWith(holdId + ":"))
                .map(e -> e.getValue().seat())
                .toList();
    }

    private void removeHold(String holdId) {
        holds.entrySet().removeIf(e -> e.getKey().startsWith(holdId + ":"));
    }

    private void expireOldHolds(LocalDateTime now) {
        List<String> expired = new ArrayList<>();
        for (var e : holds.entrySet()) {
            if (e.getValue().isExpired(now)) {
                String seatKey = e.getKey();
                String seatId = seatKey.substring(seatKey.indexOf(":") + 1);
                Seat s = seats.get(seatId);
                if (s.status() == SeatStatus.HELD) {
                    s.setStatus(SeatStatus.AVAILABLE);
                }
                expired.add(e.getKey());
            }
        }
        expired.forEach(holds::remove);
    }

    private void offerWaitlist(LocalDateTime now) {
        while (!waitlist.isEmpty() && availableSeats() > 0) {
            WaitingListEntry entry = waitlist.peek();
            if (entry.count() <= availableSeats()) {
                waitlist.poll();
                String holdId = ids.next();
                List<Seat> available = seats.values().stream()
                        .filter(Seat::isAvailable)
                        .limit(entry.count())
                        .toList();
                LocalDateTime expiresAt = now.plusMinutes(15);
                for (Seat s : available) {
                    s.setStatus(SeatStatus.HELD);
                    holds.put(holdId + ":" + s.id(), new Hold(holdId, s, expiresAt));
                }
            } else {
                break;
            }
        }
    }

    private Money calculatePrice(int count, TicketType type) {
        double basePrice = count * type.getPrice();
        Money total = new Money(basePrice);
        if (count >= 10) {
            return total.discounted(5.0);
        }
        return total;
    }
}
