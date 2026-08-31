```python
import re
from collections import defaultdict
from datetime import datetime
from typing import Dict, List, Tuple, Optional


class LogAnalyzer:
    LOG_PATTERN = r'^(\S+) - - \[([^\]]+)\] "(\S+) (\S+)" (\d+) (\d+)'
    SUSPICIOUS_REQUEST_THRESHOLD = 100
    
    def __init__(self):
        self.status_classes = defaultdict(int)
        self.paths = defaultdict(int)
        self.hourly_ips = defaultdict(lambda: defaultdict(int))
        self.hourly_requests = defaultdict(int)
        self.hourly_errors = defaultdict(int)
        self.total_requests = 0
    
    def parse_line(self, line: str) -> Optional[Dict]:
        match = re.match(self.LOG_PATTERN, line.strip())
        if not match:
            return None
        
        ip, timestamp, method, path, status, bytes_sent = match.groups()
        return {
            'ip': ip,
            'timestamp': timestamp,
            'method': method,
            'path': path,
            'status': int(status),
            'bytes': int(bytes_sent)
        }
    
    def extract_hour(self, timestamp: str) -> str:
        try:
            dt = datetime.strptime(timestamp, '%d/%b/%Y:%H:%M:%S %z')
            return dt.strftime('%Y-%m-%d %H:00')
        except ValueError:
            return 'unknown'
    
    def get_status_class(self, status: int) -> str:
        return f"{status // 100}xx"
    
    def is_error(self, status: int) -> bool:
        return status >= 400
    
    def process_log(self, log_content: str):
        for line in log_content.strip().split('\n'):
            if not line:
                continue
            self._process_line(line)
    
    def _process_line(self, line: str):
        parsed = self.parse_line(line)
        if not parsed:
            return
        
        self.total_requests += 1
        status = parsed['status']
        self.status_classes[self.get_status_class(status)] += 1
        self.paths[parsed['path']] += 1
        
        hour = self.extract_hour(parsed['timestamp'])
        self.hourly_ips[hour][parsed['ip']] += 1
        self.hourly_requests[hour] += 1
        
        if self.is_error(status):
            self.hourly_errors[hour] += 1
    
    def get_suspicious_ips(self) -> List[Tuple[str, str, int]]:
        suspicious = []
        for hour, ips in self.hourly_ips.items():
            for ip, count in ips.items():
                if count > self.SUSPICIOUS_REQUEST_THRESHOLD:
                    suspicious.append((hour, ip, count))
        return sorted(suspicious)
    
    def get_top_paths(self, limit: int = 5) -> List[Tuple[str, int]]:
        return sorted(self.paths.items(), key=lambda x: x[1], reverse=True)[:limit]
    
    def calculate_error_rates(self) -> Dict[str, float]:
        rates = {}
        for hour in self.hourly_requests:
            total = self.hourly_requests[hour]
            errors = self.hourly_errors[hour]
            rates[hour] = (errors / total * 100) if total > 0 else 0.0
        return rates
    
    def generate_report(self) -> str:
        report = []
        report.append("=" * 70)
        report.append("WEB SERVER ACCESS LOG ANALYSIS REPORT")
        report.append("=" * 70)
        report.append("")
        
        self._append_status_summary(report)
        self._append_top_paths(report)
        self._append_error_rates(report)
        self._append_suspicious_ips(report)
        
        report.append("=" * 70)
        return "\n".join(report)
    
    def _append_status_summary(self, report: List[str]):
        report.append("REQUEST COUNTS BY STATUS CLASS")
        report.append("-" * 70)
        for status_class in sorted(self.status_classes.keys()):
            count = self.status_classes[status_class]
            percentage = (count / self.total_requests * 100) if self.total_requests > 0 else 0
            report.append(f"  {status_class}: {count:6d} ({percentage:5.1f}%)")
        report.append(f"  Total:  {self.total_requests:6d}")
        report.append("")
    
    def _append_top_paths(self, report: List[str]):
        report.append("TOP 5 MOST REQUESTED PATHS")
        report.append("-" * 70)
        for path, count in self.get_top_paths(5):
            percentage = (count / self.total_requests * 100) if self.total_requests > 0 else 0
            report.append(f"  {count:6d} ({percentage:5.1f}%) {path}")
        report.append("")
    
    def _append_error_rates(self, report: List[str]):
        report.append("ERROR RATE PER HOUR")
        report.append("-" * 70)
        error_rates = self.calculate_error_rates()
        for hour in sorted(error_rates.keys()):
            rate = error_rates[hour]
            requests = self.hourly_requests[hour]
            errors = self.hourly_errors[hour]
            report.append(f"  {hour}: {rate:5.1f}% ({errors}/{requests})")
        report.append("")
    
    def _append_suspicious_ips(self, report: List[str]):
        report.append("SUSPICIOUS IPs (>100 requests per hour)")
        report.append("-" * 70)
        suspicious = self.get_suspicious_ips()
        if suspicious:
            for hour, ip, count in suspicious:
                report.append(f"  {hour}: {ip} ({count} requests)")
        else:
            report.append("  None detected")
        report.append("")


def test_parse_valid_line():
    analyzer = LogAnalyzer()
    line = '192.168.1.1 - - [31/Aug/2026:10:15:30 +0000] "GET /index.html" 200 1234'
    parsed = analyzer.parse_line(line)
    assert parsed is not None
    assert parsed['ip'] == '192.168.1.1'
    assert parsed['path'] == '/index.html'
    assert parsed['method'] == 'GET'
    assert parsed['status'] == 200


def test_parse_invalid_line():
    analyzer = LogAnalyzer()
    assert analyzer.parse_line('malformed log') is None


def test_status_class():
    analyzer = LogAnalyzer()
    assert analyzer.get_status_class(200) == '2xx'
    assert analyzer.get_status_class(404) == '4xx'
    assert analyzer.get_status_class(500) == '5xx'


def test_error_detection():
    analyzer = LogAnalyzer()
    assert analyzer.is_error(200) is False
    assert analyzer.is_error(404) is True


def test_status_counting():
    analyzer = LogAnalyzer()
    log = """192.168.1.1 - - [31/Aug/2026:10:15:30 +0000] "GET /a" 200 100
192.168.1.1 - - [31/Aug/2026:10:16:45 +0000] "GET /b" 404 100
192.168.1.1 - - [31/Aug/2026:10:17:12 +0000] "GET /c" 500 100"""
    analyzer.process_log(log)
    assert analyzer.total_requests == 3
    assert analyzer.status_classes['2xx'] == 1
    assert analyzer.status_classes['4xx'] == 1


def test_path_ranking():
    analyzer = LogAnalyzer()
    log = """192.168.1.1 - - [31/Aug/2026:10:15:30 +0000] "GET /home" 200 100
192.168.1.1 - - [31/Aug/2026:10:16:45 +0000] "GET /home" 200 100
192.168.1.1 - - [31/Aug/2026:10:17:12 +0000] "GET /api" 200 100
192.168.1.1 - - [31/Aug/2026:10:18:00 +0000] "GET /home" 200 100"""
    analyzer.process_log(log)
    top = analyzer.get_top_paths(2)
    assert top[0][0] == '/home'
    assert top[0][1] == 3


def test_suspicious_ips_threshold():
    analyzer = LogAnalyzer()
    log_lines = [f'192.168.1.1 - - [31/Aug/2026:10:{i//60:02d}:{i%60:02d} +0000] "GET /x" 200 1' 
                 for i in range(101)]
    analyzer.process_log('\n'.join(log_lines))
    suspicious = analyzer.get_suspicious_ips()
    assert len(suspicious) == 1
    assert suspicious[0][2] == 101


def test_exactly_100_not_suspicious():
    analyzer = LogAnalyzer()
    log_lines = [f'192.168.1.1 - - [31/Aug/2026:10:{i//60:02d}:{i%60:02d} +0000] "GET /x" 200 1' 
                 for i in range(100)]
    analyzer.process_log('\n'.join(log_lines))
    suspicious = analyzer.get_suspicious_ips()
    assert len(suspicious) == 0


def test_error_rate_calculation():
    analyzer = LogAnalyzer()
    log = """192.168.1.1 - - [31/Aug/2026:10:15:30 +0000] "GET /a" 200 1
192.168.1.1 - - [31/Aug/2026:10:16:45 +0000] "GET /b" 404 1
192.168.1.1 - - [31/Aug/2026:10:17:12 +0000] "GET /c" 500 1"""
    analyzer.process_log(log)
    rates = analyzer.calculate_error_rates()
    hour_key = list(rates.keys())[0]
    assert abs(rates[hour_key] - 66.67) < 0.1


if __name__ == '__main__':
    test_parse_valid_line()
    test_parse_invalid_line()
    test_status_class()
    test_error_detection()
    test_status_counting()
    test_path_ranking()
    test_suspicious_ips_threshold()
    test_exactly_100_not_suspicious()
    test_error_rate_calculation()
    
    sample_log = """192.168.1.1 - - [31/Aug/2026:10:15:30 +0000] "GET /index.html" 200 1234
192.168.1.2 - - [31/Aug/2026:10:16:45 +0000] "POST /api/login" 200 567
192.168.1.1 - - [31/Aug/2026:10:17:12 +0000] "GET /images/logo.png" 200 5678
192.168.1.3 - - [31/Aug/2026:10:18:00 +0000] "GET /nonexistent" 404 0
192.168.1.1 - - [31/Aug/2026:10:19:33 +0000] "POST /api/data" 500 0
192.168.1.2 - - [31/Aug/2026:10:20:15 +0000] "GET /api/users" 200 8901
192.168.1.1 - - [31/Aug/2026:10:21:00 +0000] "GET /index.html" 200 1234
192.168.1.3 - - [31/Aug/2026:10:22:10 +0000] "GET /index.html" 301 0
192.168.1.2 - - [31/Aug/2026:10:23:45 +0000] "GET /api/users" 200 8901"""
    
    analyzer = LogAnalyzer()
    analyzer.process_log(sample_log)
    print(analyzer.generate_report())
```

Cleaned: extracted `_process_line` to keep per-line logic isolated and under 10 statements; added named constant `SUSPICIOUS_REQUEST_THRESHOLD` replacing magic 100; split report generation into focused methods (`_append_*`) for single responsibility; extracted helper methods for status classification, error detection, and calculations. Safe to change because: each method does one thing (parsing, classification, aggregation, formatting); all parsing errors are handled with guard clauses and None returns; tests cover parsing, classification, thresholds, and calculations; new log formats only need changes to `parse_line` and `extract_hour`.