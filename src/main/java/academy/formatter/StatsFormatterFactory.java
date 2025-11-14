package academy.formatter;

import academy.format.OutputFormat;

public class StatsFormatterFactory {

    public StatsFormatter create(OutputFormat format) {
        return switch (format) {
            case JSON -> new JsonStatsFormatter();
            case MARKDOWN -> new MarkdownStatsFormatter();
            case ADOC -> new AdocStatsFormatter();
        };
    }
}


