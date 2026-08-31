import java.time.*;
import java.util.*;

// ============ BOOKING SERVICE ============

class SeatBookingService {
    private final LocalDate eventDate;
    private final Map<String, Seat> seatsById;
    private final Map<String, Hold> holdsById;
    private final Map<String, Booking> bookingsById;
    private final Queue<WaitlistEntry> waitlist;
    private final PriceCalculator priceCalculator;
    
    SeatBookingService(LocalDate eventDate, List<Seat> seats, Map<SeatType, Money> pricing) {
        this.eventDate = eventDate;
        this.seatsById = buildSeatMap(seats);
        this.holdsById = new HashMap<>();
        this.bookingsById = new HashMap<>();
        this.waitlist = new LinkedList<>();
        this.priceCalculator = new PriceCalculator(pricing);
    }
    
    synchronized Hold createHold(String customerId, Map<SeatType, Integer> requirements) {
        processExpiredHolds();
        
        int totalRequested = requirements.values().stream().mapToInt(Integer::intValue).sum();
        List<Seat> available = findAvailableSeats(requirements);
        
        if (available.size() < totalRequested) {
            addToWaitlist(customerId, requirements);
            throw new InsufficientSeatsException("Added to waitlist");
        }
        
        String holdId = UUID.randomUUID().toString();
        Hold hold = new Hold(holdId, customerId, available);
        available.forEach(seat -> seat.hold(holdId));
        holdsById.put(holdId, hold);
        
        return hold;
    }
    
    synchronized Booking confirmHold(String holdId) {
        Hold hold = holdsById.get(holdId);
        if (hold == null) throw new InvalidHoldException("Hold not found");
        if (hold.isExpired()) {
            processExpiredHolds();
            throw new InvalidHoldException("Hold expired");
        }
        
        String bookingId = UUID.randomUUID().toString();
        Money totalPrice = priceCalculator.calculateTotal(hold.seats());
        Booking booking = new Booking(bookingId, hold.customerId(), eventDate,
                                       hold.seats(), totalPrice);
        
        hold.seats().forEach(seat -> seat.book(bookingId));
        bookingsById.put(bookingId, booking);
        holdsById.remove(holdId);
        
        return booking;
    }
    
    synchronized void releaseHold(String holdId) {
        Hold hold = holdsById.get(holdId);
        if (hold == null) return;
        
        hold.seats().forEach(Seat::release);
        holdsById.remove(holdId);
        processWaitlist();
    }
    
    synchronized Money cancelBooking(String bookingId) {
        Booking booking = bookingsById.get(bookingId);
        if (booking == null) throw new InvalidBookingException("Booking not found");
        
        Money refund = booking.calculateRefund();
        booking.seats().forEach(Seat::release);
        bookingsById.remove(bookingId);
        processWaitlist();
        
        return refund;
    }
    
    synchronized int availableSeatCount() {
        return (int) seatsById.values().stream()
            .filter(seat -> seat.status() == SeatStatus.AVAILABLE)
            .count();
    }
    
    synchronized int waitlistSize() {
        return waitlist.size();
    }
    
    private List<Seat> findAvailableSeats(Map<SeatType, Integer> requirements) {
        List<Seat> result = new ArrayList<>();
        for (Map.Entry<SeatType, Integer> entry : requirements.entrySet()) {
            seatsById.values().stream()
                .filter(s -> s.type() == entry.getKey() && s.status() == SeatStatus.AVAILABLE)
                .limit(entry.getValue())
                .forEach(result::add);
        }
        return result;
    }
    
    private void processExpiredHolds() {
        holdsById.entrySet().removeIf(entry -> {
            if (entry.getValue().isExpired()) {
                entry.getValue().seats().forEach(Seat::release);
                return true;
            }
            return false;
        });
    }
    
    private void processWaitlist() {
        Iterator<WaitlistEntry> iter = waitlist.iterator();
        while (iter.hasNext()) {
            WaitlistEntry entry = iter.next();
            List<Seat> available = findAvailableSeats(entry.requirements());
            
            if (available.size() >= entry.totalRequested()) {
                String holdId = UUID.randomUUID().toString();
                Hold hold = new Hold(holdId, entry.customerId(), available);
                available.forEach(seat -> seat.hold(holdId));
                holdsById.put(holdId, hold);
                iter.remove();
            } else {
                break;
            }
        }
    }
    
    private void addToWaitlist(String customerId, Map<SeatType, Integer> requirements) {
        waitlist.offer(new WaitlistEntry(UUID.randomUUID().toString(), customerId, requirements));
    }
    
    private Map<String, Seat> buildSeatMap(List<Seat> seats) {
        Map<String, Seat> map = new HashMap<>();
        for (Seat seat : seats) {
            String key = seat.location().section() + "-" + seat.location().row() + "-" + seat.location().seatNumber();
            map.put(key, seat);
        }
        return map;
    }
}
