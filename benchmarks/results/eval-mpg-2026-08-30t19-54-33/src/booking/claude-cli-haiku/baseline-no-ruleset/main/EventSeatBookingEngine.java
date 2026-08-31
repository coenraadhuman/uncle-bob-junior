import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class EventSeatBookingEngine {
    
    enum TicketType {
        ADULT(50.00), CHILD(25.00), SENIOR(35.00), STUDENT(30.00);
        private final double basePrice;
        TicketType(double basePrice) { this.basePrice = basePrice; }
        public double getBasePrice() { return basePrice; }
    }
    
    enum SeatStatus { AVAILABLE, HELD, BOOKED }
    
    static class Event {
        String eventId;
        String name;
        LocalDateTime eventDate;
        int totalSeats;
        
        Event(String eventId, String name, LocalDateTime eventDate, int totalSeats) {
            this.eventId = eventId;
            this.name = name;
            this.eventDate = eventDate;
            this.totalSeats = totalSeats;
        }
    }
    
    static class Seat {
        String seatId;
        SeatStatus status;
        String holdId;
        LocalDateTime holdTime;
        
        Seat(String seatId) {
            this.seatId = seatId;
            this.status = SeatStatus.AVAILABLE;
        }
    }
    
    static class SeatHold {
        String holdId;
        String customerId;
        String eventId;
        List<String> seatIds;
        List<TicketType> ticketTypes;
        LocalDateTime createdAt;
        
        SeatHold(String holdId, String customerId, String eventId, List<String> seatIds, List<TicketType> ticketTypes, LocalDateTime createdAt) {
            this.holdId = holdId;
            this.customerId = customerId;
            this.eventId = eventId;
            this.seatIds = new ArrayList<>(seatIds);
            this.ticketTypes = new ArrayList<>(ticketTypes);
            this.createdAt = createdAt;
        }
        
        boolean isExpired(LocalDateTime currentTime) {
            return currentTime.isAfter(createdAt.plusMinutes(15));
        }
    }
    
    static class Booking {
        String bookingId;
        String customerId;
        String eventId;
        List<String> seatIds;
        List<TicketType> ticketTypes;
        LocalDateTime bookingDate;
        double totalPrice;
        boolean cancelled;
        LocalDateTime cancellationDate;
        
        Booking(String bookingId, String customerId, String eventId, List<String> seatIds, 
                List<TicketType> ticketTypes, LocalDateTime bookingDate, double totalPrice) {
            this.bookingId = bookingId;
            this.customerId = customerId;
            this.eventId = eventId;
            this.seatIds = new ArrayList<>(seatIds);
            this.ticketTypes = new ArrayList<>(ticketTypes);
            this.bookingDate = bookingDate;
            this.totalPrice = totalPrice;
            this.cancelled = false;
        }
    }
    
    static class WaitingListEntry {
        String customerId;
        List<TicketType> ticketTypes;
        LocalDateTime requestTime;
        
        WaitingListEntry(String customerId, List<TicketType> ticketTypes, LocalDateTime requestTime) {
            this.customerId = customerId;
            this.ticketTypes = new ArrayList<>(ticketTypes);
            this.requestTime = requestTime;
        }
    }
    
    private Map<String, Event> events;
    private Map<String, Map<String, Seat>> eventSeats;
    private Map<String, SeatHold> seatHolds;
    private Map<String, Booking> bookings;
    private Map<String, Queue<WaitingListEntry>> waitingLists;
    private ScheduledExecutorService executor;
    
    public EventSeatBookingEngine() {
        this.events = new ConcurrentHashMap<>();
        this.eventSeats = new ConcurrentHashMap<>();
        this.seatHolds = new ConcurrentHashMap<>();
        this.bookings = new ConcurrentHashMap<>();
        this.waitingLists = new ConcurrentHashMap<>();
        this.executor = Executors.newScheduledThreadPool(1);
        startHoldExpirationChecker();
    }
    
    public void createEvent(String eventId, String name, LocalDateTime eventDate, int totalSeats) {
        events.put(eventId, new Event(eventId, name, eventDate, totalSeats));
        Map<String, Seat> seats = new ConcurrentHashMap<>();
        for (int i = 1; i <= totalSeats; i++) {
            seats.put("SEAT-" + i, new Seat("SEAT-" + i));
        }
        eventSeats.put(eventId, seats);
        waitingLists.put(eventId, new ConcurrentLinkedQueue<>());
    }
    
    public SeatHold holdSeats(String customerId, String eventId, List<TicketType> ticketTypes, LocalDateTime currentTime) {
        Map<String, Seat> seats = eventSeats.get(eventId);
        if (seats == null) throw new IllegalArgumentException("Event not found");
        
        synchronized (seats) {
            List<String> availableSeats = seats.values().stream()
                    .filter(s -> s.status == SeatStatus.AVAILABLE)
                    .map(s -> s.seatId)
                    .limit(ticketTypes.size())
                    .collect(Collectors.toList());
            
            if (availableSeats.size() < ticketTypes.size()) {
                return null;
            }
            
            String holdId = UUID.randomUUID().toString();
            SeatHold hold = new SeatHold(holdId, customerId, eventId, availableSeats, ticketTypes, currentTime);
            
            for (String seatId : availableSeats) {
                Seat seat = seats.get(seatId);
                seat.status = SeatStatus.HELD;
                seat.holdId = holdId;
                seat.holdTime = currentTime;
            }
            
            seatHolds.put(holdId, hold);
            return hold;
        }
    }
    
    public boolean confirmHold(String holdId, String customerId, LocalDateTime currentTime) {
        SeatHold hold = seatHolds.get(holdId);
        if (hold == null || !hold.customerId.equals(customerId)) return false;
        
        Map<String, Seat> seats = eventSeats.get(hold.eventId);
        synchronized (seats) {
            if (hold.isExpired(currentTime)) {
                releaseHold(holdId, currentTime);
                return false;
            }
            
            double price = calculatePrice(hold.ticketTypes, hold.seatIds.size());
            String bookingId = UUID.randomUUID().toString();
            Booking booking = new Booking(bookingId, customerId, hold.eventId, hold.seatIds, hold.ticketTypes, currentTime, price);
            
            for (String seatId : hold.seatIds) {
                Seat seat = seats.get(seatId);
                seat.status = SeatStatus.BOOKED;
            }
            
            bookings.put(bookingId, booking);
            seatHolds.remove(holdId);
            
            processWaitingList(hold.eventId, currentTime);
            return true;
        }
    }
    
    public boolean releaseHold(String holdId, LocalDateTime currentTime) {
        SeatHold hold = seatHolds.get(holdId);
        if (hold == null) return false;
        
        Map<String, Seat> seats = eventSeats.get(hold.eventId);
        synchronized (seats) {
            for (String seatId : hold.seatIds) {
                Seat seat = seats.get(seatId);
                seat.status = SeatStatus.AVAILABLE;
                seat.holdId = null;
                seat.holdTime = null;
            }
            seatHolds.remove(holdId);
            processWaitingList(hold.eventId, currentTime);
            return true;
        }
    }
    
    public double cancelBooking(String bookingId, LocalDateTime currentTime) {
        Booking booking = bookings.get(bookingId);
        if (booking == null || booking.cancelled) return 0;
        
        Map<String, Seat> seats = eventSeats.get(booking.eventId);
        synchronized (seats) {
            Event event = events.get(booking.eventId);
            long daysBefore = ChronoUnit.DAYS.between(currentTime, event.eventDate);
            
            double refund = 0;
            if (daysBefore > 30) {
                refund = booking.totalPrice;
            } else if (daysBefore >= 7) {
                refund = booking.totalPrice * 0.5;
            }
            
            booking.cancelled = true;
            booking.cancellationDate = currentTime;
            
            for (String seatId : booking.seatIds) {
                Seat seat = seats.get(seatId);
                seat.status = SeatStatus.AVAILABLE;
            }
            
            processWaitingList(booking.eventId, currentTime);
            return refund;
        }
    }
    
    public boolean joinWaitingList(String customerId, String eventId, List<TicketType> ticketTypes, LocalDateTime currentTime) {
        Queue<WaitingListEntry> waitingList = waitingLists.get(eventId);
        if (waitingList == null) return false;
        WaitingListEntry entry = new WaitingListEntry(customerId, ticketTypes, currentTime);
        waitingList.offer(entry);
        return true;
    }
    
    private void processWaitingList(String eventId, LocalDateTime currentTime) {
        Map<String, Seat> seats = eventSeats.get(eventId);
        Queue<WaitingListEntry> waitingList = waitingLists.get(eventId);
        
        while (!waitingList.isEmpty()) {
            List<String> availableSeats = seats.values().stream()
                    .filter(s -> s.status == SeatStatus.AVAILABLE)
                    .map(s -> s.seatId)
                    .collect(Collectors.toList());
            
            if (availableSeats.isEmpty()) break;
            
            WaitingListEntry entry = waitingList.peek();
            if (availableSeats.size() >= entry.ticketTypes.size()) {
                waitingList.poll();
                SeatHold hold = holdSeats(entry.customerId, eventId, entry.ticketTypes, currentTime);
                if (hold != null) {
                    System.out.println("Waiting list customer " + entry.customerId + " offered hold: " + hold.holdId);
                }
            } else {
                break;
            }
        }
    }
    
    private double calculatePrice(List<TicketType> ticketTypes, int seatCount) {
        double basePrice = ticketTypes.stream().mapToDouble(TicketType::getBasePrice).sum();
        if (seatCount >= 10) basePrice *= 0.95;
        return basePrice;
    }
    
    private void startHoldExpirationChecker() {
        executor.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            seatHolds.values().stream()
                    .filter(h -> h.isExpired(now))
                    .map(h -> h.holdId)
                    .collect(Collectors.toList())
                    .forEach(holdId -> releaseHold(holdId, LocalDateTime.now()));
        }, 1, 1, TimeUnit.MINUTES);
    }
    
    public int getAvailableSeats(String eventId) {
        return (int) eventSeats.get(eventId).values().stream()
                .filter(s -> s.status == SeatStatus.AVAILABLE).count();
    }
    
    public int getWaitingListSize(String eventId) {
        Queue<WaitingListEntry> waitingList = waitingLists.get(eventId);
        return waitingList != null ? waitingList.size() : 0;
    }
    
    public static void main(String[] args) {
        EventSeatBookingEngine engine = new EventSeatBookingEngine();
        LocalDateTime eventDate = LocalDateTime.now().plusDays(60);
        engine.createEvent("EVT-001", "Concert", eventDate, 25);
        LocalDateTime now = LocalDateTime.now();
        
        System.out.println("--- Test 1: Regular booking ---");
        SeatHold hold1 = engine.holdSeats("CUST-001", "EVT-001", 
                Arrays.asList(TicketType.ADULT, TicketType.CHILD), now);
        System.out.println("Hold created: " + (hold1 != null ? hold1.holdId : "Failed"));
        if (hold1 != null) {
            engine.confirmHold(hold1.holdId, "CUST-001", now);
            System.out.println("Booking confirmed. Available: " + engine.getAvailableSeats("EVT-001"));
        }
        
        System.out.println("\n--- Test 2: Group booking (10+ seats, 5% discount) ---");
        List<TicketType> groupTickets = Arrays.asList(TicketType.ADULT, TicketType.ADULT, TicketType.STUDENT,
                TicketType.CHILD, TicketType.CHILD, TicketType.CHILD, TicketType.CHILD, TicketType.CHILD,
                TicketType.SENIOR, TicketType.SENIOR);
        SeatHold hold2 = engine.holdSeats("CUST-002", "EVT-001", groupTickets, now);
        if (hold2 != null) {
            engine.confirmHold(hold2.holdId, "CUST-002", now);
            System.out.println("Group booking confirmed. Available: " + engine.getAvailableSeats("EVT-001"));
        }
        
        System.out.println("\n--- Test 3: Cancellation with refund (>30 days before) ---");
        SeatHold hold3 = engine.holdSeats("CUST-003", "EVT-001", Arrays.asList(TicketType.ADULT), now);
        if (hold3 != null) {
            engine.confirmHold(hold3.holdId, "CUST-003", now);
            String bookingId = engine.bookings.entrySet().stream()
                    .filter(e -> e.getValue().customerId.equals("CUST-003"))
                    .map(e -> e.getKey()).findFirst().orElse(null);
            if (bookingId != null) {
                double refund = engine.cancelBooking(bookingId, now);
                System.out.println("100% refund issued: EUR " + String.format("%.2f", refund));
                System.out.println("Available after cancellation: " + engine.getAvailableSeats("EVT-001"));
            }
        }
        
        System.out.println("\n--- Test 4: Waiting list ---");
        engine.joinWaitingList("CUST-004", "EVT-001", Arrays.asList(TicketType.SENIOR, TicketType.SENIOR), now);
        System.out.println("Customer added to waiting list. Size: " + engine.getWaitingListSize("EVT-001"));
        
        System.out.println("\n--- Test 5: Hold expiration (15 minutes) ---");
        SeatHold hold5 = engine.holdSeats("CUST-005", "EVT-001", Arrays.asList(TicketType.ADULT), now);
        System.out.println("Hold created: " + (hold5 != null ? hold5.holdId : "Failed"));
        System.out.println("(Expiration check runs every 1 minute in background)");
    }
}
