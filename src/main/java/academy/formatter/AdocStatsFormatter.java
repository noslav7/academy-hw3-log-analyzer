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
import java.util.List;
import java.util.stream.Collectors;

public class AdocStatsFormatter implements StatsFormatter {

    @Override
    public String format(StatsResult statsResult) {
        String newline = System.lineSeparator();
        StringBuilder builder = new StringBuilder();

        builder.append("=== Общая информация").append(newline).append(newline);
        builder.append("[cols=\"1,1\",options=\"header\"]").append(newline);
        builder.append("|===").append(newline);
        builder.append("| Метрика | Значение").append(newline);
        builder.append("| Файл(-ы) | ")
                .append(formatFiles(statsResult.files()))
                .append(newline);
        builder.append("| Начальная дата | ")
                .append(formatDate(statsResult.firstRequestDate()))
                .append(newline);
        builder.append("| Конечная дата | ")
                .append(formatDate(statsResult.lastRequestDate()))
                .append(newline);
        builder.append("| Количество запросов | ")
                .append(formatInteger(statsResult.totalRequestsCount()))
                .append(newline);

        ResponseSizeStats responseSizeStats = statsResult.responseSizeInBytes();
        builder.append("| Средний размер ответа | ")
                .append(formatSize(responseSizeStats.average()))
                .append(newline);
        builder.append("| Максимальный ответ | ")
                .append(formatSize(responseSizeStats.max()))
                .append(newline);
        builder.append("| 95p размера ответа | ")
                .append(formatSize(responseSizeStats.p95()))
                .append(newline);
        builder.append("|===").append(newline).append(newline);

        builder.append("=== Запрашиваемые ресурсы").append(newline).append(newline);
        builder.append("[cols=\"2,1\",options=\"header\"]").append(newline);
        builder.append("|===").append(newline);
        builder.append("| Ресурс | Количество").append(newline);
        if (statsResult.resources().isEmpty()) {
            builder.append("| - | 0").append(newline);
        } else {
            for (ResourceStat resourceStat : statsResult.resources()) {
                builder.append("| ")
                        .append(formatResource(resourceStat.resource()))
                        .append(" | ")
                        .append(formatInteger(resourceStat.totalRequestsCount()))
                        .append(newline);
            }
        }
        builder.append("|===").append(newline).append(newline);

        builder.append("=== Коды ответа").append(newline).append(newline);
        builder.append("[cols=\"1,2,1\",options=\"header\"]").append(newline);
        builder.append("|===").append(newline);
        builder.append("| Код | Имя | Количество").append(newline);
        if (statsResult.responseCodes().isEmpty()) {
            builder.append("| - | - | 0").append(newline);
        } else {
            for (ResponseCodeStat responseCodeStat : statsResult.responseCodes()) {
                builder.append("| ")
                        .append(responseCodeStat.code())
                        .append(" | ")
                        .append(statusDescription(responseCodeStat.code()))
                        .append(" | ")
                        .append(formatInteger(responseCodeStat.totalResponsesCount()))
                        .append(newline);
            }
        }
        builder.append("|===").append(newline).append(newline);

        builder.append("=== Запросы по датам").append(newline).append(newline);
        builder.append("[cols=\"1,1,1,1\",options=\"header\"]").append(newline);
        builder.append("|===").append(newline);
        builder.append("| Дата | День недели | Количество | Доля,%").append(newline);
        if (statsResult.requestsPerDate().isEmpty()) {
            builder.append("| - | - | 0 | 0.00").append(newline);
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
                        .append(newline);
            }
        }
        builder.append("|===").append(newline).append(newline);

        builder.append("=== Используемые протоколы").append(newline).append(newline);
        if (statsResult.uniqueProtocols().isEmpty()) {
            builder.append("* +-+").append(newline);
        } else {
            for (String protocol : statsResult.uniqueProtocols()) {
                builder.append("* ")
                        .append(formatProtocol(protocol))
                        .append(newline);
            }
        }

        return builder.toString();
    }

    private static String formatFiles(List<String> files) {
        if (files.isEmpty()) {
            return "+-+";
        }
        return files.stream()
                .map(AdocStatsFormatter::formatProtocol)
                .collect(Collectors.joining(", "));
    }

    private static String formatResource(String resource) {
        return formatProtocol(resource);
    }

    private static String formatProtocol(String value) {
        return "+" + value + "+";
    }
}


