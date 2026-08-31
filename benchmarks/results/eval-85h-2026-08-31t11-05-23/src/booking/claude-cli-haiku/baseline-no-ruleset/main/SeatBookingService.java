import java.util.*;
import java.util.concurrent.*;

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
