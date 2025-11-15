package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.support.TestUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class LogFileParsingTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @DisplayName("Парсинг локального файла с валидными строками")
    void localFileProcessingTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.createLogFile(
                tempDir,
                "access.log",
                List.of(
                        TestUtils.SAMPLE_LOG_LINE,
                        "93.180.71.3 - - [18/May/2015:08:05:32 +0000] \"GET /downloads/product_2 HTTP/1.1\" 200 123 \"-\" \"Debian\""));
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        JsonNode root = OBJECT_MAPPER.readTree(output.toFile());
        assertThat(root.get("totalRequestsCount").asInt()).isEqualTo(2);
        assertThat(root.get("responseCodes")).hasSize(2);
        assertThat(root.get("files").get(0).asText()).isEqualTo("access.log");
    }

    @Test
    @DisplayName("Парсинг удаленного файла")
    void remoteFileProcessingTest(@TempDir Path tempDir) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.createContext("/logs/nginx.log", exchange -> {
            byte[] data = (TestUtils.SAMPLE_LOG_LINE + System.lineSeparator()
                            + "93.180.71.3 - - [17/May/2015:08:05:33 +0000] \"GET /downloads/product_2 HTTP/1.1\" 404 10 \"-\" \"Debian\"")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        });
        server.setExecutor(executor);
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/logs/nginx.log";
            Path output = tempDir.resolve("report.json");

            int exitCode = TestUtils.newCommandLine()
                    .execute("--path", url, "--format", "json", "--output", output.toString());

            assertThat(exitCode).isEqualTo(0);
            JsonNode root = OBJECT_MAPPER.readTree(output.toFile());
            assertThat(root.get("totalRequestsCount").asInt()).isEqualTo(2);
            assertThat(root.get("files").get(0).asText()).isEqualTo(url);
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    @Test
    @DisplayName("Фильтрация строк по параметрам --from и --to")
    void localFileProcessingAndFilteringTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.createLogFile(
                tempDir,
                "access.log",
                List.of(
                        TestUtils.SAMPLE_LOG_LINE,
                        "93.180.71.3 - - [18/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 200 100 \"-\" \"Debian\"",
                        "93.180.71.3 - - [19/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 404 100 \"-\" \"Debian\""));
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute(
                        "--path",
                        log.toString(),
                        "--format",
                        "json",
                        "--output",
                        output.toString(),
                        "--from",
                        "2015-05-18",
                        "--to",
                        "2015-05-19");

        assertThat(exitCode).isEqualTo(0);
        JsonNode root = OBJECT_MAPPER.readTree(output.toFile());
        assertThat(root.get("totalRequestsCount").asInt()).isEqualTo(2);
        assertThat(root.get("requestsPerDate")).hasSize(2);
    }

    @Test
    @DisplayName("Некорректные строки логов пропускаются")
    void damagedLocalFileProcessingTest(@TempDir Path tempDir) throws IOException {
        Path log = TestUtils.createLogFile(
                tempDir,
                "access.log",
                List.of(
                        "INVALID LINE",
                        TestUtils.SAMPLE_LOG_LINE,
                        "93.180.71.3 - - [bad-date] \"GET /downloads/product_2 HTTP/1.1\" 200 1 \"-\" \"Debian\""));
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(0);
        JsonNode root = OBJECT_MAPPER.readTree(output.toFile());
        assertThat(root.get("totalRequestsCount").asInt()).isEqualTo(1);
        assertThat(root.get("responseCodes").get(0).get("code").asInt()).isEqualTo(304);
    }
}
