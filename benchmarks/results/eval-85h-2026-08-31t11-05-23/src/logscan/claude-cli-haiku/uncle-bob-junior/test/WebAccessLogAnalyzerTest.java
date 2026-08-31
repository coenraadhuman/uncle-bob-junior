import java.time.*;
import java.util.*;

class WebAccessLogAnalyzerTest {
    static void testParseValidLogLine() {
        String logLine = "192.168.1.1 - - [31/Aug/2026:14:23:45 +0000] \"GET /index.html\" 200 1024";
        AccessLogParser parser = new AccessLogParser();
        LogEntry entry = parser.parse(logLine);
        
        assert entry != null;
        assert "192.168.1.1".equals(entry.ip);
        assert "GET".equals(entry.method);
        assert "/index.html".equals(entry.path);
        assert entry.statusCode == 200;
        assert entry.responseBytes == 1024;
        
        System.out.println("✓ testParseValidLogLine");
    }

    static void testStatusClassification() {
        LogEntry e200 = new LogEntry("1.1.1.1", now(), "GET", "/", 200, 100);
        LogEntry e301 = new LogEntry("1.1.1.1", now(), "GET", "/", 301, 100);
        LogEntry e404 = new LogEntry("1.1.1.1", now(), "GET", "/", 404, 100);
        LogEntry e500 = new LogEntry("1.1.1.1", now(), "GET", "/", 500, 100);
        
        assert e200.statusClass() == 2;
        assert e301.statusClass() == 3;
        assert e404.statusClass() == 4;
        assert e500.statusClass() == 5;
        
        System.out.println("✓ testStatusClassification");
    }

    static void testStatusClassCounts() {
        List<LogEntry> entries = Arrays.asList(
            new LogEntry("1.1.1.1", now(), "GET", "/", 200, 100),
            new LogEntry("1.1.1.1", now(), "GET", "/", 201, 100),
            new LogEntry("1.1.1.1", now(), "GET", "/", 301, 100),
            new LogEntry("1.1.1.1", now(), "GET", "/", 404, 100),
            new LogEntry("1.1.1.1", now(), "GET", "/", 500, 100)
        );
        AccessLogStatistics stats = new AccessLogStatistics(entries);
        
        assert stats.getStatusClassCount(2) == 2;
        assert stats.getStatusClassCount(3) == 1;
        assert stats.getStatusClassCount(4) == 1;
        assert stats.getStatusClassCount(5) == 1;
        
        System.out.println("✓ testStatusClassCounts");
    }

    static void testTopFiveRequestedPaths() {
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            for (int j = 0; j < 11 - i; j++) {
                entries.add(new LogEntry("1.1.1.1", now(), "GET", "/path" + i, 200, 100));
            }
        }
        AccessLogStatistics stats = new AccessLogStatistics(entries);
        List<String> top5 = stats.getTopFiveRequestedPaths();
        
        assert top5.size() == 5;
        assert "/path1".equals(top5.get(0));
        
        System.out.println("✓ testTopFiveRequestedPaths");
    }

    static void testErrorRateCalculation() {
        LocalDateTime hour = LocalDateTime.of(2026, 8, 31, 14, 0);
        List<LogEntry> entries = Arrays.asList(
            new LogEntry("1.1.1.1", hour, "GET", "/", 200, 100),
            new LogEntry("1.1.1.1", hour, "GET", "/", 200, 100),
            new LogEntry("1.1.1.1", hour, "GET", "/", 404, 100),
            new LogEntry("1.1.1.1", hour, "GET", "/", 500, 100)
        );
        AccessLogStatistics stats = new AccessLogStatistics(entries);
        Double rate = stats.getErrorRatePerHour().get("2026-08-31 14:00");
        
        assert Math.abs(rate - 0.5) < 0.01;
        
        System.out.println("✓ testErrorRateCalculation");
    }

    static void testSuspiciousIpDetection() {
        LocalDateTime hour = LocalDateTime.of(2026, 8, 31, 14, 0);
        List<LogEntry> entries = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            entries.add(new LogEntry("192.168.1.100", hour, "GET", "/", 200, 100));
        }
        entries.add(new LogEntry("192.168.1.101", hour, "GET", "/", 200, 100));
        
        AccessLogStatistics stats = new AccessLogStatistics(entries);
        List<String> suspicious = stats.getSuspiciousIpAddresses();
        
        assert suspicious.size() == 1;
        assert "192.168.1.100".equals(suspicious.get(0));
        
        System.out.println("✓ testSuspiciousIpDetection");
    }

    static void runAllTests() {
        System.out.println("Running tests...\n");
        testParseValidLogLine();
        testStatusClassification();
        testStatusClassCounts();
        testTopFiveRequestedPaths();
        testErrorRateCalculation();
        testSuspiciousIpDetection();
        System.out.println("\nAll tests passed!");
    }

    static LocalDateTime now() {
        return LocalDateTime.of(2026, 8, 31, 14, 0);
    }
}
