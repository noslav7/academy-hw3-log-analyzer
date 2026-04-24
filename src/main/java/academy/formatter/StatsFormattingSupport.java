package academy.formatter;

import academy.http.HttpStatus;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Набор общих утилит форматирования, используемых всеми структурированными отчётами. */
final class StatsFormattingSupport {

    /** Формат отображения даты для Markdown/AsciiDoc отчётов. */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    /** Форматтер дробных значений (размеры и проценты). */
    private static final DecimalFormat DECIMAL_FORMAT;
    /** Форматтер целочисленных значений с группировкой. */
    private static final DecimalFormat INTEGER_FORMAT;

    static {
        DECIMAL_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.US);
        DECIMAL_FORMAT.applyPattern("#0.00");

        INTEGER_FORMAT = (DecimalFormat) DecimalFormat.getIntegerInstance(Locale.US);
        INTEGER_FORMAT.setGroupingUsed(true);
    }

    /** Служебный класс, экземпляры не создаются. */
    private StatsFormattingSupport() {}

    /** Форматирует дату или возвращает дефис при отсутствии значения. */
    static String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return DATE_FORMATTER.format(date);
    }

    /** Форматирует целое число с разделителями разрядов. */
    static String formatInteger(long value) {
        return INTEGER_FORMAT.format(value);
    }

    /** Форматирует размер ответа и добавляет суффикс байтов. */
    static String formatSize(BigDecimal value) {
        return DECIMAL_FORMAT.format(value) + "b";
    }

    /** Форматирует дробное значение с двумя знаками после запятой. */
    static String formatDecimal(BigDecimal value) {
        return DECIMAL_FORMAT.format(value);
    }

    /** Возвращает текстовое описание HTTP-кода. */
    static String statusDescription(int code) {
        return HttpStatus.descriptionFor(code);
    }
}
