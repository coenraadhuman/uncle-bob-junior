import java.time.*;
import java.util.*;

class LogEntry {
    private final String ip;
    private final ZonedDateTime timestamp;
    private final String method;
    private final String path;
    private final int statusCode;
    private final long responseBytes;
    
    LogEntry(String ip, ZonedDateTime timestamp, String method, String path, 
             int statusCode, long responseBytes) {
        this.ip = Objects.requireNonNull(ip);
        this.timestamp = Objects.requireNonNull(timestamp);
        this.method = Objects.requireNonNull(method);
        this.path = Objects.requireNonNull(path);
        this.statusCode = statusCode;
        this.responseBytes = responseBytes;
    }
    
    String ip() { return ip; }
    ZonedDateTime timestamp() { return timestamp; }
    int statusCode() { return statusCode; }
    String path() { return path; }
}
