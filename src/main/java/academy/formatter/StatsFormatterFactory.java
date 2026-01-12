package academy.formatter;

import academy.format.OutputFormat;

/** Создаёт конкретные реализации форматтера статистики в зависимости от выбранного пользователем формата вывода. */
public class StatsFormatterFactory {

    /**
     * Возвращает форматтер, способный сформировать отчёт в указанном формате.
     *
     * @param format формат выхода, выбранный через CLI
     * @return экземпляр форматтера
     */
    public StatsFormatter create(OutputFormat format) {
        return switch (format) {
            case JSON -> new JsonStatsFormatter();
            case MARKDOWN -> new MarkdownStatsFormatter();
            case ADOC -> new AdocStatsFormatter();
        };
    }
}
