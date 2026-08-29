// LineItemValidator.java
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class LineItemValidator {

    public List<LineItem> validate(List<RawLineItem> rawItems) {
        if (rawItems.isEmpty()) {
            throw new OrderValidationException(List.of("Order must contain at least one line item"));
        }
        List<String> errors = new ArrayList<>();
        List<LineItem> validated = new ArrayList<>();
        for (int index = 0; index < rawItems.size(); index++) {
            validateItem(rawItems.get(index), index, errors, validated);
        }
        if (!errors.isEmpty()) {
            throw new OrderValidationException(errors);
        }
        return List.copyOf(validated);
    }

    private void validateItem(RawLineItem raw, int index, List<String> errors, List<LineItem> validated) {
        List<String> itemErrors = describeErrors(raw, index);
        if (!itemErrors.isEmpty()) {
            errors.addAll(itemErrors);
            return;
        }
        validated.add(toLineItem(raw));
    }

    private List<String> describeErrors(RawLineItem raw, int index) {
        List<String> errors = new ArrayList<>();
        if (raw.description() == null || raw.description().isBlank()) {
            errors.add("Line item " + (index + 1) + ": description must not be blank");
        }
        if (raw.unitPrice() == null || raw.unitPrice().signum() <= 0) {
            errors.add("Line item " + (index + 1) + ": unit price must be positive");
        }
        if (raw.quantity() <= 0) {
            errors.add("Line item " + (index + 1) + ": quantity must be positive");
        }
        return errors;
    }

    private LineItem toLineItem(RawLineItem raw) {
        BigDecimal lineTotal = raw.unitPrice()
                .multiply(BigDecimal.valueOf(raw.quantity()))
                .setScale(MoneyFormat.SCALE, MoneyFormat.ROUNDING);
        return new LineItem(raw.description(), raw.unitPrice(), raw.quantity(), lineTotal);
    }
}
