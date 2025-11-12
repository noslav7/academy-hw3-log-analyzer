package academy.log;

import java.time.ZonedDateTime;

public record LogEntry(
        String remoteAddress,
        String remoteUser,
        ZonedDateTime timestamp,
        String method,
        String resource,
        String protocol,
        int statusCode,
        long responseSize) {}

