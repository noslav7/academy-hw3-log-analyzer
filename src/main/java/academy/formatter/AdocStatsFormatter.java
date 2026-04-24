package academy.formatter;

import java.util.List;
import java.util.stream.Collectors;

/** Формирует отчёт в формате AsciiDoc. */
public class AdocStatsFormatter extends StructuredStatsFormatter {

    /** Платформенно-зависимый перевод строки. */
    private static final String NEWLINE = System.lineSeparator();

    /** Рендерит общую секцию отчёта в таблицу AsciiDoc. */
    @Override
    protected void renderGeneralInfo(StringBuilder builder, List<GeneralInfoRow> rows) {
        builder.append("=== Общая информация").append(NEWLINE).append(NEWLINE);
        builder.append("[cols=\"1,1\",options=\"header\"]").append(NEWLINE);
        builder.append("|===").append(NEWLINE);
        builder.append("| Метрика | Значение").append(NEWLINE);
        for (GeneralInfoRow row : rows) {
            builder.append(labelFor(row.metric())).append(row.value()).append(NEWLINE);
        }
        builder.append("|===").append(NEWLINE).append(NEWLINE);
    }

    /** Рендерит секцию ресурсов в таблицу AsciiDoc. */
    @Override
    protected void renderResources(StringBuilder builder, SectionData<ResourceRow> section) {
        builder.append("=== Запрашиваемые ресурсы").append(NEWLINE).append(NEWLINE);
        builder.append("[cols=\"2,1\",options=\"header\"]").append(NEWLINE);
        builder.append("|===").append(NEWLINE);
        builder.append("| Ресурс | Количество").append(NEWLINE);
        if (!section.hasData()) {
            builder.append("| - | 0").append(NEWLINE);
        } else {
            for (ResourceRow row : section.rows()) {
                builder.append("| ")
                        .append(formatResource(row.resource()))
                        .append(" | ")
                        .append(row.totalRequests())
                        .append(NEWLINE);
            }
        }
        builder.append("|===").append(NEWLINE).append(NEWLINE);
    }

    /** Рендерит секцию HTTP-кодов в таблицу AsciiDoc. */
    @Override
    protected void renderResponseCodes(StringBuilder builder, SectionData<ResponseCodeRow> section) {
        builder.append("=== Коды ответа").append(NEWLINE).append(NEWLINE);
        builder.append("[cols=\"1,2,1\",options=\"header\"]").append(NEWLINE);
        builder.append("|===").append(NEWLINE);
        builder.append("| Код | Имя | Количество").append(NEWLINE);
        if (!section.hasData()) {
            builder.append("| - | - | 0").append(NEWLINE);
        } else {
            for (ResponseCodeRow row : section.rows()) {
                builder.append("| ")
                        .append(row.code())
                        .append(" | ")
                        .append(row.description())
                        .append(" | ")
                        .append(row.totalResponses())
                        .append(NEWLINE);
            }
        }
        builder.append("|===").append(NEWLINE).append(NEWLINE);
    }

    /** Рендерит секцию запросов по датам в таблицу AsciiDoc. */
    @Override
    protected void renderRequestsPerDate(StringBuilder builder, SectionData<RequestsPerDateRow> section) {
        builder.append("=== Запросы по датам").append(NEWLINE).append(NEWLINE);
        builder.append("[cols=\"1,1,1,1\",options=\"header\"]").append(NEWLINE);
        builder.append("|===").append(NEWLINE);
        builder.append("| Дата | День недели | Количество | Доля,%").append(NEWLINE);
        if (!section.hasData()) {
            builder.append("| - | - | 0 | 0.00").append(NEWLINE);
        } else {
            for (RequestsPerDateRow row : section.rows()) {
                builder.append("| ")
                        .append(row.date())
                        .append(" | ")
                        .append(row.weekday())
                        .append(" | ")
                        .append(row.totalRequests())
                        .append(" | ")
                        .append(row.percentage())
                        .append(NEWLINE);
            }
        }
        builder.append("|===").append(NEWLINE).append(NEWLINE);
    }

    /** Рендерит список протоколов в виде маркированного списка AsciiDoc. */
    @Override
    protected void renderProtocols(StringBuilder builder, SectionData<String> section) {
        builder.append("=== Используемые протоколы").append(NEWLINE).append(NEWLINE);
        if (!section.hasData()) {
            builder.append("* +-+").append(NEWLINE);
        } else {
            for (String protocol : section.rows()) {
                builder.append("* ").append(formatProtocol(protocol)).append(NEWLINE);
            }
        }
    }

    /** Форматирует список файлов для ячейки AsciiDoc-таблицы. */
    @Override
    protected String formatFilesCell(List<String> files) {
        if (files.isEmpty()) {
            return "+-+";
        }
        return files.stream().map(this::formatProtocol).collect(Collectors.joining(", "));
    }

    /** Возвращает подпись метрики для общей секции AsciiDoc. */
    private static String labelFor(GeneralMetric metric) {
        return switch (metric) {
            case FILES -> "| Файл(-ы) | ";
            case FIRST_DATE -> "| Начальная дата | ";
            case LAST_DATE -> "| Конечная дата | ";
            case TOTAL_REQUESTS -> "| Количество запросов | ";
            case UNIQUE_PROTOCOLS -> "| Количество уникальных протоколов | ";
            case RESPONSE_SIZE_AVERAGE -> "| Средний размер ответа | ";
            case RESPONSE_SIZE_MAX -> "| Максимальный ответ | ";
            case RESPONSE_SIZE_P95 -> "| 95p размера ответа | ";
        };
    }

    /** Форматирует ресурс в inline-monospace синтаксис AsciiDoc. */
    private String formatResource(String resource) {
        return formatProtocol(resource);
    }

    /** Оборачивает значение в monospace-синтаксис AsciiDoc. */
    private String formatProtocol(String value) {
        return "+" + value + "+";
    }
}
