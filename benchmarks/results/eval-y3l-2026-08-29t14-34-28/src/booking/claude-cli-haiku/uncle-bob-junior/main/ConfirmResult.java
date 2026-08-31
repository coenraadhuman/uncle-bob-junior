class ConfirmResult {
    private final boolean success;
    private final String bookingId;
    private final Money totalPrice;
    private final String reason;

    private ConfirmResult(boolean success, String bookingId, Money totalPrice, String reason) {
        this.success = success;
        this.bookingId = bookingId;
        this.totalPrice = totalPrice;
        this.reason = reason;
    }

    boolean isSuccess() {
        return success;
    }

    String bookingId() {
        return bookingId;
    }

    Money totalPrice() {
        return totalPrice;
    }

    String reason() {
        return reason;
    }

    static ConfirmResult confirmed(String bookingId, Money price) {
        return new ConfirmResult(true, bookingId, price, null);
    }

    static ConfirmResult notFound() {
        return new ConfirmResult(false, null, null, "Hold not found or expired");
    }
}
