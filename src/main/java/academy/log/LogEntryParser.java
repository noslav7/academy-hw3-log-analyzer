package academy.log;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogEntryParser {

    private static final Logger LOGGER = LogManager.getLogger(LogEntryParser.class);

    private static final Pattern LOG_PATTERN =
            Pattern.compile(
                    "^(?<ip>\\S+)\\s+(?<ident>\\S+)\\s+(?<user>\\S+)\\s+\\[(?<time>[^\\]]+)]\\s+\"(?<request>[^\"]*)\"\\s+(?<status>\\d{3})\\s+(?<size>\\d+)(?:\\s+\"(?<referer>[^\"]*)\"\\s+\"(?<agent>[^\"]*)\")?$");
    private static final DateTimeFormatter LOG_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("d/MMM/yyyy:HH:mm:ss Z", Locale.US);

    public Optional<LogEntry> parse(String line) {
        Matcher matcher = LOG_PATTERN.matcher(line);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        String timestampRaw = matcher.group("time");
        ZonedDateTime timestamp;
        try {
            timestamp = ZonedDateTime.parse(timestampRaw, LOG_TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            LOGGER.warn("Unable to parse timestamp: {}", timestampRaw, ex);
            return Optional.empty();
        }

        String request = matcher.group("request");
        RequestParts requestParts = extractRequestParts(request);

        int statusCode = Integer.parseInt(matcher.group("status"));
        long responseSize = Long.parseLong(matcher.group("size"));

        return Optional.of(
                new LogEntry(
                        matcher.group("ip"),
                        normalizeValue(matcher.group("user")),
                        timestamp,
                        requestParts.method(),
                        requestParts.resource(),
                        requestParts.protocol(),
                        statusCode,
                        responseSize));
    }

    private static String normalizeValue(String value) {
        if (value == null || "-".equals(value)) {
            return "";
        }
        return value;
    }

    private static RequestParts extractRequestParts(String request) {
        if (request == null || request.isBlank() || "-".equals(request)) {
            return new RequestParts("", "", "");
        }

        String[] segments = Arrays.stream(request.trim().split("\\s+"))
                .filter(segment -> !segment.isBlank())
                .toArray(String[]::new);
        if (segments.length == 0) {
            return new RequestParts("", "", "");
        }

        String method = segments[0].toUpperCase(Locale.ROOT);
        String resource = segments.length > 1 ? segments[1] : "";
        String protocol = segments.length > 2 ? String.join(" ", Arrays.copyOfRange(segments, 2, segments.length)) : "";

        return new RequestParts(method, resource, protocol);
    }

    private record RequestParts(String method, String resource, String protocol) {}
}

