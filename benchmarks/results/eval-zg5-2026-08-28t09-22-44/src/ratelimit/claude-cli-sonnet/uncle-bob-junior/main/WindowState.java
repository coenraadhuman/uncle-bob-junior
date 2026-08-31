// WindowState.java
package ratelimit;

/** Immutable snapshot of a client's request count within the current fixed window. */
record WindowState(long windowStartSeconds, int requestCount) {
}
