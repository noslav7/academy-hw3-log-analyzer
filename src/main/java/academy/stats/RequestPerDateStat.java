package academy.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Описывает активность за конкретную дату. */
public record RequestPerDateStat(
        LocalDate date, String weekday, long totalRequestsCount, BigDecimal totalRequestsPercentage) {}
