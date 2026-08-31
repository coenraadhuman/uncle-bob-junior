import java.io.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

public class WebAccessLogAnalyzer {
    
    static class LogEntry {
        final String ip;
        final LocalDateTime timestamp;
        final String method;
        final String path;
        final int statusCode;
        final long responseBytes;

        LogEntry(String ip, LocalDateTime timestamp, String method, String path, 
                 int statusCode, long responseBytes) {
            this.ip = ip;
            this.timestamp = timestamp;
            this.method = method;
            this.path = path;
            this.statusCode = statusCode;
            this.responseBytes = responseBytes;
        }

        int statusClass() {
            return statusCode / 100;
        }

        String ipHourKey() {
            return ip + "|" + toHourBucket();
        }

        String hourBucket() {
            return toHourBucket();
        }

        private String toHourBucket() {
            return timestamp.toLocalDate() + " " + String.format("%02d:00", timestamp.getHour());
        }
    }

    static class AccessLogParser {
        private static final Pattern LOG_PATTERN = Pattern.compile(
            "^([\\d.]+) - - \\[([^\\]]+)\\] \"([A-Z]+) ([^ \"]+)[^\"]*\" (\\d+) (\\d+)$"
        );
        private static final DateTimeFormatter TIMESTAMP_FORMAT = 
            DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);

        LogEntry parse(String line) {
            Matcher m = LOG_PATTERN.matcher(line);
            if (!m.matches()) return null;

            String ip = m.group(1);
            LocalDateTime timestamp = LocalDateTime.parse(m.group(2), TIMESTAMP_FORMAT);
            String method = m.group(3);
            String path = m.group(4);
            int statusCode = Integer.parseInt(m.group(5));
            long bytes = Long.parseLong(m.group(6));

            return new LogEntry(ip, timestamp, method, path, statusCode, bytes);
        }
    }

    static class AccessLogStatistics {
        private final List<LogEntry> entries;
        private final Map<Integer, Integer> statusClassCounts;
        private final Map<String, Integer> pathCounts;
        private final Map<String, Integer> ipHourCounts;

        AccessLogStatistics(List<LogEntry> entries) {
            this.entries = entries;
            this.statusClassCounts = new HashMap<>();
            this.pathCounts = new HashMap<>();
            this.ipHourCounts = new HashMap<>();
            aggregate();
        }

        private void aggregate() {
            for (LogEntry entry : entries) {
                statusClassCounts.merge(entry.statusClass(), 1, Integer::sum);
                pathCounts.merge(entry.path, 1, Integer::sum);
                ipHourCounts.merge(entry.ipHourKey(), 1, Integer::sum);
            }
        }

        int getStatusClassCount(int statusClass) {
            return statusClassCounts.getOrDefault(statusClass, 0);
        }

        List<String> getTopFiveRequestedPaths() {
            return pathCounts.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }

        Map<String, Double> getErrorRatePerHour() {
            Map<String, HourStatistics> hourStats = new HashMap<>();
            for (LogEntry entry : entries) {
                hourStats.computeIfAbsent(entry.hourBucket(), k -> new HourStatistics())
                    .recordRequest(entry.statusClass());
            }
            return hourStats.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getErrorRate()
                ));
        }

        List<String> getSuspiciousIpAddresses() {
            return ipHourCounts.entrySet().stream()
                .filter(e -> e.getValue() > 100)
                .map(e -> e.getKey().split("\\|")[0])
                .distinct()
                .collect(Collectors.toList());
        }
    }

    static class HourStatistics {
        private int totalRequests = 0;
        private int errorRequests = 0;

        void recordRequest(int statusClass) {
            totalRequests++;
            if (statusClass >= 4) {
                errorRequests++;
            }
        }

        double getErrorRate() {
            return totalRequests > 0 ? (double) errorRequests / totalRequests : 0;
        }
    }

    static class AccessLogReport {
        private final AccessLogStatistics stats;

        AccessLogReport(AccessLogStatistics stats) {
            this.stats = stats;
        }

        void print() {
            System.out.println("=== Web Access Log Analysis Report ===\n");
            printStatusCounts();
            printTopPaths();
            printErrorRates();
            printSuspiciousIps();
        }

        private void printStatusCounts() {
            System.out.println("Request Counts by Status Class:");
            System.out.println("  2xx: " + stats.getStatusClassCount(2));
            System.out.println("  3xx: " + stats.getStatusClassCount(3));
            System.out.println("  4xx: " + stats.getStatusClassCount(4));
            System.out.println("  5xx: " + stats.getStatusClassCount(5));
        }

        private void printTopPaths() {
            System.out.println("\nTop Five Requested Paths:");
            List<String> topPaths = stats.getTopFiveRequestedPaths();
            if (topPaths.isEmpty()) {
                System.out.println("  (none)");
            } else {
                topPaths.forEach(p -> System.out.println("  " + p));
            }
        }

        private void printErrorRates() {
            System.out.println("\nError Rate per Hour:");
            stats.getErrorRatePerHour().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> System.out.printf("  %s: %.2f%%%n", e.getKey(), e.getValue() * 100));
        }

        private void printSuspiciousIps() {
            System.out.println("\nSuspicious IPs (>100 requests/hour):");
            List<String> suspicious = stats.getSuspiciousIpAddresses();
            if (suspicious.isEmpty()) {
                System.out.println("  None detected");
            } else {
                suspicious.forEach(ip -> System.out.println("  " + ip));
            }
        }
    }

    static List<LogEntry> readLogFile(String filename) throws IOException {
        List<LogEntry> entries = new ArrayList<>();
        AccessLogParser parser = new AccessLogParser();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LogEntry entry = parser.parse(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java WebAccessLogAnalyzer <logfile>");
            System.exit(1);
        }
        List<LogEntry> entries = readLogFile(args[0]);
        AccessLogStatistics stats = new AccessLogStatistics(entries);
        new AccessLogReport(stats).print();
    }
}
