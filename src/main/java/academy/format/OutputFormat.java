package academy.format;

import academy.exception.InvalidArgumentsException;

public enum OutputFormat {
    JSON("json", ".json"),
    MARKDOWN("markdown", ".md");

    private final String cliName;
    private final String fileExtension;

    OutputFormat(String cliName, String fileExtension) {
        this.cliName = cliName;
        this.fileExtension = fileExtension;
    }

    public String getCliName() {
        return cliName;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public static OutputFormat from(String value) {
        for (OutputFormat format : OutputFormat.values()) {
            if (format.cliName.equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new InvalidArgumentsException("Unsupported format: " + value);
    }
}

