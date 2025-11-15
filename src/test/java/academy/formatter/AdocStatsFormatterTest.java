package academy.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.stats.RequestPerDateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdocStatsFormatterTest {

    private final AdocStatsFormatter formatter = new AdocStatsFormatter();

    @Test
    void GivenStatsResult_WhenFormat_ThenReturnsExpectedAdoc() {
        StatsResult statsResult = new StatsResult(
                List.of("part1.txt", "part2.txt"),
                3L,
                new ResponseSizeStats(new BigDecimal("123.45"), new BigDecimal("678.90"), new BigDecimal("234.56")),
                List.of(new ResourceStat("/downloads/product_1", 2L), new ResourceStat("/downloads/product_2", 1L)),
                List.of(new ResponseCodeStat(200, 2L), new ResponseCodeStat(404, 1L)),
                List.of(
                        new RequestPerDateStat(LocalDate.of(2024, 1, 1), "Monday", 2L, new BigDecimal("66.67")),
                        new RequestPerDateStat(LocalDate.of(2024, 1, 2), "Tuesday", 1L, new BigDecimal("33.33"))),
                List.of("HTTP/1.1", "grpc"),
                2L,
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2));

        String newline = System.lineSeparator();
        String expected = new StringBuilder()
                .append("=== Общая информация")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Метрика | Значение")
                .append(newline)
                .append("| Файл(-ы) | +part1.txt+, +part2.txt+")
                .append(newline)
                .append("| Начальная дата | 01.01.2024")
                .append(newline)
                .append("| Конечная дата | 02.01.2024")
                .append(newline)
                .append("| Количество запросов | 3")
                .append(newline)
                .append("| Количество уникальных протоколов | 2")
                .append(newline)
                .append("| Средний размер ответа | 123.45b")
                .append(newline)
                .append("| Максимальный ответ | 678.90b")
                .append(newline)
                .append("| 95p размера ответа | 234.56b")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Запрашиваемые ресурсы")
                .append(newline)
                .append(newline)
                .append("[cols=\"2,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Ресурс | Количество")
                .append(newline)
                .append("| +/downloads/product_1+ | 2")
                .append(newline)
                .append("| +/downloads/product_2+ | 1")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Коды ответа")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,2,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Код | Имя | Количество")
                .append(newline)
                .append("| 200 | OK | 2")
                .append(newline)
                .append("| 404 | Not Found | 1")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Запросы по датам")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,1,1,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Дата | День недели | Количество | Доля,%")
                .append(newline)
                .append("| 2024-01-01 | Monday | 2 | 66.67")
                .append(newline)
                .append("| 2024-01-02 | Tuesday | 1 | 33.33")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Используемые протоколы")
                .append(newline)
                .append(newline)
                .append("* +HTTP/1.1+")
                .append(newline)
                .append("* +grpc+")
                .append(newline)
                .toString();

        assertEquals(expected, formatter.format(statsResult));
    }

    @Test
    void GivenEmptyStats_WhenFormat_ThenReturnsAdocWithPlaceholders() {
        StatsResult statsResult = new StatsResult(
                List.of(),
                0L,
                new ResponseSizeStats(new BigDecimal("0.00"), new BigDecimal("0.00"), new BigDecimal("0.00")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                0L,
                null,
                null);

        String newline = System.lineSeparator();
        String expected = new StringBuilder()
                .append("=== Общая информация")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Метрика | Значение")
                .append(newline)
                .append("| Файл(-ы) | +-+")
                .append(newline)
                .append("| Начальная дата | -")
                .append(newline)
                .append("| Конечная дата | -")
                .append(newline)
                .append("| Количество запросов | 0")
                .append(newline)
                .append("| Количество уникальных протоколов | 0")
                .append(newline)
                .append("| Средний размер ответа | 0.00b")
                .append(newline)
                .append("| Максимальный ответ | 0.00b")
                .append(newline)
                .append("| 95p размера ответа | 0.00b")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Запрашиваемые ресурсы")
                .append(newline)
                .append(newline)
                .append("[cols=\"2,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Ресурс | Количество")
                .append(newline)
                .append("| - | 0")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Коды ответа")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,2,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Код | Имя | Количество")
                .append(newline)
                .append("| - | - | 0")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Запросы по датам")
                .append(newline)
                .append(newline)
                .append("[cols=\"1,1,1,1\",options=\"header\"]")
                .append(newline)
                .append("|===")
                .append(newline)
                .append("| Дата | День недели | Количество | Доля,%")
                .append(newline)
                .append("| - | - | 0 | 0.00")
                .append(newline)
                .append("|===")
                .append(newline)
                .append(newline)
                .append("=== Используемые протоколы")
                .append(newline)
                .append(newline)
                .append("* +-+")
                .append(newline)
                .toString();

        assertEquals(expected, formatter.format(statsResult));
    }
}
