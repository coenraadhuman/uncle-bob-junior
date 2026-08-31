record SeatHoldResult(String holdId, List<String> seatIds, long expiryTimeMillis, boolean onWaitingList) {
    static SeatHoldResult held(String holdId, List<String> seatIds, long expiryTimeMillis) {
        return new SeatHoldResult(holdId, seatIds, expiryTimeMillis, false);
    }

    static SeatHoldResult waitingList() {
        return new SeatHoldResult("", List.of(), 0, true);
    }
}
