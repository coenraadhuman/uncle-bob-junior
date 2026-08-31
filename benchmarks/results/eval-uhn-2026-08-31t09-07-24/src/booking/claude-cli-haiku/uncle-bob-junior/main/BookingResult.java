sealed interface BookingResult {
    record Success(String id, double totalPrice) implements BookingResult {}
    record Failure(String reason) implements BookingResult {}
}
