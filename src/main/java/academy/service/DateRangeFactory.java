package academy.service;

import academy.exception.InvalidArgumentsException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Создаёт диапазоны дат на основе строковых значений CLI-параметров. */
public class DateRangeFactory {

    /**
     * Парсит входные строки и возвращает валидированный диапазон дат.
     *
     * @param fromOption значение параметра {@code --from}
     * @param toOption значение параметра {@code --to}
     * @return диапазон, ограниченный указанными датами (могут быть {@code null})
     */
    public DateRange create(String fromOption, String toOption) {
        LocalDate from = parseDate(fromOption, "--from");
        LocalDate to = parseDate(toOption, "--to");
        validateRange(from, to);
        return new DateRange(from, to);
    }

    private static LocalDate parseDate(String value, String optionName) {
        if (value == null) {
            return null;
        }
        if (value.isBlank()) {
            throw new InvalidArgumentsException("Invalid value for " + optionName + ": value must not be blank");
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new InvalidArgumentsException(
                    "Invalid value for " + optionName + ": " + value + ". Expected ISO-8601 date (yyyy-MM-dd)", ex);
        }
    }

    private static void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidArgumentsException("--from must be before or equal to --to");
        }
    }
}
