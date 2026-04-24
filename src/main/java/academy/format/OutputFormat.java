package academy.format;

import academy.exception.InvalidArgumentsException;

/** Поддерживаемые форматы вывода статистики и их CLI-псевдонимы. */
public enum OutputFormat {
    JSON("json", ".json"),
    MARKDOWN("markdown", ".md"),
    ADOC("adoc", ".adoc");

    /** Имя формата, используемое в CLI-параметрах. */
    private final String cliName;
    /** Ожидаемое расширение файла для данного формата. */
    private final String fileExtension;

    OutputFormat(String cliName, String fileExtension) {
        this.cliName = cliName;
        this.fileExtension = fileExtension;
    }

    /** Возвращает CLI-алиас формата. */
    public String getCliName() {
        return cliName;
    }

    /** Возвращает расширение файла, соответствующее формату. */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Находит формат по пользовательской строке.
     *
     * @param value значение из CLI
     * @return найденный формат вывода
     */
    public static OutputFormat from(String value) {
        for (OutputFormat format : values()) {
            if (format.cliName.equalsIgnoreCase(value)) {
                return format;
            }
        }
        throw new InvalidArgumentsException("Unsupported format: " + value);
    }
}
