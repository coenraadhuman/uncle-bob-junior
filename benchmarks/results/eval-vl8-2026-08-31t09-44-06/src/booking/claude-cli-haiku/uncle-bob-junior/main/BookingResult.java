record BookingResult(boolean success, String bookingId, Money totalPrice, String error) {
    static BookingResult confirmed(String bookingId, Money totalPrice) {
        return new BookingResult(true, bookingId, totalPrice, "");
    }

    static BookingResult error(String error) {
        return new BookingResult(false, "", new Money(0), error);
    }
}
