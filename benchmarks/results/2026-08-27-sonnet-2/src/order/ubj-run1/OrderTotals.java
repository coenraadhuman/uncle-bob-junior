// OrderTotals.java
package com.example.order;

import java.math.BigDecimal;

public record OrderTotals(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
}
