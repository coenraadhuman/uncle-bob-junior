package com.plg.orders;

import java.util.List;

sealed interface OrderResult<T> permits OrderResult.Success, OrderResult.Failure {

    record Success<T>(T value) implements OrderResult<T> {
    }

    record Failure<T>(List<String> errors) implements OrderResult<T> {
    }
}
