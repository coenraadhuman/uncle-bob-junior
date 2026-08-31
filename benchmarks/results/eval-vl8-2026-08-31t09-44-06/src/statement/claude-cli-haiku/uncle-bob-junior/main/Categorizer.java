class Categorizer {
    Category categorise(String description) {
        String lower = description.toLowerCase();
        if (hasSalaryKeyword(lower)) return Category.SALARY;
        if (hasRentKeyword(lower)) return Category.RENT;
        if (hasGroceryKeyword(lower)) return Category.GROCERIES;
        return Category.OTHER;
    }
    
    private boolean hasSalaryKeyword(String lower) {
        return lower.contains("salary") || lower.contains("wage") || lower.contains("income");
    }
    
    private boolean hasRentKeyword(String lower) {
        return lower.contains("rent") || lower.contains("lease");
    }
    
    private boolean hasGroceryKeyword(String lower) {
        return lower.contains("albert heijn") || lower.contains("supermarket") || 
               lower.contains("grocery") || lower.contains("food") || lower.contains("market");
    }
}
