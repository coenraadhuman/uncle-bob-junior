package com.example.orders;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
