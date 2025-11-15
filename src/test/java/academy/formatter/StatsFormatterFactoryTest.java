package academy.formatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

import academy.format.OutputFormat;
import org.junit.jupiter.api.Test;

class StatsFormatterFactoryTest {

    private final StatsFormatterFactory factory = new StatsFormatterFactory();

    @Test
    void GivenJsonFormat_WhenCreate_ThenReturnsJsonFormatter() {
        StatsFormatter formatter = factory.create(OutputFormat.JSON);

        assertEquals(JsonStatsFormatter.class, formatter.getClass());
    }

    @Test
    void GivenMarkdownFormat_WhenCreate_ThenReturnsMarkdownFormatter() {
        StatsFormatter formatter = factory.create(OutputFormat.MARKDOWN);

        assertEquals(MarkdownStatsFormatter.class, formatter.getClass());
    }

    @Test
    void GivenAdocFormat_WhenCreate_ThenReturnsAdocFormatter() {
        StatsFormatter formatter = factory.create(OutputFormat.ADOC);

        assertEquals(AdocStatsFormatter.class, formatter.getClass());
    }
}
