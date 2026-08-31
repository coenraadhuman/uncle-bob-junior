def test_extract_log_fields_valid():
    line = '192.168.1.1 - - [31/Aug/2026:10:15:30 +0200] "GET /index.html" 200 1234'
    fields = extract_log_fields(line)
    assert fields['ip'] == '192.168.1.1'
    assert fields['status'] == 200
    assert fields['path'] == '/index.html'

def test_extract_log_fields_invalid():
    assert extract_log_fields('invalid line') is None

def test_classify_status_code():
    assert classify_status_code(200) == '2xx'
    assert classify_status_code(404) == '4xx'
    assert classify_status_code(500) == '5xx'

def test_extract_hour_key():
    assert extract_hour_key('31/Aug/2026:10:15:30 +0200') == '2026-08-31 10:00'

def test_analyze_logs_status_distribution():
    lines = [
        '192.168.1.1 - - [31/Aug/2026:10:15:30 +0200] "GET /p1" 200 1000',
        '192.168.1.2 - - [31/Aug/2026:10:16:30 +0200] "GET /p2" 301 500',
        '192.168.1.3 - - [31/Aug/2026:10:17:30 +0200] "GET /p3" 404 512',
    ]
    stats = analyze_logs(lines)
    assert stats['status_counts']['2xx'] == 1
    assert stats['status_counts']['3xx'] == 1
    assert stats['status_counts']['4xx'] == 1

def test_analyze_logs_top_paths():
    lines = [
        '192.168.1.1 - - [31/Aug/2026:10:15:30 +0200] "GET /api/users" 200 1000',
        '192.168.1.1 - - [31/Aug/2026:10:16:30 +0200] "GET /api/users" 200 1000',
        '192.168.1.1 - - [31/Aug/2026:10:17:30 +0200] "GET /home" 200 1000',
    ]
    stats = analyze_logs(lines)
    assert stats['top_paths'][0] == ('/api/users', 2)
    assert stats['top_paths'][1] == ('/home', 1)

def test_analyze_logs_error_rate():
    lines = [
        '192.168.1.1 - - [31/Aug/2026:10:15:30 +0200] "GET /p" 200 1000',
        '192.168.1.1 - - [31/Aug/2026:10:16:30 +0200] "GET /p" 500 1000',
    ]
    stats = analyze_logs(lines)
    hour = '2026-08-31 10:00'
    assert stats['total_by_hour'][hour] == 2
    assert stats['errors_by_hour'][hour] == 1

def test_find_suspicious_ips():
    lines = ['192.168.1.1 - - [31/Aug/2026:10:30:00 +0200] "GET /page" 200 1000'] * 101
    stats = analyze_logs(lines)
    suspicious = find_suspicious_ips(stats)
    assert len(suspicious) == 1
    assert suspicious[0][0] == '192.168.1.1'
    assert suspicious[0][2] == 101

def test_analyze_logs_empty():
    stats = analyze_logs([])
    assert stats['status_counts'] == {}
    assert stats['top_paths'] == []

if __name__ == '__main__':
    import sys
    
    test_extract_log_fields_valid()
    test_extract_log_fields_invalid()
    test_classify_status_code()
    test_extract_hour_key()
    test_analyze_logs_status_distribution()
    test_analyze_logs_top_paths()
    test_analyze_logs_error_rate()
    test_find_suspicious_ips()
    test_analyze_logs_empty()
    print("All tests passed!\n")
    
    sample_logs = [
        '192.168.1.1 - - [31/Aug/2026:10:15:30 +0200] "GET /index.html" 200 1234',
        '192.168.1.2 - - [31/Aug/2026:10:16:45 +0200] "POST /api/login" 200 567',
        '192.168.1.3 - - [31/Aug/2026:10:17:12 +0200] "GET /about.html" 404 89',
        '192.168.1.1 - - [31/Aug/2026:10:18:00 +0200] "GET /index.html" 500 456',
    ]
    stats = analyze_logs(sample_logs)
    print_report(stats)
