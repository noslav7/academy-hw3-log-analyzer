package academy.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Описывает активность за конкретную дату.
 *
 * @param date дата запросов
 * @param weekday название дня недели
 * @param totalRequestsCount количество запросов за дату
 * @param totalRequestsPercentage доля запросов от общего количества в процентах
 */
public record RequestPerDateStat(
        LocalDate date, String weekday, long totalRequestsCount, BigDecimal totalRequestsPercentage) {}
