// Receipt.java
package com.plg.order;

import java.math.BigDecimal;
import java.util.List;

public record Receipt(
        List<OrderLineItem> lineItems,
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal vat,
        BigDecimal total) {

    public Receipt {
        lineItems = List.copyOf(lineItems);
    }

    public boolean hasDiscount() {
        return discount.signum() > 0;
    }
}
