package academy.stats;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.log.LogEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class StatsCollectorTest {

    @Test
    void GivenCollectedEntries_WhenBuildResult_ThenAggregatesStatistics() {
        StatsCollector collector = new StatsCollector();
        collector.register(new LogEntry(
                "10.0.0.1",
                "",
                LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC),
                "GET",
                "/downloads/product_1",
                "HTTP/1.1",
                200,
                100L));
        collector.register(new LogEntry(
                "10.0.0.2",
                "",
                LocalDate.of(2024, 1, 1).atStartOfDay(ZoneOffset.UTC),
                "GET",
                "/downloads/product_1",
                "HTTP/1.1",
                304,
                200L));
        collector.register(new LogEntry(
                "10.0.0.3",
                "",
                LocalDate.of(2024, 1, 2).atStartOfDay(ZoneOffset.UTC),
                "GET",
                "/downloads/product_2",
                "grpc",
                404,
                300L));

        StatsResult result = collector.buildResult(List.of("part1.log", "part2.log"));

        assertAll(
                () -> assertEquals(List.of("part1.log", "part2.log"), result.files()),
                () -> assertEquals(3L, result.totalRequestsCount()),
                () -> assertEquals(
                        new BigDecimal("200.00"), result.responseSizeInBytes().average()),
                () -> assertEquals(
                        new BigDecimal("300.00"), result.responseSizeInBytes().max()),
                () -> assertEquals(
                        new BigDecimal("290.00"), result.responseSizeInBytes().p95()),
                () -> assertEquals(
                        List.of(
                                new ResourceStat("/downloads/product_1", 2L),
                                new ResourceStat("/downloads/product_2", 1L)),
                        result.resources()),
                () -> assertEquals(
                        List.of(
                                new ResponseCodeStat(200, 1L),
                                new ResponseCodeStat(304, 1L),
                                new ResponseCodeStat(404, 1L)),
                        result.responseCodes()),
                () -> assertEquals(
                        List.of(
                                new RequestPerDateStat(LocalDate.of(2024, 1, 1), "Monday", 2L, new BigDecimal("66.67")),
                                new RequestPerDateStat(
                                        LocalDate.of(2024, 1, 2), "Tuesday", 1L, new BigDecimal("33.33"))),
                        result.requestsPerDate()),
                () -> assertEquals(List.of("HTTP/1.1", "grpc"), result.uniqueProtocols()),
                () -> assertEquals(2L, result.uniqueProtocolsCount()),
                () -> assertEquals(LocalDate.of(2024, 1, 1), result.firstRequestDate()),
                () -> assertEquals(LocalDate.of(2024, 1, 2), result.lastRequestDate()));
    }

    @Test
    void GivenNoEntries_WhenBuildResult_ThenReturnsZeroedStats() {
        StatsCollector collector = new StatsCollector();

        StatsResult result = collector.buildResult(List.of());

        assertAll(
                () -> assertTrue(result.files().isEmpty()),
                () -> assertEquals(0L, result.totalRequestsCount()),
                () -> assertEquals(
                        new BigDecimal("0.00"), result.responseSizeInBytes().average()),
                () -> assertEquals(
                        new BigDecimal("0.00"), result.responseSizeInBytes().max()),
                () -> assertEquals(
                        new BigDecimal("0.00"), result.responseSizeInBytes().p95()),
                () -> assertTrue(result.resources().isEmpty()),
                () -> assertTrue(result.responseCodes().isEmpty()),
                () -> assertTrue(result.requestsPerDate().isEmpty()),
                () -> assertTrue(result.uniqueProtocols().isEmpty()),
                () -> assertEquals(0L, result.uniqueProtocolsCount()),
                () -> assertNull(result.firstRequestDate()),
                () -> assertNull(result.lastRequestDate()));
    }
}
