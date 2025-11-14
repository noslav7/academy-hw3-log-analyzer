package academy.formatter;

import static academy.formatter.StatsFormattingSupport.formatDate;
import static academy.formatter.StatsFormattingSupport.formatDecimal;
import static academy.formatter.StatsFormattingSupport.formatInteger;
import static academy.formatter.StatsFormattingSupport.formatSize;
import static academy.formatter.StatsFormattingSupport.statusDescription;

import academy.stats.RequestPerDateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import java.util.stream.Collectors;

public class MarkdownStatsFormatter implements StatsFormatter {

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
                        .append(statusDescription(responseCodeStat.code()))
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
                        .append(perDateStat.weekday())
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
}

