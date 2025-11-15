package academy.stats;

import academy.log.LogEntry;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class StatsCollector {

    private final List<Long> responseSizes = new ArrayList<>();
    private final Map<String, LongAdder> resourceCounters = new ConcurrentHashMap<>();
    private final Map<Integer, LongAdder> responseCodeCounters = new ConcurrentHashMap<>();
    private final RequestsPerDateStatistics requestsPerDateStatistics = new RequestsPerDateStatistics();
    private final ProtocolStatistics protocolStatistics = new ProtocolStatistics();

    private long totalRequestsCount;
    private long responseSizeSum;
    private long responseSizeMax;
    private LocalDate firstRequestDate;
    private LocalDate lastRequestDate;

    public void register(LogEntry entry) {
        totalRequestsCount++;
        responseSizeSum += entry.responseSize();
        responseSizeMax = Math.max(responseSizeMax, entry.responseSize());
        responseSizes.add(entry.responseSize());

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

    public StatsResult buildResult(List<String> files) {
        ResponseSizeStats responseSizeStats = new ResponseSizeStats(
                toScaledDecimal(calculateAverage()),
                toScaledDecimal(responseSizeMax),
                toScaledDecimal(calculatePercentile(0.95)));

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

    private double calculateAverage() {
        if (totalRequestsCount == 0) {
            return 0.0d;
        }
        return (double) responseSizeSum / totalRequestsCount;
    }

    private double calculatePercentile(double percentile) {
        if (responseSizes.isEmpty()) {
            return 0.0d;
        }

        List<Long> sorted = responseSizes.stream().sorted().collect(Collectors.toList());
        double rank = percentile * (sorted.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return sorted.get(lowerIndex);
        }

        double lowerValue = sorted.get(lowerIndex);
        double upperValue = sorted.get(upperIndex);
        double weight = rank - lowerIndex;

        return lowerValue + weight * (upperValue - lowerValue);
    }

    private static BigDecimal toScaledDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal toScaledDecimal(long value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
