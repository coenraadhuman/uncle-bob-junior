public class RetryBuilder {
    private int maxAttempts = 3;
    private long delayMillis = 1000;

    public RetryBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public RetryBuilder delayMillis(long delayMillis) {
        this.delayMillis = delayMillis;
        return this;
    }

    public RetryHelper build() {
        return new RetryHelper(maxAttempts, delayMillis);
    }
}
