package academy.stats;

import java.time.LocalDate;
import java.util.List;

public record StatsResult(
        List<String> files,
        long totalRequestsCount,
        ResponseSizeStats responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStat> responseCodes,
        List<RequestPerDateStat> requestsPerDate,
        List<String> uniqueProtocols,
        LocalDate firstRequestDate,
        LocalDate lastRequestDate) {}

