package academy.input;

import academy.exception.InvalidArgumentsException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Преобразует пользовательские пути и URL в набор открываемых источников логов. */
public class InputSourceResolver {

    /** Разрешённые расширения лог-файлов. */
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("log", "txt");
    /** Параметры обхода файловой системы при обработке glob-паттернов. */
    private static final Set<FileVisitOption> FILE_VISIT_OPTIONS = EnumSet.of(FileVisitOption.FOLLOW_LINKS);

    /** HTTP-клиент для чтения удалённых логов по URL. */
    private final HttpClient httpClient;

    /** Создаёт резолвер источников на основе переданного HTTP-клиента. */
    public InputSourceResolver(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Анализирует список входных путей и возвращает соответствующие им источники чтения.
     *
     * @param inputs пути/URL/глоб-паттерны, переданные через CLI
     * @return список абстракций, позволяющих открыть каждый файл
     */
    public List<ResolvedLogSource> resolve(List<String> inputs) {
        if (inputs == null || inputs.isEmpty()) {
            throw new InvalidArgumentsException("No input files were provided");
        }

        List<ResolvedLogSource> resolvedSources = new ArrayList<>();

        for (String rawInput : inputs) {
            String input = normalizeInput(rawInput);
            if (isRemote(input)) {
                resolvedSources.add(resolveRemote(input));
            } else {
                resolvedSources.addAll(resolveLocal(input));
            }
        }

        if (resolvedSources.isEmpty()) {
            throw new InvalidArgumentsException("No log files matched provided paths");
        }

        return resolvedSources;
    }

    /** Разрешает удалённый путь и готовит ленивый opener для загрузки файла. */
    private ResolvedLogSource resolveRemote(String input) {
        URI uri;
        try {
            uri = new URI(input);
        } catch (URISyntaxException ex) {
            throw new InvalidArgumentsException("Invalid URL: " + input, ex);
        }

        validateRemoteExtension(uri);

        return new ResolvedLogSource(uri.toString(), () -> openRemote(uri));
    }

    /** Разрешает локальный путь: одиночный файл или glob-шаблон. */
    private List<ResolvedLogSource> resolveLocal(String input) {
        Path path = Path.of(input);
        if (!containsGlob(input)) {
            return List.of(resolveConcreteLocal(path));
        }

        Path baseDir = determineBaseDirectory(path);
        if (!Files.exists(baseDir)) {
            throw new InvalidArgumentsException("Base directory for glob does not exist: " + baseDir);
        }
        Path resolvedPattern = resolveAgainstBase(path, baseDir);
        Path relativePattern = baseDir.relativize(resolvedPattern);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + relativePattern.toString());
        int maxDepth = containsRecursiveWildcard(relativePattern)
                ? Integer.MAX_VALUE
                : Math.max(1, relativePattern.getNameCount());

        FileVisitOption[] visitOptions = FILE_VISIT_OPTIONS.toArray(FileVisitOption[]::new);
        try (Stream<Path> pathStream = Files.walk(baseDir, maxDepth, visitOptions)) {
            List<Path> matched = pathStream
                    .filter(Files::isRegularFile)
                    .filter(candidate -> matcher.matches(baseDir.relativize(candidate)))
                    .sorted()
                    .collect(Collectors.toList());

            if (matched.isEmpty()) {
                throw new InvalidArgumentsException("No files matched pattern: " + input);
            }

            List<ResolvedLogSource> sources = new ArrayList<>(matched.size());
            for (Path matchedPath : matched) {
                validateLocalExtension(matchedPath);
                sources.add(new ResolvedLogSource(
                        fileName(matchedPath), () -> Files.newBufferedReader(matchedPath, StandardCharsets.UTF_8)));
            }
            return sources;
        } catch (IOException ex) {
            throw new InvalidArgumentsException("Failed to resolve glob pattern: " + input, ex);
        }
    }

    /** Проверяет, что путь указывает на существующий локальный файл поддерживаемого формата. */
    private ResolvedLogSource resolveConcreteLocal(Path path) {
        if (!Files.exists(path)) {
            throw new InvalidArgumentsException("File not found: " + path);
        }
        if (!Files.isRegularFile(path)) {
            throw new InvalidArgumentsException("Path does not point to a regular file: " + path);
        }
        validateLocalExtension(path);
        Path normalized = path.toAbsolutePath().normalize();
        return new ResolvedLogSource(
                fileName(normalized), () -> Files.newBufferedReader(normalized, StandardCharsets.UTF_8));
    }

    /** Выполняет HTTP-запрос и открывает поток чтения удалённого лога. */
    private BufferedReader openRemote(URI uri) throws IOException {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        try {
            HttpResponse<InputStream> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 404) {
                throw new InvalidArgumentsException("Remote file not found (404): " + uri);
            }
            if (response.statusCode() >= 400) {
                throw new InvalidArgumentsException(
                        "Failed to download remote file: " + uri + ", status: " + response.statusCode());
            }
            return new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while downloading remote file: " + uri, ex);
        }
    }

    /** Проверяет расширение локального файла на соответствие разрешённым форматам. */
    private void validateLocalExtension(Path path) {
        String extension = extractExtension(fileName(path));
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new InvalidArgumentsException(
                    "Unsupported file format for path: " + path + ". Supported: " + SUPPORTED_EXTENSIONS);
        }
    }

    /** Проверяет расширение удалённого ресурса, если оно явно присутствует в URL. */
    private void validateRemoteExtension(URI uri) {
        String path = uri.getPath();
        if (path == null || path.isBlank()) {
            return;
        }
        String extension = extractExtension(path);
        if (extension.isBlank()) {
            return;
        }
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new InvalidArgumentsException(
                    "Unsupported remote file format: " + uri + ". Supported: " + SUPPORTED_EXTENSIONS);
        }
    }

    /** Извлекает расширение файла в нижнем регистре. */
    private static String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    /** Нормализует входной параметр пути и валидирует, что он не пустой. */
    private static String normalizeInput(String input) {
        if (input == null) {
            throw new InvalidArgumentsException("Input path must not be null");
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidArgumentsException("Input path must not be blank");
        }
        return trimmed;
    }

    /** Определяет, содержит ли путь glob-символы. */
    private static boolean containsGlob(String input) {
        return input.contains("*") || input.contains("?") || input.contains("[") || input.contains("{");
    }

    /** Определяет, является ли вход удалённым URL. */
    private static boolean isRemote(String input) {
        String lower = input.toLowerCase(Locale.ROOT);
        return lower.startsWith("http://") || lower.startsWith("https://");
    }

    /** Находит базовую директорию, от которой будет вычисляться glob-паттерн. */
    private static Path determineBaseDirectory(Path path) {
        Path base = path.getParent();
        while (base != null && containsGlob(base.toString())) {
            base = base.getParent();
        }

        if (base == null) {
            Path root = path.getRoot();
            if (root != null) {
                base = root;
            } else {
                base = Path.of(".");
            }
        }

        return base.toAbsolutePath().normalize();
    }

    /** Проверяет, использует ли шаблон рекурсивную маску {@code **}. */
    private static boolean containsRecursiveWildcard(Path pattern) {
        for (Path segment : pattern) {
            if (segment.toString().contains("**")) {
                return true;
            }
        }
        return false;
    }

    /** Разрешает относительный паттерн относительно базовой директории. */
    private static Path resolveAgainstBase(Path pattern, Path baseDir) {
        if (pattern.isAbsolute()) {
            return pattern;
        }
        return baseDir.resolve(pattern).normalize();
    }

    /** Возвращает имя файла из пути или бросает исключение, если его нет. */
    private static String fileName(Path path) {
        Path fileName = path.getFileName();
        if (fileName == null) {
            throw new InvalidArgumentsException("Path does not contain a file name: " + path);
        }
        return fileName.toString();
    }
}
