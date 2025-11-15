package academy.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OutputFileWriterTest {

    private final OutputFileWriter writer = new OutputFileWriter();

    @TempDir
    Path tempDir;

    @Test
    void GivenNonExistingFile_WhenWrite_ThenCreatesFileWithContent() throws IOException {
        Path target = tempDir.resolve("output.txt");

        writer.write(target, "sample content");

        assertEquals("sample content", Files.readString(target));
    }

    @Test
    void GivenExistingFile_WhenWrite_ThenThrowsFileAlreadyExistsException() throws IOException {
        Path target = tempDir.resolve("output.txt");
        Files.writeString(target, "existing");

        assertThrows(FileAlreadyExistsException.class, () -> writer.write(target, "new content"));
    }
}
