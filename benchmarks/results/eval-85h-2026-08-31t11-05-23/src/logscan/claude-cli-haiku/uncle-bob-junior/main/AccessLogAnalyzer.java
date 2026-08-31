import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class AccessLogAnalyzer {
    private static final Pattern LOG_PATTERN = Pattern.compile(
        "^(\\S+) - - \\[(.*?)\\] \"(\\S+) (\\S+)\" (\\d+) (\\d+)$"
    );
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
    private static final int SUSPICIOUS_REQUEST_THRESHOLD = 100;
    private static final String HOUR_FORMAT = "yyyy-MM-dd HH";
    
    public static class LogEntry {
        public final String ip;
        public final LocalDateTime timestamp;
        public final String method;
        public final String path;
        public final int status;
        public final long bytes;
        
        public LogEntry(String ip, LocalDateTime timestamp, String method, 
                       String path, int status, long bytes) {
            this.ip = ip;
            this.timestamp = timestamp;
            this.method = method;
            this.path = path;
            this.status = status;
            this.bytes = bytes;
        }
        
        public int statusClass() {
            return (status / 100) * 100;
        }
        
        public String hourKey() {
            return timestamp.format(DateTimeFormatter.ofPattern(HOUR_FORMAT));
        }
        
        public String ipHourKey() {
            return ip + ":" + hourKey();
        }
    }
    
    public static class AnalysisResults {
        public final Map<Integer, Long> statusClassCounts;
        public final List<Map.Entry<String, Long>> topPaths;
        public final Map<String, Double> errorRatePerHour;
        public final Set<String> suspiciousIPs;
        
        public AnalysisResults(Map<Integer, Long> statusClassCounts,
                               List<Map.Entry<String, Long>> topPaths,
                               Map<String, Double> errorRatePerHour,
                               Set<String> suspiciousIPs) {
            this.statusClassCounts = statusClassCounts;
            this.topPaths = topPaths;
            this.errorRatePerHour = errorRatePerHour;
            this.suspiciousIPs = suspiciousIPs;
        }
    }
    
    public static Optional<LogEntry> parseLine(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) return Optional.empty();
        
        try {
            String ip = matcher.group(1);
            LocalDateTime timestamp = LocalDateTime.parse(
                matcher.group(2), TIMESTAMP_FORMAT
            );
            String method = matcher.group(3);
            String path = matcher.group(4);
            int status = Integer.parseInt(matcher.group(5));
            long bytes = Long.parseLong(matcher.group(6));
            
            return Optional.of(new LogEntry(ip, timestamp, method, path, status, bytes));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    public static AnalysisResults analyze(List<LogEntry> entries) {
        Map<Integer, Long> statusCounts = countByStatusClass(entries);
        List<Map.Entry<String, Long>> topPaths = findTopPaths(entries, 5);
        Map<String, Double> errorRates = computeErrorRatesPerHour(entries);
        Set<String> suspiciousIPs = findSuspiciousIPs(entries);
        
        return new AnalysisResults(statusCounts, topPaths, errorRates, suspiciousIPs);
    }
    
    private static Map<Integer, Long> countByStatusClass(List<LogEntry> entries) {
        return entries.stream()
            .collect(Collectors.groupingBy(
                LogEntry::statusClass,
                Collectors.counting()
            ));
    }
    
    private static List<Map.Entry<String, Long>> findTopPaths(List<LogEntry> entries, int limit) {
        return entries.stream()
            .collect(Collectors.groupingBy(e -> e.path, Collectors.counting()))
            .entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    private static Map<String, Double> computeErrorRatesPerHour(List<LogEntry> entries) {
        Map<String, List<LogEntry>> entriesByHour = entries.stream()
            .collect(Collectors.groupingBy(LogEntry::hourKey));
        
        return entriesByHour.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> calculateErrorRate(e.getValue())
            ));
    }
    
    private static double calculateErrorRate(List<LogEntry> entries) {
        if (entries.isEmpty()) return 0.0;
        long errors = entries.stream()
            .filter(e -> e.statusClass() >= 400)
            .count();
        return (errors * 100.0) / entries.size();
    }
    
    private static Set<String> findSuspiciousIPs(List<LogEntry> entries) {
        Map<String, Long> requestsPerIPHour = entries.stream()
            .collect(Collectors.groupingBy(
                LogEntry::ipHourKey,
                Collectors.counting()
            ));
        
        return requestsPerIPHour.entrySet().stream()
            .filter(e -> e.getValue() > SUSPICIOUS_REQUEST_THRESHOLD)
            .map(e -> e.getKey().split(":")[0])
            .collect(Collectors.toSet());
    }
    
    public static void printReport(AnalysisResults results) {
        System.out.println("=== Access Log Analysis Report ===\n");
        printStatusDistribution(results.statusClassCounts);
        printTopPaths(results.topPaths);
        printErrorRates(results.errorRatePerHour);
        printSuspiciousIPs(results.suspiciousIPs);
    }
    
    private static void printStatusDistribution(Map<Integer, Long> counts) {
        System.out.println("Status Code Distribution:");
        counts.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %dxx: %d%n", e.getKey() / 100, e.getValue()));
    }
    
    private static void printTopPaths(List<Map.Entry<String, Long>> paths) {
        System.out.println("\nTop 5 Requested Paths:");
        if (paths.isEmpty()) {
            System.out.println("  (none)");
        } else {
            paths.forEach(e -> System.out.printf("  %s: %d requests%n", e.getKey(), e.getValue()));
        }
    }
    
    private static void printErrorRates(Map<String, Double> errorRates) {
        System.out.println("\nError Rate Per Hour:");
        errorRates.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(e -> System.out.printf("  %s: %.2f%%%n", e.getKey(), e.getValue()));
    }
    
    private static void printSuspiciousIPs(Set<String> ips) {
        System.out.println("\nSuspicious IPs (>100 requests/hour):");
        if (ips.isEmpty()) {
            System.out.println("  (none)");
        } else {
            ips.stream().sorted().forEach(ip -> System.out.printf("  %s%n", ip));
        }
    }
}
