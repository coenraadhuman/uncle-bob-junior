import java.time.format.*;
import java.util.*;
import java.util.stream.*;

class WebLogAnalyzer {
    private static final int SUSPICIOUS_THRESHOLD = 100;
    private static final int TOP_PATHS_COUNT = 5;
    
    private final List<LogEntry> entries = new ArrayList<>();
    private final LogLineParser parser = new LogLineParser();
    
    void addLogLine(String line) {
        parser.parseLine(line).ifPresent(entries::add);
    }
    
    void report() {
        System.out.println("=== WEB ACCESS LOG ANALYSIS ===\n");
        reportStatusCounts();
        reportTopPaths();
        reportHourlyErrorRate();
        reportSuspiciousIPs();
    }
    
    private void reportStatusCounts() {
        System.out.println("Request Counts by Status Class:");
        System.out.println("  2xx: " + countInRange(200, 299));
        System.out.println("  3xx: " + countInRange(300, 399));
        System.out.println("  4xx: " + countInRange(400, 499));
        System.out.println("  5xx: " + countInRange(500, 599));
        System.out();
    }
    
    private void reportTopPaths() {
        System.out.println("Top " + TOP_PATHS_COUNT + " Requested Paths:");
        entries.stream()
            .collect(Collectors.groupingBy(LogEntry::path, Collectors.counting()))
            .entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(TOP_PATHS_COUNT)
            .forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        System.out();
    }
    
    private void reportHourlyErrorRate() {
        System.out.println("Error Rate Per Hour (status >= 400):");
        entries.stream()
            .collect(Collectors.groupingBy(this::hourKey, Collectors.toList()))
            .entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> printHourStats(e.getKey(), e.getValue()));
        System.out();
    }
    
    private void printHourStats(String hour, List<LogEntry> hourEntries) {
        long errorCount = hourEntries.stream()
            .filter(e -> e.statusCode() >= 400)
            .count();
        double errorRate = denominator(hourEntries.size()) == 0 ? 0 
            : (double) errorCount / hourEntries.size() * 100;
        System.out.printf("  %s: %.1f%% (%d/%d errors)%n", hour, errorRate, errorCount, 
            hourEntries.size());
    }
    
    private void reportSuspiciousIPs() {
        System.out.println("Suspicious IPs (>" + SUSPICIOUS_THRESHOLD + " requests/hour):");
        var suspicious = entries.stream()
            .collect(Collectors.groupingBy(this::ipHourKey, Collectors.counting()))
            .entrySet().stream()
            .filter(e -> e.getValue() > SUSPICIOUS_THRESHOLD)
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .toList();
        
        if (suspicious.isEmpty()) {
            System.out.println("  None detected.");
        } else {
            suspicious.forEach(e -> System.out.println("  " + e.getKey() + ": " + e.getValue()));
        }
    }
    
    private long countInRange(int minStatus, int maxStatus) {
        return entries.stream()
            .filter(e -> e.statusCode() >= minStatus && e.statusCode() <= maxStatus)
            .count();
    }
    
    private String hourKey(LogEntry e) {
        return e.timestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00"));
    }
    
    private String ipHourKey(LogEntry e) {
        return e.ip() + " @ " + hourKey(e);
    }
    
    private long denominator(int value) { return value; }
    
    List<LogEntry> entries() { return Collections.unmodifiableList(entries); }
}
