import java.io.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

class Categorizer {
    private static final Map<String, List<String>> CATEGORY_KEYWORDS = Map.ofEntries(
        Map.entry("salary", List.of("salary", "wage", "payroll", "payment received")),
        Map.entry("rent", List.of("rent", "lease", "mortgage", "landlord")),
        Map.entry("groceries", List.of("albert heijn", "supermarket", "grocery", "food", "supermarkt"))
    );
    
    static String categorize(String description) {
        String lower = description.toLowerCase();
        for (var entry : CATEGORY_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (lower.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }
        return "other";
    }
}
