public class RefundInfo {
    private final double refundAmount;
    private final String reason;

    public RefundInfo(double refundAmount, String reason) {
        this.refundAmount = refundAmount;
        this.reason = reason;
    }

    public double refundAmount() { return refundAmount; }
    public String reason() { return reason; }
}
