package academy.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.log.LogEntryParser;
import academy.stats.StatsCollector;
import academy.stats.StatsResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LogProcessingServiceTest {

    private final LogProcessingService service = new LogProcessingService(new LogEntryParser());

    @Test
    void GivenMixedLogLines_WhenProcess_ThenRegistersEntriesWithinDateRange() throws IOException {
        String newline = System.lineSeparator();
        String lines = String.join(
                newline,
                "93.180.71.3 - - [02/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Agent\"",
                "invalid line",
                "93.180.71.3 - - [03/May/2015:08:05:32 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 100 \"-\" \"Agent\"");
        BufferedReader reader = new BufferedReader(new StringReader(lines));
        DateRange dateRange = new DateRange(LocalDate.of(2015, 5, 2), LocalDate.of(2015, 5, 2));
        StatsCollector collector = new StatsCollector();

        service.process(reader, "source.log", dateRange, collector);

        StatsResult result = collector.buildResult(List.of("source.log"));

        assertAll(
                () -> assertEquals(1L, result.totalRequestsCount()),
                () -> assertEquals(List.of("source.log"), result.files()),
                () -> assertEquals(
                        List.of("/downloads/product_1"),
                        result.resources().stream()
                                .map(resource -> resource.resource())
                                .toList()));
    }
}
