package com.example.orders;

import java.math.BigDecimal;
import java.util.List;

/**
 * Computed totals for a processed order, in euros, rounded to 2 decimals.
 */
public record Receipt(
        List<LineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {
}
