import java.time.*;
import java.util.*;

class AccessLogAnalyzerTest {
    public static void main(String[] args) {
        testParseValidLine();
        testParseInvalidLine();
        testFullAnalysis();
        System.out.println("\nAll tests passed!");
    }
    
    static void testParseValidLine() {
        String line = "192.168.1.1 - - [01/Aug/2023:10:15:30 +0000] \"GET /index.html\" 200 1234";
        var result = AccessLogAnalyzer.parseLine(line);
        assert result.isPresent() : "Should parse valid line";
        var entry = result.get();
        assert entry.ip.equals("192.168.1.1") : "IP mismatch";
        assert entry.status == 200 : "Status mismatch";
        assert entry.statusClass() == 200 : "Status class should be 200";
        assert entry.path.equals("/index.html") : "Path mismatch";
        System.out.println("✓ testParseValidLine passed");
    }
    
    static void testParseInvalidLine() {
        var result = AccessLogAnalyzer.parseLine("invalid line");
        assert result.isEmpty() : "Should reject invalid line";
        System.out.println("✓ testParseInvalidLine passed");
    }
    
    static void testFullAnalysis() {
        List<AccessLogAnalyzer.LogEntry> entries = List.of(
            new AccessLogAnalyzer.LogEntry(
                "192.168.1.1", LocalDateTime.of(2023, 8, 1, 10, 0, 0),
                "GET", "/index.html", 200, 1234
            ),
            new AccessLogAnalyzer.LogEntry(
                "192.168.1.1", LocalDateTime.of(2023, 8, 1, 10, 0, 1),
                "GET", "/index.html", 200, 1234
            ),
            new AccessLogAnalyzer.LogEntry(
                "192.168.1.1", LocalDateTime.of(2023, 8, 1, 10, 0, 2),
                "GET", "/api/users", 404, 0
            ),
            new AccessLogAnalyzer.LogEntry(
                "192.168.1.2", LocalDateTime.of(2023, 8, 1, 10, 0, 3),
                "POST", "/api/users", 500, 256
            )
        );
        
        var results = AccessLogAnalyzer.analyze(entries);
        assert results.statusClassCounts.get(200) == 2L : "Should have 2 2xx responses";
        assert results.statusClassCounts.get(400) == 1L : "Should have 1 4xx response";
        assert results.statusClassCounts.get(500) == 1L : "Should have 1 5xx response";
        assert results.topPaths.get(0).getKey().equals("/index.html") : "Top path mismatch";
        assert results.topPaths.get(0).getValue() == 2L : "Top path count mismatch";
        assert results.errorRatePerHour.get("2023-08-01 10") == 50.0 : "Error rate should be 50%";
        assert results.suspiciousIPs.isEmpty() : "No IP should exceed 100 requests";
        
        System.out.println("✓ testFullAnalysis passed");
        System.out.println("\nSample report output:");
        AccessLogAnalyzer.printReport(results);
    }
}
