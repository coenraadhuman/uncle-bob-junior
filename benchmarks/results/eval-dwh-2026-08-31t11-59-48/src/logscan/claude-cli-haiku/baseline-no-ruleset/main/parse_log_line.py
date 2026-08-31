import re
import sys
from datetime import datetime
from collections import Counter, defaultdict

def parse_log_line(line):
    """Parse Apache/Nginx access log line."""
    pattern = r'(\S+)\s+\S+\s+\S+\s+\[([^\]]+)\]\s+"(\S+)\s+(\S+)"\s+(\d+)\s+(\d+)'
    match = re.match(pattern, line.strip())
    if not match:
        return None
    
    ip, timestamp_str, method, path, status, bytes_str = match.groups()
    
    try:
        ts_clean = timestamp_str.rsplit(' ', 1)[0] if ' ' in timestamp_str else timestamp_str
        dt = datetime.strptime(ts_clean, '%d/%b/%Y:%H:%M:%S')
        hour_key = dt.strftime('%Y-%m-%d %H:00:00')
        return {
            'ip': ip,
            'hour_key': hour_key,
            'path': path,
            'status': int(status),
        }
    except ValueError:
        return None

def get_status_class(status):
    """Return status class (2xx, 3xx, 4xx, 5xx)."""
    return f"{status // 100}xx"

def analyze_log(lines):
    """Analyze log lines and return metrics."""
    status_counts = Counter()
    path_counts = Counter()
    hourly_requests = defaultdict(lambda: defaultdict(int))
    hourly_errors = defaultdict(int)
    hourly_total = defaultdict(int)
    
    parsed_count = 0
    
    for line in lines:
        entry = parse_log_line(line)
        if not entry:
            continue
        
        parsed_count += 1
        status = entry['status']
        path = entry['path']
        ip = entry['ip']
        hour_key = entry['hour_key']
        
        status_class = get_status_class(status)
        status_counts[status_class] += 1
        path_counts[path] += 1
        hourly_requests[hour_key][ip] += 1
        
        if status >= 400:
            hourly_errors[hour_key] += 1
        hourly_total[hour_key] += 1
    
    return {
        'status_counts': status_counts,
        'path_counts': path_counts,
        'hourly_requests': hourly_requests,
        'hourly_errors': hourly_errors,
        'hourly_total': hourly_total,
        'parsed_count': parsed_count
    }

def print_report(metrics):
    """Print a readable report."""
    print("=" * 70)
    print("WEB SERVER ACCESS LOG ANALYSIS REPORT")
    print("=" * 70)
    
    print(f"\nTotal lines parsed: {metrics['parsed_count']}")
    
    print("\n--- REQUEST COUNT BY STATUS CLASS ---")
    for status_class in ['2xx', '3xx', '4xx', '5xx']:
        count = metrics['status_counts'].get(status_class, 0)
        print(f"  {status_class}: {count}")
    
    print("\n--- TOP 5 REQUESTED PATHS ---")
    top_paths = metrics['path_counts'].most_common(5)
    if top_paths:
        for idx, (path, count) in enumerate(top_paths, 1):
            print(f"  {idx}. {path} ({count} requests)")
    else:
        print("  No paths found")
    
    print("\n--- ERROR RATE PER HOUR ---")
    hours = sorted(metrics['hourly_total'].keys())
    if hours:
        for hour in hours:
            total = metrics['hourly_total'][hour]
            errors = metrics['hourly_errors'][hour]
            error_rate = (errors / total * 100) if total > 0 else 0
            print(f"  {hour}: {error_rate:.1f}% ({errors}/{total})")
    else:
        print("  No hourly data found")
    
    print("\n--- SUSPICIOUS IPs (>100 requests/hour) ---")
    suspicious = []
    for hour, ips in metrics['hourly_requests'].items():
        for ip, count in ips.items():
            if count > 100:
                suspicious.append((hour, ip, count))
    
    if suspicious:
        suspicious.sort(key=lambda x: x[2], reverse=True)
        for hour, ip, count in suspicious:
            print(f"  {hour}: {ip} ({count} requests)")
    else:
        print("  None detected")
    
    print("\n" + "=" * 70)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        with open(sys.argv[1], 'r') as f:
            lines = f.readlines()
    else:
        lines = sys.stdin.readlines()
    
    metrics = analyze_log(lines)
    print_report(metrics)
