package academy.service;

import academy.exception.InvalidArgumentsException;
import academy.format.OutputFormat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public class OutputFilePreparer {

    public Path prepare(String outputPathOption, OutputFormat format) throws IOException {
        Path outputPath = Paths.get(outputPathOption).toAbsolutePath().normalize();

        ensureFileDoesNotExist(outputPath);
        ensureExtensionMatchesFormat(outputPath, format);
        ensureParentDirectoryReady(outputPath);

        return outputPath;
    }

    private static void ensureFileDoesNotExist(Path outputPath) {
        if (Files.exists(outputPath)) {
            throw new InvalidArgumentsException("Output file already exists: " + outputPath);
        }
    }

    private static void ensureExtensionMatchesFormat(Path outputPath, OutputFormat format) {
        String fileName = outputPath.getFileName().toString();
        String extension = extractExtension(fileName);
        String expectedExtension = format.getFileExtension().substring(1);
        if (!expectedExtension.equalsIgnoreCase(extension)) {
            throw new InvalidArgumentsException(
                    "Output file extension does not match format. Expected " + format.getFileExtension());
        }
    }

    private static void ensureParentDirectoryReady(Path outputPath) throws IOException {
        Path parent = outputPath.getParent();
        if (parent == null) {
            return;
        }
        if (!Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        if (!Files.isWritable(parent)) {
            throw new InvalidArgumentsException("Output directory is not writable: " + parent);
        }
    }

    private static String extractExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}


