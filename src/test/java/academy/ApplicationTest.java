package academy;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import academy.support.TestUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ApplicationTest {

    @Test
    @DisplayName("Базовая проверка работоспособности программы")
    void happyPathTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.copySampleLog(tempDir, "part1.txt");
        Path output = tempDir.resolve("report.md");

        int exitCode = TestUtils.newCommandLine()
                .execute(
                        "--path",
                        log.toString(),
                        "--format",
                        "markdown",
                        "--output",
                        output.toString(),
                        "--from",
                        "2015-05-17");

        assertAll(
                () -> assertEquals(0, exitCode, "exit code"),
                () -> assertTrue(Files.exists(output), "output file exists"),
                () -> assertTrue(
                        Files.readString(output).contains("#### Общая информация"),
                        "report contains expected header"));
    }
}
