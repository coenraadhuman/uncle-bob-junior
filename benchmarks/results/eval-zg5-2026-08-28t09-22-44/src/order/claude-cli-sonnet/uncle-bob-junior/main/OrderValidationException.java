// File: OrderValidationException.java
package com.postcodeloterij.orders;

public final class OrderValidationException extends RuntimeException {

    public OrderValidationException(String message) {
        super(message);
    }
}
