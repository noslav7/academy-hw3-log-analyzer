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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "log-analyzer",
        version = "1.0.0",
        mixinStandardHelpOptions = true,
        description = "Analyze NGINX access logs and produce aggregated statistics.")
public class Application implements java.util.concurrent.Callable<Integer> {

    private static final Logger LOGGER = LogManager.getLogger(Application.class);

    @Option(names = {"-p", "--path"}, required = true, arity = "1..*", description = "One or many log file paths")
    private List<String> inputPaths = new ArrayList<>();

    @Option(
            names = {"-f", "--format"},
            required = true,
            description = "Output format: json, markdown, adoc")
    private String formatOption;

    @Option(
            names = {"-o", "--output"},
            required = true,
            description = "Path to output file (must not exist)")
    private String outputPathOption;

    @Option(names = "--from", description = "Filter logs from ISO-8601 date (inclusive)")
    private String fromOption;

    @Option(names = "--to", description = "Filter logs to ISO-8601 date (inclusive)")
    private String toOption;

    private static final String UNDEFINED_PARAMETER = "undefined";

    private final DateRangeFactory dateRangeFactory;
    private final OutputFilePreparer outputFilePreparer;
    private final LogAnalyzer logAnalyzer;

    public Application() {
        this(new DateRangeFactory(), new OutputFilePreparer(), createDefaultLogAnalyzer());
    }

    Application(
            DateRangeFactory dateRangeFactory,
            OutputFilePreparer outputFilePreparer,
            LogAnalyzer logAnalyzer) {
        this.dateRangeFactory = dateRangeFactory;
        this.outputFilePreparer = outputFilePreparer;
        this.logAnalyzer = logAnalyzer;
    }

    public static void main(String[] args) {
        // Логирование входных параметров для проверки работоспособности black-box тестов
        debugArgs(Arrays.asList(args));

        // Запуск программы
        int exitCode = new CommandLine(new Application()).execute(args);
        System.exit(exitCode);
    }

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

    private void runApplication() throws IOException {
        OutputFormat outputFormat = OutputFormat.from(formatOption);
        Path outputPath = outputFilePreparer.prepare(outputPathOption, outputFormat);
        DateRange dateRange = dateRangeFactory.create(fromOption, toOption);

        LogAnalysisRequest request =
                new LogAnalysisRequest(inputPaths, outputFormat, outputPath, dateRange);

        logAnalyzer.analyze(request);
    }

    private static LogAnalyzer createDefaultLogAnalyzer() {
        HttpClient httpClient = HttpClient.newBuilder().build();
        InputSourceResolver resolver = new InputSourceResolver(httpClient);
        LogProcessingService processingService = new LogProcessingService(new LogEntryParser());
        StatsFormatterFactory formatterFactory = new StatsFormatterFactory();
        OutputFileWriter outputFileWriter = new OutputFileWriter();
        return new LogAnalyzer(resolver, processingService, formatterFactory, outputFileWriter);
    }

    // Note: нужно только для отладки, удалить в случае ненадобности
    @Deprecated(forRemoval = true)
    private static void debugArgs(List<String> args) {
        var argsPerParam = getArgumentsPerParameter(args);
        System.out.printf("Входные параметры программы: %s%n", argsPerParam);

        logPaths("Пути к лог-файлам", argsPerParam, "p", "path");
        logPaths("Пути к отчетам", argsPerParam, "o", "output");
    }

    private static Map<String, List<String>> getArgumentsPerParameter(List<String> args) {
        var argsPerParameter = new HashMap<String, List<String>>();
        argsPerParameter.put(UNDEFINED_PARAMETER, new ArrayList<>());

        var queue = new ArrayDeque<>(args);
        String currentParameter = null;
        while (!queue.isEmpty()) {
            var element = queue.removeFirst();
            if (element.startsWith("-")) {
                currentParameter = element.startsWith("--") ? element.substring(2) : element.substring(1);
                argsPerParameter.putIfAbsent(currentParameter, new ArrayList<>());
            } else {
                argsPerParameter
                        .get(Optional.ofNullable(currentParameter).orElse(UNDEFINED_PARAMETER))
                        .add(element);
            }
        }

        return argsPerParameter;
    }

    private static void logPaths(String description, Map<String, List<String>> argsPerParam, String... params) {
        var paths = new ArrayList<String>();
        for (var param : params) {
            paths.addAll(argsPerParam.getOrDefault(param, List.of()));
        }
        System.out.printf(
                "%s: %s%n",
                description,
                paths.stream()
                        .map(it -> it.contains("*")
                                ? "glob: " + it
                                : "path: %s, exists: %s".formatted(it, Files.exists(Path.of(it))))
                        .collect(Collectors.joining(";")));
    }
}
