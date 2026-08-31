import re
from collections import defaultdict, Counter
from datetime import datetime

LOG_PATTERN = r'(\S+) - - \[([^\]]+)\] "(\w+) (\S+)" (\d+) (\d+)'
TIMESTAMP_FORMAT = '%d/%b/%Y:%H:%M:%S'
HOUR_FORMAT = '%Y-%m-%d %H:00'
SUSPICIOUS_REQUEST_THRESHOLD = 100

def extract_log_fields(line):
    match = re.match(LOG_PATTERN, line)
    if not match:
        return None
    
    ip, timestamp_str, method, path, status, bytes_str = match.groups()
    return {
        'ip': ip,
        'timestamp': timestamp_str,
        'method': method,
        'path': path,
        'status': int(status),
        'bytes': int(bytes_str),
    }

def classify_status_code(status):
    if 200 <= status < 300:
        return '2xx'
    if 300 <= status < 400:
        return '3xx'
    if 400 <= status < 500:
        return '4xx'
    if 500 <= status < 600:
        return '5xx'
    return 'unknown'

def extract_hour_key(timestamp_str):
    time_part = timestamp_str.split()[0]
    dt = datetime.strptime(time_part, TIMESTAMP_FORMAT)
    return dt.strftime(HOUR_FORMAT)

def analyze_logs(lines):
    status_counts = defaultdict(int)
    path_requests = Counter()
    requests_by_ip_and_hour = defaultdict(lambda: defaultdict(int))
    total_by_hour = defaultdict(int)
    errors_by_hour = defaultdict(int)
    
    for line in lines:
        fields = extract_log_fields(line)
        if not fields:
            continue
        
        status = fields['status']
        status_class = classify_status_code(status)
        status_counts[status_class] += 1
        
        path_requests[fields['path']] += 1
        
        hour = extract_hour_key(fields['timestamp'])
        requests_by_ip_and_hour[hour][fields['ip']] += 1
        total_by_hour[hour] += 1
        
        if status >= 400:
            errors_by_hour[hour] += 1
    
    return {
        'status_counts': dict(status_counts),
        'top_paths': path_requests.most_common(5),
        'requests_by_ip_and_hour': requests_by_ip_and_hour,
        'total_by_hour': total_by_hour,
        'errors_by_hour': errors_by_hour,
    }

def find_suspicious_ips(stats):
    suspicious = []
    for hour, ip_counts in stats['requests_by_ip_and_hour'].items():
        for ip, count in ip_counts.items():
            if count > SUSPICIOUS_REQUEST_THRESHOLD:
                suspicious.append((ip, hour, count))
    return suspicious

def print_report(stats):
    suspicious = find_suspicious_ips(stats)
    
    print("=== Web Server Log Analysis Report ===\n")
    
    print("Status Code Distribution:")
    for status_class in ['2xx', '3xx', '4xx', '5xx']:
        count = stats['status_counts'].get(status_class, 0)
        print(f"  {status_class}: {count}")
    print()
    
    print("Top 5 Requested Paths:")
    if stats['top_paths']:
        for rank, (path, count) in enumerate(stats['top_paths'], 1):
            print(f"  {rank}. {path}: {count} requests")
    else:
        print("  (No paths found)")
    print()
    
    print("Error Rate per Hour:")
    if stats['total_by_hour']:
        for hour in sorted(stats['total_by_hour'].keys()):
            total = stats['total_by_hour'][hour]
            errors = stats['errors_by_hour'].get(hour, 0)
            rate = (errors / total * 100) if total > 0 else 0
            print(f"  {hour}: {rate:.2f}% ({errors}/{total} requests)")
    else:
        print("  (No hourly data)")
    print()
    
    print("Suspicious IPs (>100 requests in single hour):")
    if suspicious:
        for ip, hour, count in sorted(suspicious):
            print(f"  {ip} at {hour}: {count} requests")
    else:
        print("  None detected")
