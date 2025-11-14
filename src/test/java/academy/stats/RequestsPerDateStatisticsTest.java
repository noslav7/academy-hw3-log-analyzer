package academy.stats;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestsPerDateStatisticsTest {

    @Test
    void GivenRegisteredDates_WhenBuild_ThenReturnSortedStatsWithPercentages() {
        RequestsPerDateStatistics statistics = new RequestsPerDateStatistics();
        statistics.register(LocalDate.of(2024, 3, 2));
        statistics.register(LocalDate.of(2024, 3, 2));
        statistics.register(LocalDate.of(2024, 3, 1));

        List<RequestPerDateStat> result = statistics.build(3L);

        assertEquals(
                List.of(
                        new RequestPerDateStat(
                                LocalDate.of(2024, 3, 1), "Friday", 1L, new BigDecimal("33.33")),
                        new RequestPerDateStat(
                                LocalDate.of(2024, 3, 2), "Saturday", 2L, new BigDecimal("66.67"))),
                result);
    }

    @Test
    void GivenNoRegistrations_WhenBuild_ThenReturnsEmptyList() {
        RequestsPerDateStatistics statistics = new RequestsPerDateStatistics();

        List<RequestPerDateStat> result = statistics.build(0L);

        assertTrue(result.isEmpty());
    }
}


