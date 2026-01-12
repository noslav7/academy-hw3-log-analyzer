package academy.formatter;

import java.util.List;
import java.util.stream.Collectors;

/** Генерирует отчёт в формате Markdown, используя табличные секции. */
public class MarkdownStatsFormatter extends StructuredStatsFormatter {

    private static final String NEWLINE = System.lineSeparator();

    @Override
    protected void renderGeneralInfo(StringBuilder builder, List<GeneralInfoRow> rows) {
        builder.append("#### Общая информация").append(NEWLINE).append(NEWLINE);
        builder.append("|        Метрика        |     Значение |").append(NEWLINE);
        builder.append("|:---------------------:|-------------:|").append(NEWLINE);
        for (GeneralInfoRow row : rows) {
            builder.append(labelFor(row.metric()))
                    .append(row.value())
                    .append(" |")
                    .append(NEWLINE);
        }
        builder.append(NEWLINE);
    }

    @Override
    protected void renderResources(StringBuilder builder, SectionData<ResourceRow> section) {
        builder.append("#### Запрашиваемые ресурсы").append(NEWLINE).append(NEWLINE);
        builder.append("|     Ресурс      | Количество |").append(NEWLINE);
        builder.append("|:---------------:|-----------:|").append(NEWLINE);
        if (!section.hasData()) {
            builder.append("|        -        |          0 |").append(NEWLINE);
        } else {
            for (ResourceRow row : section.rows()) {
                builder.append("|  `")
                        .append(row.resource())
                        .append("`  | ")
                        .append(row.totalRequests())
                        .append(" |")
                        .append(NEWLINE);
            }
        }
        builder.append(NEWLINE);
    }

    @Override
    protected void renderResponseCodes(StringBuilder builder, SectionData<ResponseCodeRow> section) {
        builder.append("#### Коды ответа").append(NEWLINE).append(NEWLINE);
        builder.append("| Код |          Имя          | Количество |").append(NEWLINE);
        builder.append("|:---:|:---------------------:|-----------:|").append(NEWLINE);
        if (!section.hasData()) {
            builder.append("|  -  |           -           |          0 |").append(NEWLINE);
        } else {
            for (ResponseCodeRow row : section.rows()) {
                builder.append("| ")
                        .append(row.code())
                        .append(" | ")
                        .append(row.description())
                        .append(" | ")
                        .append(row.totalResponses())
                        .append(" |")
                        .append(NEWLINE);
            }
        }
        builder.append(NEWLINE);
    }

    @Override
    protected void renderRequestsPerDate(StringBuilder builder, SectionData<RequestsPerDateRow> section) {
        builder.append("#### Запросы по датам").append(NEWLINE).append(NEWLINE);
        builder.append("|    Дата    |    День недели    | Количество |  Доля,% |")
                .append(NEWLINE);
        builder.append("|:----------:|:-----------------:|-----------:|--------:|")
                .append(NEWLINE);
        if (!section.hasData()) {
            builder.append("|     -      |         -         |          0 |   0.00 |")
                    .append(NEWLINE);
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
                        .append(" |")
                        .append(NEWLINE);
            }
        }
        builder.append(NEWLINE);
    }

    @Override
    protected void renderProtocols(StringBuilder builder, SectionData<String> section) {
        builder.append("#### Используемые протоколы").append(NEWLINE).append(NEWLINE);
        if (!section.hasData()) {
            builder.append("- `-`").append(NEWLINE);
        } else {
            for (String protocol : section.rows()) {
                builder.append("- `").append(protocol).append("`").append(NEWLINE);
            }
        }
    }

    @Override
    protected String formatFilesCell(List<String> files) {
        if (files.isEmpty()) {
            return "`-`";
        }
        return files.stream().map(file -> "`" + file + "`").collect(Collectors.joining(", "));
    }

    private static String labelFor(GeneralMetric metric) {
        return switch (metric) {
            case FILES -> "|       Файл(-ы)        | ";
            case FIRST_DATE -> "|    Начальная дата     | ";
            case LAST_DATE -> "|     Конечная дата     | ";
            case TOTAL_REQUESTS -> "|  Количество запросов  | ";
            case UNIQUE_PROTOCOLS -> "| Кол-во уникальных протоколов | ";
            case RESPONSE_SIZE_AVERAGE -> "| Средний размер ответа | ";
            case RESPONSE_SIZE_MAX -> "|  Максимальный ответ   | ";
            case RESPONSE_SIZE_P95 -> "|  95p размера ответа   | ";
        };
    }
}
