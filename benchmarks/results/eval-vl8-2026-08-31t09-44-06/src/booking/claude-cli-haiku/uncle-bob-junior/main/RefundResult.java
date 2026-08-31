record RefundResult(boolean success, Money amount, double refundPercent, String error) {
    static RefundResult refunded(Money amount, double percent) {
        return new RefundResult(true, amount, percent, "");
    }

    static RefundResult error(String error) {
        return new RefundResult(false, new Money(0), 0, error);
    }
}
