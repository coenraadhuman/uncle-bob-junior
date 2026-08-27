package com.example.orders;

public class InvalidLineItemException extends IllegalArgumentException {
    public InvalidLineItemException(String message) {
        super(message);
    }
}
