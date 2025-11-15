package academy.support;

import academy.Application;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import org.assertj.core.api.Assertions;
import picocli.CommandLine;

public final class TestUtils {

    public static final String SAMPLE_LOG_LINE =
            "93.180.71.3 - - [17/May/2015:08:05:32 +0000] \"GET /downloads/product_1 HTTP/1.1\" 304 0 \"-\" \"Debian\"";

    private TestUtils() {}

    public static CommandLine newCommandLine() {
        CommandLine commandLine = new CommandLine(new Application());
        commandLine.setTrimQuotes(true);
        return commandLine;
    }

    public static Path createLogFile(Path directory, String fileName, List<String> lines) {
        Path target = directory.resolve(fileName);
        try {
            Files.write(target, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return target;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to create log file in tests", ex);
        }
    }

    public static Path copySampleLog(Path tempDir, String fileName) {
        Path source = Path.of("scripts", "data", "input", "logs", fileName);
        Path target = tempDir.resolve(fileName);
        try {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Assertions.fail("Failed to copy sample log " + fileName, ex);
        }
        return target;
    }
}
