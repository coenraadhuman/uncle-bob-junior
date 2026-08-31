import re
from collections import defaultdict, Counter
from datetime import datetime
import sys

def parse_log_line(line):
    """Parse a log line in the format: IP - - [timestamp] "METHOD path" status bytes"""
    pattern = r'(\S+) - - \[([^\]]+)\] "(\S+) ([^"]+)" (\d+) (\d+)'
    match = re.match(pattern, line.strip())
    if not match:
        return None
    
    ip, timestamp, method, path, status, bytes_sent = match.groups()
    return {
        'ip': ip,
        'timestamp': timestamp,
        'path': path,
        'status': int(status),
    }

def get_hour_from_timestamp(timestamp_str):
    """Extract hour from timestamp like '31/Aug/2026:14:30:45 +0000'"""
    try:
        dt_part = timestamp_str.split(' ')[0]
        dt = datetime.strptime(dt_part, '%d/%b/%Y:%H:%M:%S')
        return dt.replace(minute=0, second=0, microsecond=0)
    except:
        return None

def get_status_class(status):
    """Get status class from status code (e.g., 200 -> 2xx)"""
    return f"{status // 100}xx"

def analyze_log(log_lines):
    """Analyze log lines and return statistics"""
    status_counts = defaultdict(int)
    path_counts = Counter()
    hourly_ip_requests = defaultdict(lambda: defaultdict(int))
    hourly_error_count = defaultdict(int)
    hourly_total_count = defaultdict(int)
    
    for line in log_lines:
        parsed = parse_log_line(line)
        if not parsed:
            continue
        
        ip = parsed['ip']
        status = parsed['status']
        path = parsed['path']
        timestamp = parsed['timestamp']
        
        # Count by status class
        status_class = get_status_class(status)
        status_counts[status_class] += 1
        
        # Count by path
        path_counts[path] += 1
        
        # Hourly statistics
        hour = get_hour_from_timestamp(timestamp)
        if hour:
            hourly_ip_requests[hour][ip] += 1
            hourly_total_count[hour] += 1
            if status >= 400:
                hourly_error_count[hour] += 1
    
    return status_counts, path_counts, hourly_ip_requests, hourly_error_count, hourly_total_count

def print_report(status_counts, path_counts, hourly_ip_requests, hourly_error_count, hourly_total_count):
    """Print a readable report"""
    print("=" * 80)
    print("WEB SERVER ACCESS LOG ANALYSIS REPORT")
    print("=" * 80)
    print()
    
    print("REQUEST COUNTS BY STATUS CLASS:")
    print("-" * 40)
    if status_counts:
        for status_class in sorted(status_counts.keys()):
            count = status_counts[status_class]
            print(f"  {status_class}: {count:,} requests")
    else:
        print("  No requests found.")
    print()
    
    print("TOP 5 MOST REQUESTED PATHS:")
    print("-" * 40)
    if path_counts:
        for i, (path, count) in enumerate(path_counts.most_common(5), 1):
            print(f"  {i}. {path:<50} {count:,} requests")
    else:
        print("  No paths found.")
    print()
    
    print("ERROR RATE PER HOUR:")
    print("-" * 40)
    if hourly_total_count:
        for hour in sorted(hourly_total_count.keys()):
            total = hourly_total_count[hour]
            errors = hourly_error_count[hour]
            error_rate = (errors / total * 100) if total > 0 else 0
            print(f"  {hour.strftime('%Y-%m-%d %H:00')}: {error_rate:6.2f}% ({errors}/{total})")
    else:
        print("  No hourly data found.")
    print()
    
    print("SUSPICIOUS IPs (>100 REQUESTS IN A SINGLE HOUR):")
    print("-" * 40)
    suspicious_found = False
    for hour in sorted(hourly_ip_requests.keys()):
        for ip, count in sorted(hourly_ip_requests[hour].items()):
            if count > 100:
                print(f"  {ip:<20} {count:3d} requests at {hour.strftime('%Y-%m-%d %H:00')}")
                suspicious_found = True
    if not suspicious_found:
        print("  No suspicious IPs found.")
    print()
    print("=" * 80)

def main():
    if len(sys.argv) > 1:
        try:
            with open(sys.argv[1], 'r') as f:
                log_lines = f.readlines()
        except FileNotFoundError:
            print(f"Error: File '{sys.argv[1]}' not found.", file=sys.stderr)
            sys.exit(1)
    else:
        log_lines = sys.stdin.readlines()
    
    status_counts, path_counts, hourly_ip_requests, hourly_error_count, hourly_total_count = analyze_log(log_lines)
    print_report(status_counts, path_counts, hourly_ip_requests, hourly_error_count, hourly_total_count)

if __name__ == '__main__':
    main()
