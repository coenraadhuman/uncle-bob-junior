I'll build a complete event seat booking engine with all the specified features. Here are my assumptions:

**Assumptions:**
- Holds are stored in-memory with an automated expiry checker running every minute
- Ticket prices are: Adult $100, Child $50, Senior $75, Student $60
- Group discount applies at confirmation time based on total seat count
- Refund percentages are calculated based on days between now and event date (not booking date)
- Waiting list is FIFO; customers are notified when seats free up (placeholder in code)
- Thread-safe implementation using concurrent collections

```java
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

public class EventBookingEngine {
    
    enum TicketType {
        ADULT(100.0),
        CHILD(50.0),
        SENIOR(75.0),
        STUDENT(60.0);
        
        private final double basePrice;
        
        TicketType(double basePrice) {
            this.basePrice = basePrice;
        }
        
        public double getBasePrice() {
            return basePrice;
        }
    }
    
    static class Event {
        private final String eventId;
        private final LocalDateTime eventDate;
        private final int totalSeats;
        private int availableSeats;
        
        Event(String eventId, LocalDateTime eventDate, int totalSeats) {
            this.eventId = eventId;
            this.eventDate = eventDate;
            this.totalSeats = totalSeats;
            this.availableSeats = totalSeats;
        }
        
        synchronized void reserveSeats(int count) {
            if (availableSeats < count) {
                throw new IllegalStateException("Not enough seats available");
            }
            availableSeats -= count;
        }
        
        synchronized void releaseSeats(int count) {
            availableSeats = Math.min(availableSeats + count, totalSeats);
        }
        
        int getAvailableSeats() {
            return availableSeats;
        }
        
        LocalDateTime getEventDate() {
            return eventDate;
        }
    }
    
    static class SeatHold {
        private final String holdId;
        private final String customerId;
        private final int seatCount;
        private final LocalDateTime expiresAt;
        private boolean confirmed;
        
        SeatHold(String customerId, int seatCount) {
            this.holdId = UUID.randomUUID().toString();
            this.customerId = customerId;
            this.seatCount = seatCount;
            this.expiresAt = LocalDateTime.now().plusMinutes(15);
            this.confirmed = false;
        }
        
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiresAt);
        }
        
        void confirm() {
            this.confirmed = true;
        }
        
        String getHoldId() { return holdId; }
        String getCustomerId() { return customerId; }
        int getSeatCount() { return seatCount; }
        boolean isConfirmed() { return confirmed; }
    }
    
    static class BookingRecord {
        private final String bookingId;
        private final String customerId;
        private final int seatCount;
        private final double totalPrice;
        private final LocalDateTime bookingDate;
        private final LocalDateTime eventDate;
        
        BookingRecord(String customerId, int seatCount, double totalPrice, LocalDateTime eventDate) {
            this.bookingId = UUID.randomUUID().toString();
            this.customerId = customerId;
            this.seatCount = seatCount;
            this.totalPrice = totalPrice;
            this.bookingDate = LocalDateTime.now();
            this.eventDate = eventDate;
        }
        
        double calculateRefund() {
            long daysUntilEvent = ChronoUnit.DAYS.between(LocalDateTime.now(), eventDate);
            
            if (daysUntilEvent > 30) {
                return totalPrice;
            } else if (daysUntilEvent >= 7) {
                return totalPrice * 0.5;
            }
            return 0.0;
        }
        
        String getBookingId() { return bookingId; }
        int getSeatCount() { return seatCount; }
        double getTotalPrice() { return totalPrice; }
    }
    
    static class WaitingListEntry {
        private final String customerId;
        private final int seatCount;
        private final LocalDateTime requestTime;
        
        WaitingListEntry(String customerId, int seatCount) {
            this.customerId = customerId;
            this.seatCount = seatCount;
            this.requestTime = LocalDateTime.now();
        }
    }
    
    static class BookingEngine {
        private final Map<String, Event> events = new ConcurrentHashMap<>();
        private final Map<String, SeatHold> activeHolds = new ConcurrentHashMap<>();
        private final Map<String, String> holdToEventMap = new ConcurrentHashMap<>();
        private final Map<String, BookingRecord> bookings = new ConcurrentHashMap<>();
        private final Map<String, Queue<WaitingListEntry>> waitingLists = new ConcurrentHashMap<>();
        private final ScheduledExecutorService expiryChecker = Executors.newScheduledThreadPool(1);
        
        public BookingEngine() {
            startExpiryChecker();
        }
        
        public void createEvent(String eventId, LocalDateTime eventDate, int totalSeats) {
            events.put(eventId, new Event(eventId, eventDate, totalSeats));
            waitingLists.put(eventId, new ConcurrentLinkedQueue<>());
        }
        
        public SeatHold holdSeats(String eventId, String customerId, int seatCount) {
            if (seatCount <= 0) {
                throw new IllegalArgumentException("Must hold at least one seat");
            }
            
            Event event = events.get(eventId);
            if (event == null) {
                throw new IllegalArgumentException("Event not found");
            }
            
            synchronized (event) {
                if (event.getAvailableSeats() < seatCount) {
                    waitingLists.get(eventId).add(new WaitingListEntry(customerId, seatCount));
                    throw new IllegalStateException("No seats available. Added to waiting list.");
                }
                event.reserveSeats(seatCount);
            }
            
            SeatHold hold = new SeatHold(customerId, seatCount);
            activeHolds.put(hold.getHoldId(), hold);
            holdToEventMap.put(hold.getHoldId(), eventId);
            
            return hold;
        }
        
        public BookingRecord confirmHold(String eventId, String holdId, List<TicketType> ticketTypes) {
            SeatHold hold = activeHolds.get(holdId);
            if (hold == null) {
                throw new IllegalArgumentException("Hold not found");
            }
            
            if (hold.isExpired()) {
                releaseHold(holdId);
                throw new IllegalStateException("Hold has expired");
            }
            
            if (hold.isConfirmed()) {
                throw new IllegalStateException("Hold already confirmed");
            }
            
            if (ticketTypes.size() != hold.getSeatCount()) {
                throw new IllegalArgumentException("Ticket count does not match hold");
            }
            
            hold.confirm();
            
            double basePrice = ticketTypes.stream()
                .mapToDouble(TicketType::getBasePrice)
                .sum();
            
            double totalPrice = basePrice;
            if (hold.getSeatCount() >= 10) {
                totalPrice = basePrice * 0.95;
            }
            
            Event event = events.get(eventId);
            BookingRecord booking = new BookingRecord(hold.getCustomerId(), hold.getSeatCount(), 
                                                      totalPrice, event.getEventDate());
            bookings.put(booking.getBookingId(), booking);
            
            return booking;
        }
        
        public void releaseHold(String holdId) {
            SeatHold hold = activeHolds.remove(holdId);
            String eventId = holdToEventMap.remove(holdId);
            
            if (hold != null && !hold.isConfirmed()) {
                Event event = events.get(eventId);
                if (event != null) {
                    event.releaseSeats(hold.getSeatCount());
                    processWaitingList(eventId);
                }
            }
        }
        
        public double cancelBooking(String eventId, String bookingId) {
            BookingRecord booking = bookings.remove(bookingId);
            if (booking == null) {
                throw new IllegalArgumentException("Booking not found");
            }
            
            Event event = events.get(eventId);
            event.releaseSeats(booking.getSeatCount());
            processWaitingList(eventId);
            
            return booking.calculateRefund();
        }
        
        public List<WaitingListEntry> getWaitingList(String eventId) {
            Queue<WaitingListEntry> queue = waitingLists.get(eventId);
            return queue != null ? new ArrayList<>(queue) : Collections.emptyList();
        }
        
        public int getAvailableSeats(String eventId) {
            Event event = events.get(eventId);
            return event != null ? event.getAvailableSeats() : -1;
        }
        
        private void processWaitingList(String eventId) {
            Event event = events.get(eventId);
            Queue<WaitingListEntry> waitingList = waitingLists.get(eventId);
            
            if (event == null || waitingList == null) {
                return;
            }
            
            while (!waitingList.isEmpty()) {
                WaitingListEntry entry = waitingList.peek();
                
                synchronized (event) {
                    if (event.getAvailableSeats() >= entry.seatCount) {
                        event.reserveSeats(entry.seatCount);
                        waitingList.poll();
                        // In production: notifyCustomer(entry.customerId)
                    } else {
                        break;
                    }
                }
            }
        }
        
        private void startExpiryChecker() {
            expiryChecker.scheduleAtFixedRate(() -> {
                List<String> expiredHolds = activeHolds.entrySet().stream()
                    .filter(e -> e.getValue().isExpired() && !e.getValue().isConfirmed())
                    .map(Map.Entry::getKey)
                    .toList();
                
                expiredHolds.forEach(this::releaseHold);
            }, 1, 1, TimeUnit.MINUTES);
        }
        
        public void shutdown() {
            expiryChecker.shutdown();
            try {
                if (!expiryChecker.awaitTermination(5, TimeUnit.SECONDS)) {
                    expiryChecker.shutdownNow();
                }
            } catch (InterruptedException e) {
                expiryChecker.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public static void main(String[] args) {
        BookingEngine engine = new BookingEngine();
        engine.createEvent("evt-001", LocalDateTime.now().plusDays(60), 20);
        
        System.out.println("=== Regular Booking (4 seats) ===");
        SeatHold hold1 = engine.holdSeats("evt-001", "cust-001", 4);
        BookingRecord booking1 = engine.confirmHold("evt-001", hold1.getHoldId(), 
            Arrays.asList(TicketType.ADULT, TicketType.ADULT, TicketType.CHILD, TicketType.SENIOR));
        System.out.println("Booking: " + booking1.getBookingId());
        System.out.println("Price: $" + String.format("%.2f", booking1.getTotalPrice()));
        System.out.println("Available seats: " + engine.getAvailableSeats("evt-001"));
        
        System.out.println("\n=== Group Booking (10 seats, 5% discount) ===");
        SeatHold hold2 = engine.holdSeats("evt-001", "cust-002", 10);
        List<TicketType> groupTickets = new ArrayList<>();
        for (int i = 0; i < 10; i++) groupTickets.add(TicketType.ADULT);
        BookingRecord booking2 = engine.confirmHold("evt-001", hold2.getHoldId(), groupTickets);
        System.out.println("Booking: " + booking2.getBookingId());
        System.out.println("Price: $" + String.format("%.2f", booking2.getTotalPrice()) + " (base: $1000)");
        System.out.println("Available seats: " + engine.getAvailableSeats("evt-001"));
        
        System.out.println("\n=== Waiting List ===");
        try {
            engine.holdSeats("evt-001", "cust-003", 6);
        } catch (IllegalStateException e) {
            System.out.println("Status: " + e.getMessage());
            System.out.println("Waiting list size: " + engine.getWaitingList("evt-001").size());
        }
        
        System.out.println("\n=== Cancellation (>30 days, 100% refund) ===");
        double refund = engine.cancelBooking("evt-001", booking1.getBookingId());
        System.out.println("Refund: $" + String.format("%.2f", refund));
        System.out.println("Available seats: " + engine.getAvailableSeats("evt-001"));
        System.out.println("Waiting list served - Remaining: " + engine.getWaitingList("evt-001").size());
        
        engine.shutdown();
    }
}
```

**Key features implemented:**
- **Hold management**: 15-minute expiry with background checker; can confirm or release
- **Pricing tiers**: ADULT/CHILD/SENIOR/STUDENT with configurable base prices
- **Group discount**: Automatic 5% off for 10+ seats
- **Refund policy**: 100% (>30 days), 50% (7-30 days), 0% (<7 days)
- **Waiting list**: FIFO queue automatically served when seats free up
- **Thread-safe**: ConcurrentHashMap, synchronized Event methods