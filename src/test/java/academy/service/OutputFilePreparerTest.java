package academy.service;

import academy.exception.InvalidArgumentsException;
import academy.format.OutputFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutputFilePreparerTest {

    private final OutputFilePreparer preparer = new OutputFilePreparer();

    @TempDir
    Path tempDir;

    @Test
    void GivenValidOutput_WhenPrepare_ThenCreatesParentAndReturnsPath() throws IOException {
        Path target = tempDir.resolve("reports").resolve("result.json");

        Path prepared = preparer.prepare(target.toString(), OutputFormat.JSON);

        assertEquals(target.toAbsolutePath().normalize(), prepared);
        assertTrue(Files.exists(prepared.getParent()));
    }

    @Test
    void GivenExistingFile_WhenPrepare_ThenThrowsInvalidArgumentsException() throws IOException {
        Path target = tempDir.resolve("existing.json");
        Files.createFile(target);

        assertThrows(
                InvalidArgumentsException.class,
                () -> preparer.prepare(target.toString(), OutputFormat.JSON));
    }

    @Test
    void GivenMismatchedExtension_WhenPrepare_ThenThrowsInvalidArgumentsException() {
        Path target = tempDir.resolve("report.md");

        assertThrows(
                InvalidArgumentsException.class,
                () -> preparer.prepare(target.toString(), OutputFormat.JSON));
    }
}


