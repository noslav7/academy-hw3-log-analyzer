package academy.formatter;

import academy.stats.RequestPerDateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownStatsFormatter implements StatsFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final DecimalFormat INTEGER_FORMAT;
    private static final Map<Integer, String> STATUS_DESCRIPTIONS =
            Map.ofEntries(
                    Map.entry(100, "Continue"),
                    Map.entry(101, "Switching Protocols"),
                    Map.entry(200, "OK"),
                    Map.entry(201, "Created"),
                    Map.entry(202, "Accepted"),
                    Map.entry(204, "No Content"),
                    Map.entry(301, "Moved Permanently"),
                    Map.entry(302, "Found"),
                    Map.entry(304, "Not Modified"),
                    Map.entry(400, "Bad Request"),
                    Map.entry(401, "Unauthorized"),
                    Map.entry(403, "Forbidden"),
                    Map.entry(404, "Not Found"),
                    Map.entry(405, "Method Not Allowed"),
                    Map.entry(409, "Conflict"),
                    Map.entry(500, "Internal Server Error"),
                    Map.entry(501, "Not Implemented"),
                    Map.entry(502, "Bad Gateway"),
                    Map.entry(503, "Service Unavailable"));

    static {
        DECIMAL_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.US);
        DECIMAL_FORMAT.applyPattern("#0.00");

        INTEGER_FORMAT = (DecimalFormat) DecimalFormat.getIntegerInstance(Locale.US);
        INTEGER_FORMAT.setGroupingUsed(true);
    }

    @Override
    public String format(StatsResult statsResult) {
        StringBuilder builder = new StringBuilder();

        builder.append("#### Общая информация").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("|        Метрика        |     Значение |").append(System.lineSeparator());
        builder.append("|:---------------------:|-------------:|").append(System.lineSeparator());
        builder.append("|       Файл(-ы)        | ")
                .append(formatFiles(statsResult))
                .append(" |")
                .append(System.lineSeparator());
        builder.append("|    Начальная дата     | ")
                .append(formatDate(statsResult.firstRequestDate()))
                .append(" |")
                .append(System.lineSeparator());
        builder.append("|     Конечная дата     | ")
                .append(formatDate(statsResult.lastRequestDate()))
                .append(" |")
                .append(System.lineSeparator());
        builder.append("|  Количество запросов  | ")
                .append(formatInteger(statsResult.totalRequestsCount()))
                .append(" |")
                .append(System.lineSeparator());

        ResponseSizeStats responseSizeStats = statsResult.responseSizeInBytes();
        builder.append("| Средний размер ответа | ")
                .append(formatSize(responseSizeStats.average()))
                .append(" |")
                .append(System.lineSeparator());
        builder.append("|  Максимальный ответ   | ")
                .append(formatSize(responseSizeStats.max()))
                .append(" |")
                .append(System.lineSeparator());
        builder.append("|  95p размера ответа   | ")
                .append(formatSize(responseSizeStats.p95()))
                .append(" |")
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        builder.append("#### Запрашиваемые ресурсы")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        builder.append("|     Ресурс      | Количество |").append(System.lineSeparator());
        builder.append("|:---------------:|-----------:|").append(System.lineSeparator());
        if (statsResult.resources().isEmpty()) {
            builder.append("|        -        |          0 |").append(System.lineSeparator());
        } else {
            for (ResourceStat resourceStat : statsResult.resources()) {
                builder.append("|  `")
                        .append(resourceStat.resource())
                        .append("`  | ")
                        .append(formatInteger(resourceStat.totalRequestsCount()))
                        .append(" |")
                        .append(System.lineSeparator());
            }
        }
        builder.append(System.lineSeparator());

        builder.append("#### Коды ответа").append(System.lineSeparator()).append(System.lineSeparator());
        builder.append("| Код |          Имя          | Количество |").append(System.lineSeparator());
        builder.append("|:---:|:---------------------:|-----------:|").append(System.lineSeparator());
        if (statsResult.responseCodes().isEmpty()) {
            builder.append("|  -  |           -           |          0 |").append(System.lineSeparator());
        } else {
            for (ResponseCodeStat responseCodeStat : statsResult.responseCodes()) {
                builder.append("| ")
                        .append(responseCodeStat.code())
                        .append(" | ")
                        .append(padStatusName(getStatusDescription(responseCodeStat.code())))
                        .append(" | ")
                        .append(formatInteger(responseCodeStat.totalResponsesCount()))
                        .append(" |")
                        .append(System.lineSeparator());
            }
        }
        builder.append(System.lineSeparator());

        builder.append("#### Запросы по датам")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
        builder.append("|    Дата    |    День недели    | Количество |  Доля,% |").append(System.lineSeparator());
        builder.append("|:----------:|:-----------------:|-----------:|--------:|").append(System.lineSeparator());
        if (statsResult.requestsPerDate().isEmpty()) {
            builder.append("|     -      |         -         |          0 |   0.00 |")
                    .append(System.lineSeparator());
        } else {
            for (RequestPerDateStat perDateStat : statsResult.requestsPerDate()) {
                builder.append("| ")
                        .append(perDateStat.date())
                        .append(" | ")
                        .append(padStatusName(perDateStat.weekday()))
                        .append(" | ")
                        .append(formatInteger(perDateStat.totalRequestsCount()))
                        .append(" | ")
                        .append(formatDecimal(perDateStat.totalRequestsPercentage()))
                        .append(" |")
                        .append(System.lineSeparator());
            }
        }
        builder.append(System.lineSeparator());

        builder.append("#### Используемые протоколы").append(System.lineSeparator()).append(System.lineSeparator());
        if (statsResult.uniqueProtocols().isEmpty()) {
            builder.append("- `-`").append(System.lineSeparator());
        } else {
            for (String protocol : statsResult.uniqueProtocols()) {
                builder.append("- `").append(protocol).append("`").append(System.lineSeparator());
            }
        }

        return builder.toString();
    }

    private static String formatFiles(StatsResult statsResult) {
        if (statsResult.files().isEmpty()) {
            return "`-`";
        }
        return statsResult.files().stream()
                .map(file -> "`" + file + "`")
                .collect(Collectors.joining(", "));
    }

    private static String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return DATE_FORMATTER.format(date);
    }

    private static String formatInteger(long value) {
        return INTEGER_FORMAT.format(value);
    }

    private static String formatSize(BigDecimal value) {
        return DECIMAL_FORMAT.format(value) + "b";
    }

    private static String getStatusDescription(int code) {
        return STATUS_DESCRIPTIONS.getOrDefault(code, "Unknown");
    }

    private static String padStatusName(String value) {
        return value;
    }

    private static String formatDecimal(BigDecimal value) {
        return DECIMAL_FORMAT.format(value);
    }
}

