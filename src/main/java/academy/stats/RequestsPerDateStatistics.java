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

public class RequestsPerDateStatistics {

    private final Map<LocalDate, LongAdder> requestsCounters = new ConcurrentHashMap<>();

    public void register(LocalDate date) {
        requestsCounters.computeIfAbsent(date, key -> new LongAdder()).increment();
    }

    public List<RequestPerDateStat> build(long totalRequestsCount) {
        return requestsCounters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> toStat(entry, totalRequestsCount))
                .collect(Collectors.toList());
    }

    private static RequestPerDateStat toStat(
            Map.Entry<LocalDate, LongAdder> entry, long totalRequestsCount) {
        long count = entry.getValue().sum();
        BigDecimal percentage = totalRequestsCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(count * 100.0d / totalRequestsCount)
                        .setScale(2, RoundingMode.HALF_UP);
        String weekday = entry.getKey().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return new RequestPerDateStat(entry.getKey(), weekday, count, percentage);
    }
}


