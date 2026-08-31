class BookingNotFoundException extends RuntimeException {
  BookingNotFoundException(String bookingId) { super("Booking not found: " + bookingId); }
}
