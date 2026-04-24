package academy.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/** Считает распределение количества запросов по датам и долю от общего числа. */
public class RequestsPerDateStatistics {

    /** Счётчики количества запросов по датам. */
    private final Map<LocalDate, LongAdder> requestsCounters = new ConcurrentHashMap<>();

    /** Увеличивает счётчик запросов для указанной даты. */
    public void register(LocalDate date) {
        requestsCounters.computeIfAbsent(date, key -> new LongAdder()).increment();
    }

    /**
     * Формирует отсортированный список статистики по датам.
     *
     * @param totalRequestsCount общее число запросов (используется для вычисления процентов)
     * @return список статистик по датам
     */
    public List<RequestPerDateStat> build(long totalRequestsCount) {
        return requestsCounters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toStat(entry, totalRequestsCount))
                .collect(Collectors.toList());
    }

    /** Преобразует запись счётчика даты в формат финальной статистики. */
    private static RequestPerDateStat toStat(Map.Entry<LocalDate, LongAdder> entry, long totalRequestsCount) {
        long count = entry.getValue().sum();
        BigDecimal percentage = totalRequestsCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(count * 100.0d / totalRequestsCount).setScale(2, RoundingMode.HALF_UP);
        String weekday = entry.getKey().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return new RequestPerDateStat(entry.getKey(), weekday, count, percentage);
    }
}
