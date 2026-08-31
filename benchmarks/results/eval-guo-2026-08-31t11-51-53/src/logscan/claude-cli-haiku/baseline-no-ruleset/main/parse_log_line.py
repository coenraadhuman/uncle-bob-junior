import re
from collections import defaultdict, Counter
from datetime import datetime

def parse_log_line(line):
    """Parse a single access log line."""
    # Pattern: IP - - [timestamp] "METHOD path" status bytes
    pattern = r'(\S+) - - \[([^\]]+)\] "(\S+) (\S+)" (\d+) (\d+|-)'
    match = re.match(pattern, line)
    if not match:
        return None
    
    ip, timestamp_str, method, path, status, bytes_str = match.groups()
    
    try:
        # Parse timestamp: "31/Aug/2026:10:15:30 +0000"
        timestamp = datetime.strptime(timestamp_str.split()[0], "%d/%b/%Y:%H:%M:%S")
        status = int(status)
        bytes_val = int(bytes_str) if bytes_str != '-' else 0
        return {
            'ip': ip,
            'timestamp': timestamp,
            'method': method,
            'path': path,
            'status': status,
            'bytes': bytes_val
        }
    except ValueError:
        return None

def get_status_class(status):
    """Get the status class (2xx, 3xx, 4xx, 5xx)."""
    return f"{status // 100}xx"

def analyze_log(filename):
    """Analyze the access log and return statistics."""
    status_counts = defaultdict(int)
    path_counts = Counter()
    hourly_ip_counts = defaultdict(lambda: defaultdict(int))
    hourly_requests = defaultdict(int)
    error_requests = defaultdict(int)
    total_requests = 0
    
    try:
        with open(filename, 'r') as f:
            for line in f:
                parsed = parse_log_line(line.strip())
                if not parsed:
                    continue
                
                total_requests += 1
                ip = parsed['ip']
                status = parsed['status']
                path = parsed['path']
                timestamp = parsed['timestamp']
                
                status_class = get_status_class(status)
                status_counts[status_class] += 1
                path_counts[path] += 1
                
                hour_key = timestamp.strftime("%Y-%m-%d %H:00")
                hourly_ip_counts[hour_key][ip] += 1
                hourly_requests[hour_key] += 1
                
                if status >= 400:
                    error_requests[hour_key] += 1
    
    except FileNotFoundError:
        print(f"Error: File '{filename}' not found.")
        return None
    
    suspicious_ips = defaultdict(list)
    for hour_key, ip_counts in hourly_ip_counts.items():
        for ip, count in ip_counts.items():
            if count > 100:
                suspicious_ips[hour_key].append((ip, count))
    
    return {
        'status_counts': status_counts,
        'path_counts': path_counts,
        'hourly_requests': hourly_requests,
        'error_requests': error_requests,
        'suspicious_ips': suspicious_ips,
        'total_requests': total_requests
    }

def print_report(stats):
    """Print a formatted report of the statistics."""
    if not stats:
        return
    
    print("=" * 70)
    print("WEB SERVER ACCESS LOG ANALYSIS REPORT")
    print("=" * 70)
    
    print("\n1. REQUEST COUNTS BY STATUS CLASS")
    print("-" * 70)
    for status in ['2xx', '3xx', '4xx', '5xx']:
        count = stats['status_counts'].get(status, 0)
        pct = (count / stats['total_requests'] * 100) if stats['total_requests'] > 0 else 0
        print(f"   {status}: {count:6d} requests ({pct:5.2f}%)")
    print(f"   Total: {stats['total_requests']:6d} requests")
    
    print("\n2. TOP 5 MOST REQUESTED PATHS")
    print("-" * 70)
    for rank, (path, count) in enumerate(stats['path_counts'].most_common(5), 1):
        pct = (count / stats['total_requests'] * 100) if stats['total_requests'] > 0 else 0
        print(f"   {rank}. {path:40s} {count:6d} requests ({pct:5.2f}%)")
    
    print("\n3. ERROR RATE PER HOUR")
    print("-" * 70)
    for hour in sorted(stats['hourly_requests'].keys()):
        total = stats['hourly_requests'][hour]
        errors = stats['error_requests'][hour]
        rate = (errors / total * 100) if total > 0 else 0
        print(f"   {hour}: {errors:4d}/{total:4d} errors ({rate:5.2f}%)")
    
    print("\n4. SUSPICIOUS IPs (>100 requests per hour)")
    print("-" * 70)
    if stats['suspicious_ips']:
        for hour in sorted(stats['suspicious_ips'].keys()):
            print(f"   {hour}:")
            for ip, count in sorted(stats['suspicious_ips'][hour], key=lambda x: -x[1]):
                print(f"      {ip:20s} {count:4d} requests")
    else:
        print("   No suspicious IPs detected.")
    
    print("\n" + "=" * 70)

if __name__ == "__main__":
    import sys
    
    if len(sys.argv) < 2:
        print("Usage: python solution.py <logfile>")
        sys.exit(1)
    
    stats = analyze_log(sys.argv[1])
    print_report(stats)
