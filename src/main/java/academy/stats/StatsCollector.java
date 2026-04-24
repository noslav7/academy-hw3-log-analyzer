package academy.stats;

import academy.log.LogEntry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/** Накопитель метрик, подсчитывающий агрегированную статистику по потокам логов. */
public class StatsCollector {

    /** Счётчики запросов по ресурсам. */
    private final Map<String, LongAdder> resourceCounters = new ConcurrentHashMap<>();
    /** Счётчики ответов по HTTP-кодам. */
    private final Map<Integer, LongAdder> responseCodeCounters = new ConcurrentHashMap<>();
    /** Агрегатор статистики запросов по датам. */
    private final RequestsPerDateStatistics requestsPerDateStatistics = new RequestsPerDateStatistics();
    /** Агрегатор статистики используемых протоколов. */
    private final ProtocolStatistics protocolStatistics = new ProtocolStatistics();
    /** Оценщик 95-процентиля размера ответа. */
    private final PercentileEstimator percentileEstimator = new PercentileEstimator(0.95d);

    /** Общее количество валидных запросов. */
    private long totalRequestsCount;
    /** Суммарный размер всех ответов. */
    private long responseSizeSum;
    /** Максимальный размер ответа. */
    private long responseSizeMax;
    /** Наиболее ранняя дата запроса. */
    private LocalDate firstRequestDate;
    /** Наиболее поздняя дата запроса. */
    private LocalDate lastRequestDate;

    /**
     * Регистрирует очередную запись лога в статистике.
     *
     * @param entry успешно распарсенная строка лога
     */
    public void register(LogEntry entry) {
        totalRequestsCount++;
        responseSizeSum += entry.responseSize();
        responseSizeMax = Math.max(responseSizeMax, entry.responseSize());
        percentileEstimator.addSample(entry.responseSize());

        if (!entry.resource().isBlank()) {
            resourceCounters
                    .computeIfAbsent(entry.resource(), key -> new LongAdder())
                    .increment();
        }

        responseCodeCounters
                .computeIfAbsent(entry.statusCode(), key -> new LongAdder())
                .increment();

        LocalDate date = entry.timestamp().toLocalDate();
        requestsPerDateStatistics.register(date);

        protocolStatistics.register(entry.protocol());

        if (firstRequestDate == null || date.isBefore(firstRequestDate)) {
            firstRequestDate = date;
        }
        if (lastRequestDate == null || date.isAfter(lastRequestDate)) {
            lastRequestDate = date;
        }
    }

    /**
     * Собирает итоговый снимок статистики и сбрасывает его в неизменяемую структуру.
     *
     * @param files список обработанных файлов (используется в финальном отчёте)
     * @return готовый {@link StatsResult}
     */
    public StatsResult buildResult(List<String> files) {
        ResponseSizeStats responseSizeStats = new ResponseSizeStats(
                toScaledDecimal(calculateAverage()),
                toScaledDecimal(responseSizeMax),
                toScaledDecimal(percentileEstimator.estimate()));

        List<ResourceStat> topResources = resourceCounters.entrySet().stream()
                .map(entry -> new ResourceStat(entry.getKey(), entry.getValue().sum()))
                .sorted(Comparator.comparingLong(ResourceStat::totalRequestsCount)
                        .reversed()
                        .thenComparing(ResourceStat::resource))
                .limit(10)
                .collect(Collectors.toList());

        List<ResponseCodeStat> responseCodeStats = responseCodeCounters.entrySet().stream()
                .map(entry ->
                        new ResponseCodeStat(entry.getKey(), entry.getValue().sum()))
                .sorted(Comparator.<ResponseCodeStat>comparingLong(ResponseCodeStat::totalResponsesCount)
                        .reversed()
                        .thenComparingInt(ResponseCodeStat::code))
                .collect(Collectors.toList());

        List<RequestPerDateStat> perDateStats = requestsPerDateStatistics.build(totalRequestsCount);

        ProtocolStats protocolStats = protocolStatistics.build();

        return new StatsResult(
                List.copyOf(files),
                totalRequestsCount,
                responseSizeStats,
                topResources,
                responseCodeStats,
                perDateStats,
                protocolStats.uniqueProtocols(),
                protocolStats.uniqueProtocolsCount(),
                firstRequestDate,
                lastRequestDate);
    }

    /** Вычисляет средний размер ответа. */
    private double calculateAverage() {
        if (totalRequestsCount == 0) {
            return 0.0d;
        }
        return (double) responseSizeSum / totalRequestsCount;
    }

    /** Преобразует дробное значение к нормализованному формату десятичного числа. */
    private static BigDecimal toScaledDecimal(double value) {
        return adjustScale(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
    }

    /** Преобразует целочисленное значение к нормализованному формату десятичного числа. */
    private static BigDecimal toScaledDecimal(long value) {
        return adjustScale(BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP));
    }

    /** Удаляет лишние нули, сохраняя минимум один знак после запятой. */
    private static BigDecimal adjustScale(BigDecimal value) {
        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() < 1) {
            return normalized.setScale(1, RoundingMode.UNNECESSARY);
        }
        return normalized;
    }
}
