class RefundPolicy {
    private static final long THIRTY_DAYS = 30L * 24 * 60 * 60 * 1000;
    private static final long SEVEN_DAYS = 7L * 24 * 60 * 60 * 1000;

    double refundPercent(long eventTimeMillis, long nowMillis) {
        long msUntilEvent = eventTimeMillis - nowMillis;

        if (msUntilEvent > THIRTY_DAYS) {
            return 100.0;
        }
        if (msUntilEvent > SEVEN_DAYS) {
            return 50.0;
        }
        return 0.0;
    }
}
