package academy.stats;

import java.time.LocalDate;
import java.util.List;

/** Итоговый снимок статистики, который передаётся форматтерам. */
public record StatsResult(
        List<String> files,
        long totalRequestsCount,
        ResponseSizeStats responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStat> responseCodes,
        List<RequestPerDateStat> requestsPerDate,
        List<String> uniqueProtocols,
        long uniqueProtocolsCount,
        LocalDate firstRequestDate,
        LocalDate lastRequestDate) {}
