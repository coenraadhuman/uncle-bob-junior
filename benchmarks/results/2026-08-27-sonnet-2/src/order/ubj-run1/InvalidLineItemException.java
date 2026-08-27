// InvalidLineItemException.java
package com.example.order;

public class InvalidLineItemException extends RuntimeException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}
