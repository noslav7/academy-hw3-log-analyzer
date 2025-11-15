package academy.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class DateRangeTest {

    @Test
    void GivenNullBounds_WhenIncludes_ThenReturnsTrue() {
        DateRange range = new DateRange(null, null);
        ZonedDateTime timestamp = LocalDate.of(2024, 1, 5).atStartOfDay(ZoneOffset.UTC);

        assertTrue(range.includes(timestamp));
    }

    @Test
    void GivenTimestampBeforeFrom_WhenIncludes_ThenReturnsFalse() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 10));
        ZonedDateTime timestamp = LocalDate.of(2024, 1, 4).atStartOfDay(ZoneOffset.UTC);

        assertFalse(range.includes(timestamp));
    }

    @Test
    void GivenTimestampAfterTo_WhenIncludes_ThenReturnsFalse() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 10));
        ZonedDateTime timestamp = LocalDate.of(2024, 1, 11).atStartOfDay(ZoneOffset.UTC);

        assertFalse(range.includes(timestamp));
    }

    @Test
    void GivenTimestampWithinRange_WhenIncludes_ThenReturnsTrue() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 5), LocalDate.of(2024, 1, 10));
        ZonedDateTime timestamp = LocalDate.of(2024, 1, 7).atStartOfDay(ZoneOffset.UTC);

        assertTrue(range.includes(timestamp));
    }
}
