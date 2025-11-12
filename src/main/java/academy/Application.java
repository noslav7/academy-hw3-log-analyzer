package academy;

import academy.exception.InvalidArgumentsException;
import academy.format.OutputFormat;
import academy.formatter.JsonStatsFormatter;
import academy.formatter.MarkdownStatsFormatter;
import academy.formatter.StatsFormatter;
import academy.input.InputSourceResolver;
import academy.input.ResolvedLogSource;
import academy.log.LogEntryParser;
import academy.service.DateRange;
import academy.service.LogProcessingService;
import academy.stats.StatsCollector;
import academy.stats.StatsResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
            description = "Output format: json, markdown")
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
        Path outputPath = validateAndPrepareOutputPath(outputFormat);
        LocalDate fromDate = parseDate(fromOption, "--from");
        LocalDate toDate = parseDate(toOption, "--to");
        validateDateRange(fromDate, toDate);

        DateRange dateRange = new DateRange(fromDate, toDate);
        HttpClient httpClient = HttpClient.newBuilder().build();
        InputSourceResolver resolver = new InputSourceResolver(httpClient);
        List<ResolvedLogSource> sources = resolver.resolve(inputPaths);

        StatsCollector collector = new StatsCollector();
        LogProcessingService processingService = new LogProcessingService(new LogEntryParser());
        List<String> processedFiles = new ArrayList<>();

        for (ResolvedLogSource source : sources) {
            try (BufferedReader reader = source.opener().open()) {
                processingService.process(reader, source.displayName(), dateRange, collector);
                processedFiles.add(source.displayName());
            }
        }

        StatsResult statsResult = collector.buildResult(processedFiles);
        StatsFormatter formatter = selectFormatter(outputFormat);
        String content = formatter.format(statsResult);

        Files.writeString(outputPath, content, StandardOpenOption.CREATE_NEW);
        LOGGER.info("Statistics successfully written to {}", outputPath);
    }

    private Path validateAndPrepareOutputPath(OutputFormat format) throws IOException {
        Path path = Paths.get(outputPathOption).toAbsolutePath().normalize();

        if (Files.exists(path)) {
            throw new InvalidArgumentsException("Output file already exists: " + path);
        }

        String extension = extractExtension(path.getFileName().toString());
        if (!format.getFileExtension().equalsIgnoreCase("." + extension)) {
            throw new InvalidArgumentsException(
                    "Output file extension does not match format. Expected "
                            + format.getFileExtension());
        }

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (parent != null && !Files.isWritable(parent)) {
            throw new InvalidArgumentsException("Output directory is not writable: " + parent);
        }

        return path;
    }

    private static String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private static StatsFormatter selectFormatter(OutputFormat format) {
        return switch (format) {
            case JSON -> new JsonStatsFormatter();
            case MARKDOWN -> new MarkdownStatsFormatter();
        };
    }

    private static LocalDate parseDate(String value, String optionName) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new InvalidArgumentsException(
                    "Invalid value for " + optionName + ": value must not be blank");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new InvalidArgumentsException(
                    "Invalid value for " + optionName + ": " + value + ". Expected ISO-8601 date (yyyy-MM-dd)", ex);
        }
    }

    private static void validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidArgumentsException("--from must be before or equal to --to");
        }
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
