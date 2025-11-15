package academy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OutputFileWriter {

    public void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
    }
}
