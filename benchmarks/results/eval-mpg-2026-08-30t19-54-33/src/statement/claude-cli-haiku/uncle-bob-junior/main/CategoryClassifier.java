class CategoryClassifier {
    static String categorize(String description) {
        String lower = description.toLowerCase();
        if (containsAny(lower, "salary", "wage", "payment", "income")) return "salary";
        if (containsAny(lower, "rent", "landlord", "apartment", "housing")) return "rent";
        if (containsAny(lower, "albert heijn", "supermarket", "grocery", "food", "bakery", "market")) return "groceries";
        return "other";
    }

    private static boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) return true;
        }
        return false;
    }
}
