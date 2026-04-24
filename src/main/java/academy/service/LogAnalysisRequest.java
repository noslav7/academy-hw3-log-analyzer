package academy.service;

import academy.format.OutputFormat;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Неизменяемый набор параметров, описывающих запуск анализа логов.
 *
 * @param inputPaths пути к файлам/URL
 * @param outputFormat требуемый формат отчёта
 * @param outputPath путь к выходному файлу
 * @param dateRange диапазон дат для фильтрации логов
 */
public record LogAnalysisRequest(
        List<String> inputPaths, OutputFormat outputFormat, Path outputPath, DateRange dateRange) {

    /** Валидирует и нормализует входные параметры запроса анализа. */
    public LogAnalysisRequest {
        Objects.requireNonNull(inputPaths, "inputPaths must not be null");
        Objects.requireNonNull(outputFormat, "outputFormat must not be null");
        Objects.requireNonNull(outputPath, "outputPath must not be null");
        Objects.requireNonNull(dateRange, "dateRange must not be null");
        inputPaths = List.copyOf(inputPaths);
    }
}
