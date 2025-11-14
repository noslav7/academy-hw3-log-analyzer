package academy.formatter;

import academy.stats.RequestPerDateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonStatsFormatterTest {

    private final JsonStatsFormatter formatter = new JsonStatsFormatter();

    @Test
    void GivenStatsResult_WhenFormat_ThenReturnsExpectedJson() {
        StatsResult statsResult = new StatsResult(
                List.of("part1.txt", "part2.txt"),
                3L,
                new ResponseSizeStats(
                        new BigDecimal("123.45"), new BigDecimal("678.90"), new BigDecimal("234.56")),
                List.of(
                        new ResourceStat("/downloads/product_1", 2L),
                        new ResourceStat("/downloads/product_2", 1L)),
                List.of(
                        new ResponseCodeStat(200, 2L),
                        new ResponseCodeStat(404, 1L)),
                List.of(
                        new RequestPerDateStat(
                                LocalDate.of(2024, 1, 1), "Monday", 2L, new BigDecimal("66.67")),
                        new RequestPerDateStat(
                                LocalDate.of(2024, 1, 2), "Tuesday", 1L, new BigDecimal("33.33"))),
                List.of("HTTP/1.1", "grpc"),
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2));

        String expected = """
                {
                  "files" : [ "part1.txt", "part2.txt" ],
                  "totalRequestsCount" : 3,
                  "responseSizeInBytes" : {
                    "average" : 123.45,
                    "max" : 678.90,
                    "p95" : 234.56
                  },
                  "resources" : [ {
                    "resource" : "/downloads/product_1",
                    "totalRequestsCount" : 2
                  }, {
                    "resource" : "/downloads/product_2",
                    "totalRequestsCount" : 1
                  } ],
                  "responseCodes" : [ {
                    "code" : 200,
                    "totalResponsesCount" : 2
                  }, {
                    "code" : 404,
                    "totalResponsesCount" : 1
                  } ],
                  "requestsPerDate" : [ {
                    "date" : "2024-01-01",
                    "weekday" : "Monday",
                    "totalRequestsCount" : 2,
                    "totalRequestsPercentage" : 66.67
                  }, {
                    "date" : "2024-01-02",
                    "weekday" : "Tuesday",
                    "totalRequestsCount" : 1,
                    "totalRequestsPercentage" : 33.33
                  } ],
                  "uniqueProtocols" : [ "HTTP/1.1", "grpc" ]
                }
                """;

        assertEquals(
                normalizeMultiline(expected),
                normalizeMultiline(formatter.format(statsResult)));
    }

    private static String normalizeMultiline(String value) {
        return value.strip().replace("\r\n", "\n");
    }
}


