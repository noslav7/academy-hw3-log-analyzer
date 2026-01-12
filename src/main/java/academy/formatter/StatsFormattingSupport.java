package academy.formatter;

import academy.http.HttpStatus;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Набор общих утилит форматирования, используемых всеми структурированными отчётами. */
final class StatsFormattingSupport {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final DecimalFormat INTEGER_FORMAT;

    static {
        DECIMAL_FORMAT = (DecimalFormat) DecimalFormat.getNumberInstance(Locale.US);
        DECIMAL_FORMAT.applyPattern("#0.00");

        INTEGER_FORMAT = (DecimalFormat) DecimalFormat.getIntegerInstance(Locale.US);
        INTEGER_FORMAT.setGroupingUsed(true);
    }

    private StatsFormattingSupport() {}

    static String formatDate(LocalDate date) {
        if (date == null) {
            return "-";
        }
        return DATE_FORMATTER.format(date);
    }

    static String formatInteger(long value) {
        return INTEGER_FORMAT.format(value);
    }

    static String formatSize(BigDecimal value) {
        return DECIMAL_FORMAT.format(value) + "b";
    }

    static String formatDecimal(BigDecimal value) {
        return DECIMAL_FORMAT.format(value);
    }

    static String statusDescription(int code) {
        return HttpStatus.descriptionFor(code);
    }
}
