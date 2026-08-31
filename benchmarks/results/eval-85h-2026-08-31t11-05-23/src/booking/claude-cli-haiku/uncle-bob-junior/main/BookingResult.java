public class BookingResult {
    private final String bookingId;
    private final int priceInCents;
    private final String message;
    private final boolean success;
    
    private BookingResult(String bookingId, int priceInCents, String message, boolean success) {
        this.bookingId = bookingId;
        this.priceInCents = priceInCents;
        this.message = message;
        this.success = success;
    }
    
    public static BookingResult confirmed(String bookingId, int priceInCents) {
        return new BookingResult(bookingId, priceInCents, "Booking held", true);
    }
    
    public static BookingResult waitlisted(String entryId) {
        return new BookingResult(entryId, 0, "Added to waiting list", false);
    }
    
    public static BookingResult failed(String message) {
        return new BookingResult(null, 0, message, false);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public int getPriceInCents() {
        return priceInCents;
    }
    
    public String getMessage() {
        return message;
    }
}
