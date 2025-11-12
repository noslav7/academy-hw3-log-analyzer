package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.support.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StatsReportTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Сохранение статистики в формате JSON")
    void jsonTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.copySampleLog(tempDir, "part1.txt");
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        JsonNode node = OBJECT_MAPPER.readTree(output.toFile());
        assertThat(node.has("files")).isTrue();
        assertThat(node.get("files").get(0).asText()).isEqualTo("part1.txt");
    }

    @Test
    @DisplayName("Сохранение статистики в формате MARKDOWN")
    void markdownTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("report.md");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "markdown", "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        String content = Files.readString(output);
        assertThat(content).contains("#### Общая информация");
        assertThat(content).contains("`access.log`");
    }

    @Test
    @DisplayName("Сохранение статистики в формате ADOC не поддерживается")
    void adocTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("report.ad");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "adoc", "--output", output.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }
}
