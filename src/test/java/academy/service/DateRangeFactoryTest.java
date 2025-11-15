package academy.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.exception.InvalidArgumentsException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateRangeFactoryTest {

    private final DateRangeFactory factory = new DateRangeFactory();

    @Test
    void GivenValidFromAndTo_WhenCreate_ThenReturnsDateRange() {
        DateRange range = factory.create("2025-01-01", "2025-01-31");

        assertAll(
                () -> assertEquals(LocalDate.of(2025, 1, 1), range.from()),
                () -> assertEquals(LocalDate.of(2025, 1, 31), range.to()));
    }

    @Test
    void GivenNullValues_WhenCreate_ThenAllowsOpenInterval() {
        DateRange range = factory.create(null, null);

        assertAll(() -> assertNull(range.from()), () -> assertNull(range.to()));
    }

    @Test
    void GivenBlankFrom_WhenCreate_ThenThrowsInvalidArgumentsException() {
        assertThrows(InvalidArgumentsException.class, () -> factory.create("   ", "2025-01-31"));
    }

    @Test
    void GivenInvalidFormat_WhenCreate_ThenThrowsInvalidArgumentsException() {
        assertThrows(InvalidArgumentsException.class, () -> factory.create("2025/01/01", null));
    }

    @Test
    void GivenFromAfterTo_WhenCreate_ThenThrowsInvalidArgumentsException() {
        assertThrows(InvalidArgumentsException.class, () -> factory.create("2025-02-01", "2025-01-31"));
    }
}
