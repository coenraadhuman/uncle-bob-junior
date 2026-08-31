record SeatHold(
    String holdId,
    String seatId,
    long expiryTimeMillis
) {
    boolean isExpired(long nowMillis) {
        return nowMillis > expiryTimeMillis;
    }
}
