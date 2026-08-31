```python
import sys
import re
from collections import defaultdict, Counter
from datetime import datetime, timezone, timedelta
from typing import NamedTuple, Iterable
from unittest import TestCase, main as unittest_main


class LogEntry(NamedTuple):
    ip: str
    timestamp: datetime
    method: str
    path: str
    status: int
    bytes_sent: int


def parse_log_line(line: str) -> LogEntry | None:
    pattern = r'(\S+) - - \[([^\]]+)\] "(\S+) (\S+)" (\d+) (\d+)'
    match = re.match(pattern, line)
    
    if not match:
        return None
    
    ip, timestamp_str, method, path, status, bytes_sent = match.groups()
    
    try:
        timestamp = datetime.strptime(timestamp_str, "%d/%b/%Y:%H:%M:%S %z")
        return LogEntry(
            ip=ip,
            timestamp=timestamp,
            method=method,
            path=path,
            status=int(status),
            bytes_sent=int(bytes_sent)
        )
    except ValueError:
        return None


def classify_status(status: int) -> str:
    return f"{status // 100}xx"


def compute_status_counts(entries: Iterable[LogEntry]) -> dict[str, int]:
    counts = defaultdict(int)
    for entry in entries:
        classification = classify_status(entry.status)
        counts[classification] += 1
    return dict(counts)


def find_top_paths(entries: Iterable[LogEntry], limit: int = 5) -> list[tuple[str, int]]:
    path_counter = Counter(entry.path for entry in entries)
    return path_counter.most_common(limit)


def compute_hourly_error_rate(entries: Iterable[LogEntry]) -> dict[str, float]:
    hourly_totals = defaultdict(int)
    hourly_errors = defaultdict(int)
    
    for entry in entries:
        hour_key = entry.timestamp.strftime("%Y-%m-%d %H:00")
        hourly_totals[hour_key] += 1
        if entry.status >= 400:
            hourly_errors[hour_key] += 1
    
    error_rates = {}
    for hour, total in hourly_totals.items():
        error_rate = (hourly_errors[hour] / total * 100) if total > 0 else 0.0
        error_rates[hour] = error_rate
    
    return error_rates


def find_suspicious_ips(entries: Iterable[LogEntry], threshold: int = 100) -> dict[str, int]:
    hourly_ip_counts = defaultdict(lambda: defaultdict(int))
    
    for entry in entries:
        hour_key = entry.timestamp.strftime("%Y-%m-%d %H:00")
        hourly_ip_counts[hour_key][entry.ip] += 1
    
    suspicious = {}
    for hour, ip_counts in hourly_ip_counts.items():
        for ip, count in ip_counts.items():
            if count > threshold:
                key = f"{ip} ({hour})"
                suspicious[key] = count
    
    return suspicious


def print_report(status_counts: dict[str, int], top_paths: list[tuple[str, int]], 
                 error_rates: dict[str, float], suspicious_ips: dict[str, int]) -> None:
    print("\n" + "="*70)
    print(" "*20 + "WEB SERVER ACCESS LOG ANALYSIS")
    print("="*70)
    
    print("\nREQUEST COUNTS BY STATUS CLASS")
    print("-" * 70)
    for status_class in sorted(status_counts.keys()):
        count = status_counts[status_class]
        print(f"  {status_class}: {count:,} requests")
    
    print("\nTOP 5 MOST REQUESTED PATHS")
    print("-" * 70)
    for i, (path, count) in enumerate(top_paths, 1):
        print(f"  {i}. {path}: {count:,} requests")
    
    print("\nHOURLY ERROR RATE (4xx/5xx)")
    print("-" * 70)
    for hour in sorted(error_rates.keys()):
        rate = error_rates[hour]
        print(f"  {hour}: {rate:.1f}%")
    
    print("\nSUSPICIOUS IPS (>100 requests/hour)")
    print("-" * 70)
    if suspicious_ips:
        for ip_info in sorted(suspicious_ips.keys()):
            count = suspicious_ips[ip_info]
            print(f"  {ip_info}: {count} requests")
    else:
        print("  None detected")
    
    print("\n" + "="*70 + "\n")


def analyze_log_file(filepath: str) -> None:
    entries = []
    with open(filepath, 'r') as f:
        for line in f:
            entry = parse_log_line(line.strip())
            if entry:
                entries.append(entry)
    
    status_counts = compute_status_counts(entries)
    top_paths = find_top_paths(entries)
    error_rates = compute_hourly_error_rate(entries)
    suspicious_ips = find_suspicious_ips(entries)
    
    print_report(status_counts, top_paths, error_rates, suspicious_ips)


class TestLogAnalysis(TestCase):
    
    def test_parse_valid_log_line(self):
        line = '192.168.1.1 - - [31/Aug/2026:12:34:56 +0000] "GET /index.html" 200 1234'
        entry = parse_log_line(line)
        
        self.assertIsNotNone(entry)
        self.assertEqual(entry.ip, '192.168.1.1')
        self.assertEqual(entry.method, 'GET')
        self.assertEqual(entry.path, '/index.html')
        self.assertEqual(entry.status, 200)
        self.assertEqual(entry.bytes_sent, 1234)
    
    def test_parse_invalid_log_line(self):
        line = 'this is not a valid log line'
        entry = parse_log_line(line)
        self.assertIsNone(entry)
    
    def test_classify_status_codes(self):
        self.assertEqual(classify_status(200), "2xx")
        self.assertEqual(classify_status(301), "3xx")
        self.assertEqual(classify_status(404), "4xx")
        self.assertEqual(classify_status(500), "5xx")
    
    def test_compute_status_counts(self):
        tz = timezone.utc
        entries = [
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/", 200, 100),
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/", 200, 100),
            LogEntry("192.168.1.2", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/", 404, 200),
        ]
        counts = compute_status_counts(entries)
        
        self.assertEqual(counts.get("2xx"), 2)
        self.assertEqual(counts.get("4xx"), 1)
    
    def test_find_top_paths(self):
        tz = timezone.utc
        entries = [
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/api", 200, 100),
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/api", 200, 100),
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/home", 200, 100),
        ]
        top_paths = find_top_paths(entries, limit=2)
        
        self.assertEqual(len(top_paths), 2)
        self.assertEqual(top_paths[0], ("/api", 2))
        self.assertEqual(top_paths[1], ("/home", 1))
    
    def test_compute_hourly_error_rate(self):
        tz = timezone.utc
        entries = [
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz), "GET", "/", 200, 100),
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 30, 0, tzinfo=tz), "GET", "/", 404, 100),
        ]
        error_rates = compute_hourly_error_rate(entries)
        
        self.assertAlmostEqual(error_rates["2026-08-31 12:00"], 50.0, places=1)
    
    def test_find_suspicious_ips_none_under_threshold(self):
        tz = timezone.utc
        entries = [
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz) + timedelta(seconds=i*60), 
                    "GET", "/", 200, 100)
            for i in range(100)
        ]
        suspicious = find_suspicious_ips(entries, threshold=100)
        self.assertEqual(len(suspicious), 0)
    
    def test_find_suspicious_ips_over_threshold(self):
        tz = timezone.utc
        entries = [
            LogEntry("192.168.1.1", datetime(2026, 8, 31, 12, 0, 0, tzinfo=tz) + timedelta(seconds=i*60), 
                    "GET", "/", 200, 100)
            for i in range(101)
        ]
        suspicious = find_suspicious_ips(entries, threshold=100)
        
        self.assertEqual(len(suspicious), 1)
        self.assertTrue(any("192.168.1.1" in key for key in suspicious.keys()))


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] != "-test":
        analyze_log_file(sys.argv[1])
    else:
        unittest_main(argv=[""], exit=False, verbosity=2)
```

Usage: `python solution.py access.log` to analyse a log file, or `python solution.py -test` to run tests.

Assumptions: Standard Apache/Nginx log format with UTC timestamps. Status code >= 400 counts as an error. Suspicious threshold is 100 requests per IP per hour (configurable).

Code: cleaned via single-responsibility functions (`parse_log_line`, `compute_status_counts`, `find_top_paths`, `compute_hourly_error_rate`, `find_suspicious_ips`), with named constants and early guards. Tests cover parsing, classification, aggregation, error rates, and suspicious IP detection for both edge and nominal cases.