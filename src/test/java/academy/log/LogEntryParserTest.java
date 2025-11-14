package academy.log;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEntryParserTest {

    private final LogEntryParser parser = new LogEntryParser();

    @Test
    void GivenValidLine_WhenParse_ThenReturnsLogEntry() {
        String line =
                "10.0.0.1 - john [05/May/2015:10:15:30 +0000] \"POST /submit HTTP/1.1\" 201 512 \"-\" \"Agent\"";

        Optional<LogEntry> result = parser.parse(line);

        assertTrue(result.isPresent());
        result.ifPresent(entry -> assertAll(
                () -> assertEquals("10.0.0.1", entry.remoteAddress()),
                () -> assertEquals("john", entry.remoteUser()),
                () -> assertEquals(
                        ZonedDateTime.of(
                                2015, 5, 5, 10, 15, 30, 0, ZoneOffset.UTC),
                        entry.timestamp()),
                () -> assertEquals("POST", entry.method()),
                () -> assertEquals("/submit", entry.resource()),
                () -> assertEquals("HTTP/1.1", entry.protocol()),
                () -> assertEquals(201, entry.statusCode()),
                () -> assertEquals(512L, entry.responseSize())));
    }

    @Test
    void GivenMalformedLine_WhenParse_ThenReturnsEmpty() {
        Optional<LogEntry> result = parser.parse("malformed line without expected format");

        assertFalse(result.isPresent());
    }

    @Test
    void GivenInvalidTimestamp_WhenParse_ThenReturnsEmpty() {
        String line =
                "10.0.0.1 - john [invalid-timestamp] \"GET /resource HTTP/1.1\" 200 123 \"-\" \"Agent\"";

        Optional<LogEntry> result = parser.parse(line);

        assertFalse(result.isPresent());
    }

    @Test
    void GivenMissingValues_WhenParse_ThenNormalizesResult() {
        String line =
                "10.0.0.1 - - [05/May/2015:10:15:30 +0000] \"-\" 200 0 \"-\" \"Agent\"";

        Optional<LogEntry> result = parser.parse(line);

        assertTrue(result.isPresent());
        result.ifPresent(entry -> assertAll(
                () -> assertEquals("", entry.remoteUser()),
                () -> assertEquals("", entry.method()),
                () -> assertEquals("", entry.resource()),
                () -> assertEquals("", entry.protocol())));
    }
}


