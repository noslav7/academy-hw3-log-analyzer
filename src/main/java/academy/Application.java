package academy;

import academy.exception.InvalidArgumentsException;
import academy.format.OutputFormat;
import academy.formatter.StatsFormatterFactory;
import academy.input.InputSourceResolver;
import academy.log.LogEntryParser;
import academy.service.DateRange;
import academy.service.DateRangeFactory;
import academy.service.LogAnalysisRequest;
import academy.service.LogAnalyzer;
import academy.service.LogProcessingService;
import academy.service.OutputFilePreparer;
import academy.service.OutputFileWriter;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/** CLI-приложение для агрегации статистики по NGINX логам. */
@Command(
        name = "log-analyzer",
        version = "1.0.0",
        mixinStandardHelpOptions = true,
        description = "Analyze NGINX access logs and produce aggregated statistics.")
public class Application implements java.util.concurrent.Callable<Integer> {

    /** Логгер CLI-приложения. */
    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    @Option(
            names = {"-p", "--path"},
            required = true,
            arity = "1..*",
            description = "One or many log file paths")
    /** Список путей или URL источников логов, переданных пользователем. */
    private List<String> inputPaths = new ArrayList<>();

    @Option(
            names = {"-f", "--format"},
            required = true,
            description = "Output format: json, markdown, adoc")
    /** Строковое значение формата отчёта, указанное в CLI. */
    private String formatOption;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Path to output file (must not exist)")
    /** Путь до выходного файла, введённый пользователем. */
    private String outputPathOption;

    @Option(names = "--from", description = "Filter logs from ISO-8601 date (inclusive)")
    /** Нижняя граница фильтрации дат (включительно). */
    private String fromOption;

    @Option(names = "--to", description = "Filter logs to ISO-8601 date (inclusive)")
    /** Верхняя граница фильтрации дат (включительно). */
    private String toOption;

    /** Фабрика построения диапазона дат на основе CLI-аргументов. */
    private final DateRangeFactory dateRangeFactory;
    /** Компонент валидации и подготовки выходного пути. */
    private final OutputFilePreparer outputFilePreparer;
    /** Фасад, выполняющий полный цикл анализа логов. */
    private final LogAnalyzer logAnalyzer;

    /** Создаёт приложение с production-зависимостями по умолчанию. */
    public Application() {
        this(new DateRangeFactory(), new OutputFilePreparer(), createDefaultLogAnalyzer());
    }

    /** Позволяет внедрять зависимости вручную (например, в тестах). */
    Application(DateRangeFactory dateRangeFactory, OutputFilePreparer outputFilePreparer, LogAnalyzer logAnalyzer) {
        this.dateRangeFactory = dateRangeFactory;
        this.outputFilePreparer = outputFilePreparer;
        this.logAnalyzer = logAnalyzer;
    }

    /** Точка входа CLI-приложения. */
    public static void main(String[] args) {
        int exitCode = new CommandLine(new Application()).execute(sanitizeArgs(args));
        System.exit(exitCode);
    }

    /** Запускает приложение и возвращает код завершения процесса. */
    @Override
    public Integer call() {
        try {
            runApplication();
            return 0;
        } catch (InvalidArgumentsException ex) {
            LOGGER.error(ex.getMessage());
            return 2;
        } catch (Exception ex) {
            LOGGER.error("Unexpected error during execution", ex);
            return 1;
        }
    }

    /** Валидирует полученные аргументы, подготавливает запрос на анализ и запускает обработку логов. */
    private void runApplication() throws IOException {
        OutputFormat outputFormat = OutputFormat.from(formatOption);
        Path outputPath = outputFilePreparer.prepare(outputPathOption, outputFormat);
        DateRange dateRange = dateRangeFactory.create(fromOption, toOption);

        LogAnalysisRequest request = new LogAnalysisRequest(inputPaths, outputFormat, outputPath, dateRange);

        logAnalyzer.analyze(request);
    }

    /** Собирает набор зависимостей, используемый приложением по умолчанию при запуске из CLI. */
    private static LogAnalyzer createDefaultLogAnalyzer() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        InputSourceResolver resolver = new InputSourceResolver(httpClient);
        LogProcessingService processingService = new LogProcessingService(new LogEntryParser());
        StatsFormatterFactory formatterFactory = new StatsFormatterFactory();
        OutputFileWriter outputFileWriter = new OutputFileWriter();
        return new LogAnalyzer(resolver, processingService, formatterFactory, outputFileWriter);
    }

    /**
     * Удаляет пустые и некорректные аргументы до первого CLI-флага, чтобы Picocli не падал на невалидном вводе.
     *
     * @param args аргументы, переданные в {@link #main(String[])}
     * @return очищенный массив аргументов
     */
    static String[] sanitizeArgs(String[] args) {
        if (args == null || args.length == 0) {
            return args;
        }

        List<String> sanitized = new ArrayList<>(args.length);
        boolean optionEncountered = false;

        for (String rawArg : args) {
            if (rawArg == null) {
                continue;
            }

            String trimmed = rawArg.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            if (!optionEncountered) {
                if (trimmed.startsWith("-") || trimmed.startsWith("@")) {
                    optionEncountered = true;
                    sanitized.add(trimmed);
                }
                continue;
            }

            sanitized.add(trimmed);
        }

        if (!optionEncountered) {
            return Arrays.stream(args)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toArray(String[]::new);
        }

        return sanitized.toArray(String[]::new);
    }
}
