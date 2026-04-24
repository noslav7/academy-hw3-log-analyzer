package academy.service;

import academy.formatter.StatsFormatter;
import academy.formatter.StatsFormatterFactory;
import academy.input.InputSourceResolver;
import academy.input.ResolvedLogSource;
import academy.stats.StatsCollector;
import academy.stats.StatsResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Входная точка бизнес-логики: объединяет источники логов, обработку и запись результатов. */
public class LogAnalyzer {

    /** Логгер основного пайплайна анализа. */
    private static final Logger LOGGER = LogManager.getLogger(LogAnalyzer.class);

    /** Компонент разрешения входных путей в реальные источники логов. */
    private final InputSourceResolver inputSourceResolver;
    /** Сервис построчной обработки логов. */
    private final LogProcessingService logProcessingService;
    /** Фабрика форматтеров итогового отчёта. */
    private final StatsFormatterFactory statsFormatterFactory;
    /** Компонент физической записи отчёта в файл. */
    private final OutputFileWriter outputFileWriter;

    /** Создаёт анализатор с явно переданными зависимостями. */
    public LogAnalyzer(
            InputSourceResolver inputSourceResolver,
            LogProcessingService logProcessingService,
            StatsFormatterFactory statsFormatterFactory,
            OutputFileWriter outputFileWriter) {
        this.inputSourceResolver = inputSourceResolver;
        this.logProcessingService = logProcessingService;
        this.statsFormatterFactory = statsFormatterFactory;
        this.outputFileWriter = outputFileWriter;
    }

    /**
     * Загружает указанные логи, вычисляет статистику и сохраняет результат в файл.
     *
     * @param request параметры анализа (пути, формат, диапазон дат)
     */
    public void analyze(LogAnalysisRequest request) throws IOException {
        List<ResolvedLogSource> sources = inputSourceResolver.resolve(request.inputPaths());

        StatsCollector collector = new StatsCollector();
        List<String> processedFiles = new ArrayList<>();

        for (ResolvedLogSource source : sources) {
            try (BufferedReader reader = source.opener().open()) {
                logProcessingService.process(reader, source.displayName(), request.dateRange(), collector);
                processedFiles.add(source.displayName());
            }
        }

        StatsResult statsResult = collector.buildResult(processedFiles);
        StatsFormatter formatter = statsFormatterFactory.create(request.outputFormat());
        String content = formatter.format(statsResult);

        outputFileWriter.write(request.outputPath(), content);
        LOGGER.info("Statistics successfully written to {}", request.outputPath());
    }
}
