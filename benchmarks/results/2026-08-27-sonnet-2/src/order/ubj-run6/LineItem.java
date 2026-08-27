package com.plg.orders;

import java.math.BigDecimal;

record LineItem(String description, BigDecimal unitPrice, int quantity) {
}
