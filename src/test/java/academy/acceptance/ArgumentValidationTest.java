package academy.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import academy.support.TestUtils;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

public class ArgumentValidationTest {

    @TempDir
    Path tempRoot;

    @Test
    @DisplayName("На вход передан несуществующий локальный файл")
    void test1() {
        Path tempDir = freshTempDir();
        Path missing = tempDir.resolve("missing.log");
        Path output = tempDir.resolve("result.json");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", missing.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    @Test
    @DisplayName("На вход передан несуществующий удаленный файл")
    void test2() throws IOException {
        Path tempDir = freshTempDir();
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        server.createContext("/logs/missing.log", exchange -> {
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
        });
        server.setExecutor(executor);
        server.start();

        try {
            String url = "http://localhost:" + server.getAddress().getPort() + "/logs/missing.log";
            Path output = tempDir.resolve("result.json");

            int exitCode = TestUtils.newCommandLine()
                    .execute("--path", url, "--format", "json", "--output", output.toString());

            assertThat(exitCode).isEqualTo(2);
            assertThat(Files.exists(output)).isFalse();
        } finally {
            server.stop(0);
            executor.shutdownNow();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ".docx")
    @DisplayName("На вход передан файл в неподдерживаемом формате")
    void test3(String extension) throws IOException {
        Path tempDir = freshTempDir();
        Path file = tempDir.resolve("data" + extension);
        Files.writeString(file, TestUtils.SAMPLE_LOG_LINE);
        Path output = tempDir.resolve("result.json");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", file.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"2025.01.01 10:30", "today"})
    @DisplayName("На вход переданы невалидные параметры --from / --to - {0}")
    void test4(String from) {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("result.json");

        CommandLine commandLine = TestUtils.newCommandLine();
        List<String> args = new ArrayList<>(
                List.of("--path", log.toString(), "--format", "json", "--output", output.toString()));

        if (from == null) {
            args.add("--from");
        } else if (from.isEmpty()) {
            args.add("--from");
            args.add("");
        } else {
            args.add("--from");
            args.add(from);
        }

        int exitCode = commandLine.execute(args.toArray(String[]::new));

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = "txt")
    @DisplayName("Результаты запрошены в неподдерживаемом формате {0}")
    void test5(String format) {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("result." + format);

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", format, "--output", output.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    @ParameterizedTest
    @MethodSource("test6ArgumentsSource")
    @DisplayName("По пути в аргументе --output указан файл с некоректным расширением")
    void test6(String format, String output) {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path outputPath = tempDir.resolve(output);

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", format, "--output", outputPath.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(outputPath)).isFalse();
    }

    @Test
    @DisplayName("По пути в аргументе --output уже существует файл")
    void test7() throws IOException {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("report.json");
        Files.writeString(output, "{}");

        int exitCode = TestUtils.newCommandLine()
                .execute("--path", log.toString(), "--format", "json", "--output", output.toString());

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.readString(output)).isEqualTo("{}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"--path", "--output", "--format", "-p", "-o", "-f"})
    @DisplayName("На вход не передан обязательный параметр \"{0}\"")
    void test8(String argument) {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("report.json");

        List<String> args = new ArrayList<>();

        if (!argument.equals("--path") && !argument.equals("-p")) {
            args.add("--path");
            args.add(log.toString());
        }
        if (!argument.equals("--format") && !argument.equals("-f")) {
            args.add("--format");
            args.add("json");
        }
        if (!argument.equals("--output") && !argument.equals("-o")) {
            args.add("--output");
            args.add(output.toString());
        }

        int exitCode = TestUtils.newCommandLine().execute(args.toArray(String[]::new));
        assertThat(exitCode).isEqualTo(2);
    }

    @ParameterizedTest
    @ValueSource(strings = {"--input", "--filter"})
    @DisplayName("На вход передан неподдерживаемый параметр \"{0}\"")
    void test9(String argument) {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
        Path output = tempDir.resolve("report.json");

        int exitCode = TestUtils.newCommandLine()
                .execute(
                        "--path", log.toString(), "--format", "json", "--output", output.toString(), argument, "value");

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    @Test
    @DisplayName("Значение параметра --from больше, чем значение параметра --to")
    void test10() {
        Path tempDir = freshTempDir();
        Path log = TestUtils.createLogFile(tempDir, "access.log", List.of(TestUtils.SAMPLE_LOG_LINE));
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
                        "2025-01-02",
                        "--to",
                        "2025-01-01");

        assertThat(exitCode).isEqualTo(2);
        assertThat(Files.exists(output)).isFalse();
    }

    private static Stream<Arguments> test6ArgumentsSource() {
        return Stream.of(
                Arguments.of("markdown", "./results.txt"),
                Arguments.of("json", "./results.md"),
                Arguments.of("adoc", "./results.ad1"));
    }

    private Path freshTempDir() {
        try {
            return Files.createTempDirectory(tempRoot, "case-");
        } catch (IOException ex) {
            throw new RuntimeException("Failed to create temp directory for test", ex);
        }
    }
}
