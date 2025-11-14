package academy.formatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

final class StatsFormattingSupport {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DecimalFormat DECIMAL_FORMAT;
    private static final DecimalFormat INTEGER_FORMAT;
    private static final Map<Integer, String> STATUS_DESCRIPTIONS =
            Map.ofEntries(
                    Map.entry(100, "Continue"),
                    Map.entry(101, "Switching Protocols"),
                    Map.entry(200, "OK"),
                    Map.entry(201, "Created"),
                    Map.entry(202, "Accepted"),
                    Map.entry(204, "No Content"),
                    Map.entry(301, "Moved Permanently"),
                    Map.entry(302, "Found"),
                    Map.entry(304, "Not Modified"),
                    Map.entry(400, "Bad Request"),
                    Map.entry(401, "Unauthorized"),
                    Map.entry(403, "Forbidden"),
                    Map.entry(404, "Not Found"),
                    Map.entry(405, "Method Not Allowed"),
                    Map.entry(409, "Conflict"),
                    Map.entry(500, "Internal Server Error"),
                    Map.entry(501, "Not Implemented"),
                    Map.entry(502, "Bad Gateway"),
                    Map.entry(503, "Service Unavailable"));

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
        return STATUS_DESCRIPTIONS.getOrDefault(code, "Unknown");
    }
}


