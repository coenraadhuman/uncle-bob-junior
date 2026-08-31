import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.regex.*;

class LogLineParser {
    private static final String LOG_PATTERN = 
        "^(\\S+) - - \\[(.*?)\\] \"(\\S+) ([^\"]*)\" (\\d+) (\\d+)$";
    private static final Pattern pattern = Pattern.compile(LOG_PATTERN);
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
    
    Optional<LogEntry> parseLine(String line) {
        Matcher match = pattern.matcher(line);
        if (!match.find()) return Optional.empty();
        
        try {
            String ip = match.group(1);
            ZonedDateTime time = ZonedDateTime.parse(match.group(2), TIMESTAMP_FORMAT);
            String method = match.group(3);
            String path = match.group(4);
            int status = Integer.parseInt(match.group(5));
            long bytes = Long.parseLong(match.group(6));
            
            return Optional.of(new LogEntry(ip, time, method, path, status, bytes));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
