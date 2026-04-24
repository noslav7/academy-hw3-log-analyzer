package academy.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Диапазон дат (включительно), используемый для фильтрации логов.
 *
 * @param from нижняя граница (может отсутствовать)
 * @param to верхняя граница (может отсутствовать)
 */
public record DateRange(LocalDate from, LocalDate to) {

    /**
     * Проверяет, попадает ли timestamp в диапазон дат.
     *
     * @param timestamp дата/время запроса из лога
     * @return {@code true}, если запись удовлетворяет фильтру
     */
    public boolean includes(ZonedDateTime timestamp) {
        LocalDate date = timestamp.toLocalDate();
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }
}
