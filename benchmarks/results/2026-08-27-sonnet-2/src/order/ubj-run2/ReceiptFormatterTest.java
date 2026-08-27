package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ReceiptFormatterTest {

    private final ReceiptFormatter formatter = new ReceiptFormatter();

    @Test
    void includesSubtotalVatAndTotal() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.50"),
                new BigDecimal("60.50"));

        String text = formatter.format(receipt);

        assertTrue(text.contains("Subtotal:"));
        assertTrue(text.contains("VAT (21%):"));
        assertTrue(text.contains("Total:"));
        assertTrue(text.contains("\u20AC60.50"));
    }

    @Test
    void omitsDiscountLineWhenNoDiscountApplied() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("50.00"), 1)),
                new BigDecimal("50.00"),
                new BigDecimal("0.00"),
                new BigDecimal("10.50"),
                new BigDecimal("60.50"));

        assertFalse(formatter.format(receipt).contains("Discount"));
    }

    @Test
    void includesDiscountLineWhenDiscountApplied() {
        Receipt receipt = new Receipt(
                List.of(new LineItem("Widget", new BigDecimal("200.00"), 1)),
                new BigDecimal("200.00"),
                new BigDecimal("20.00"),
                new BigDecimal("37.80"),
                new BigDecimal("217.80"));

        assertTrue(formatter.format(receipt).contains("Discount (10%):"));
    }
}
