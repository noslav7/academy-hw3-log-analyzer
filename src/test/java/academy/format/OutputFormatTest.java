package academy.format;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import academy.exception.InvalidArgumentsException;
import org.junit.jupiter.api.Test;

class OutputFormatTest {

    @Test
    void GivenSupportedFormatName_WhenFrom_ThenReturnsExpectedFormat() {
        assertAll(
                () -> assertEquals(OutputFormat.JSON, OutputFormat.from("json")),
                () -> assertEquals(OutputFormat.MARKDOWN, OutputFormat.from("MARKDOWN")),
                () -> assertEquals(OutputFormat.ADOC, OutputFormat.from("AdOc")));
    }

    @Test
    void GivenUnsupportedValue_WhenFrom_ThenThrowsInvalidArgumentsException() {
        assertThrows(InvalidArgumentsException.class, () -> OutputFormat.from("xml"));
    }
}
