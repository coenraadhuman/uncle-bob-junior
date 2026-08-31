import java.util.*;

class TransactionCategorizer {
    private static final String[] SALARY_KEYWORDS = {"SALARY", "WAGE", "PAYMENT"};
    private static final String[] RENT_KEYWORDS = {"RENT", "APARTMENT", "LANDLORD"};
    private static final String[] GROCERY_KEYWORDS = {"ALBERT HEIJN", "SUPERMARKET",
                                                       "GROCERY", "FOOD", "MARKET"};

    TransactionCategory categorize(String description) {
        String upper = description.toUpperCase();

        if (matchesAny(upper, SALARY_KEYWORDS)) return TransactionCategory.SALARY;
        if (matchesAny(upper, RENT_KEYWORDS)) return TransactionCategory.RENT;
        if (matchesAny(upper, GROCERY_KEYWORDS)) return TransactionCategory.GROCERIES;
        return TransactionCategory.OTHER;
    }

    private boolean matchesAny(String text, String[] keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
}
