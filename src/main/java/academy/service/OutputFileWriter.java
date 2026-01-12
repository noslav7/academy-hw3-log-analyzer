package academy.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Выполняет запись готового отчёта на диск. */
public class OutputFileWriter {

    /**
     * Создаёт новый файл и записывает в него содержимое отчёта.
     *
     * @param path путь к выходному файлу
     * @param content сформированная статистика
     */
    public void write(Path path, String content) throws IOException {
        Files.writeString(path, content, StandardOpenOption.CREATE_NEW);
    }
}
