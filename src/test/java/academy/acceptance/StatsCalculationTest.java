package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.support.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class StatsCalculationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Расчет статистики на основании нескольких log-файлов")
    void happyPathTest(@TempDir Path tempDir) throws IOException {
        Path part1 = TestUtils.copySampleLog(tempDir, "part1.txt");
        Path part2 = TestUtils.copySampleLog(tempDir, "part2.txt");
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute(
                        "--path",
                        part1.toString(),
                        part2.toString(),
                        "--format",
                        "json",
                        "--output",
                        output.toString());

        assertThat(exitCode).isEqualTo(0);

        JsonNode actual = OBJECT_MAPPER.readTree(output.toFile());
        JsonNode expected = OBJECT_MAPPER.readTree(
                Path.of("scripts", "data", "output", "expected.json").toFile());

        assertThat(actual.get("files")).isEqualTo(expected.get("files"));
        assertThat(actual.get("totalRequestsCount")).isEqualTo(expected.get("totalRequestsCount"));
        assertThat(actual.get("responseSizeInBytes")).isEqualTo(expected.get("responseSizeInBytes"));
        assertThat(actual.get("resources")).isEqualTo(expected.get("resources"));
        assertThat(actual.get("responseCodes")).isEqualTo(expected.get("responseCodes"));
        assertThat(actual.get("requestsPerDate")).isEqualTo(expected.get("requestsPerDate"));
        assertThat(actual.get("uniqueProtocolsCount")).isEqualTo(expected.get("uniqueProtocolsCount"));
        assertThat(actual.get("uniqueProtocols")).isEqualTo(expected.get("uniqueProtocols"));
    }
}
