// OrderItemValidator.java
import java.math.BigDecimal;
import java.util.List;

final class OrderItemValidator {

    private OrderItemValidator() {
    }

    static void validate(List<OrderItem> items) {
        if (items.isEmpty()) {
            throw new OrderValidationException("Order must contain at least one line item");
        }
        items.forEach(OrderItemValidator::validateItem);
    }

    private static void validateItem(OrderItem item) {
        if (item.description().isBlank()) {
            throw new OrderValidationException("Line item description must not be blank");
        }
        if (item.quantity() <= 0) {
            throw new OrderValidationException("Line item quantity must be positive: " + item.description());
        }
        if (item.unitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderValidationException("Line item unit price must not be negative: " + item.description());
        }
    }
}
