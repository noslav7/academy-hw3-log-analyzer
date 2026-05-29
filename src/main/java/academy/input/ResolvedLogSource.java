package academy.input;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Описывает один источник логов и способ получить {@link BufferedReader} для него.
 *
 * @param displayName человекочитаемое название (имя файла, URL и т.д.)
 * @param opener лямбда, предоставляющая поток чтения
 */
public record ResolvedLogSource(String displayName, SourceOpener opener) {

    @FunctionalInterface
    public interface SourceOpener {
        /** Открывает источник логов и возвращает подготовленный {@link BufferedReader}. */
        BufferedReader open() throws IOException;
    }
}
