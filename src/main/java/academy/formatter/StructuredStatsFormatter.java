package academy.formatter;

import static academy.formatter.StatsFormattingSupport.formatDate;
import static academy.formatter.StatsFormattingSupport.formatDecimal;
import static academy.formatter.StatsFormattingSupport.formatInteger;
import static academy.formatter.StatsFormattingSupport.formatSize;
import static academy.formatter.StatsFormattingSupport.statusDescription;

import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Базовый класс для форматтеров с одинаковым набором секций и структурированных таблиц. */
abstract class StructuredStatsFormatter implements StatsFormatter {

    @Override
    public final String format(StatsResult statsResult) {
        StringBuilder builder = new StringBuilder();
        renderGeneralInfo(builder, buildGeneralInfo(statsResult));
        renderResources(builder, buildResources(statsResult));
        renderResponseCodes(builder, buildResponseCodes(statsResult));
        renderRequestsPerDate(builder, buildRequestsPerDate(statsResult));
        renderProtocols(builder, buildProtocols(statsResult));
        return builder.toString();
    }

    protected abstract void renderGeneralInfo(StringBuilder builder, List<GeneralInfoRow> rows);

    protected abstract void renderResources(StringBuilder builder, SectionData<ResourceRow> section);

    protected abstract void renderResponseCodes(StringBuilder builder, SectionData<ResponseCodeRow> section);

    protected abstract void renderRequestsPerDate(StringBuilder builder, SectionData<RequestsPerDateRow> section);

    protected abstract void renderProtocols(StringBuilder builder, SectionData<String> section);

    protected abstract String formatFilesCell(List<String> files);

    private List<GeneralInfoRow> buildGeneralInfo(StatsResult statsResult) {
        List<GeneralInfoRow> rows = new ArrayList<>();
        rows.add(new GeneralInfoRow(GeneralMetric.FILES, formatFilesCell(statsResult.files())));
        rows.add(new GeneralInfoRow(GeneralMetric.FIRST_DATE, formatDate(statsResult.firstRequestDate())));
        rows.add(new GeneralInfoRow(GeneralMetric.LAST_DATE, formatDate(statsResult.lastRequestDate())));
        rows.add(new GeneralInfoRow(GeneralMetric.TOTAL_REQUESTS, formatInteger(statsResult.totalRequestsCount())));
        rows.add(new GeneralInfoRow(GeneralMetric.UNIQUE_PROTOCOLS, formatInteger(statsResult.uniqueProtocolsCount())));

        ResponseSizeStats responseSizeStats = statsResult.responseSizeInBytes();
        rows.add(new GeneralInfoRow(GeneralMetric.RESPONSE_SIZE_AVERAGE, formatSize(responseSizeStats.average())));
        rows.add(new GeneralInfoRow(GeneralMetric.RESPONSE_SIZE_MAX, formatSize(responseSizeStats.max())));
        rows.add(new GeneralInfoRow(GeneralMetric.RESPONSE_SIZE_P95, formatSize(responseSizeStats.p95())));
        return rows;
    }

    private SectionData<ResourceRow> buildResources(StatsResult statsResult) {
        if (statsResult.resources().isEmpty()) {
            return new SectionData<>(List.of(new ResourceRow("-", formatInteger(0), true)), false);
        }
        List<ResourceRow> rows = statsResult.resources().stream()
                .map(stat -> new ResourceRow(stat.resource(), formatInteger(stat.totalRequestsCount()), false))
                .toList();
        return new SectionData<>(rows, true);
    }

    private SectionData<ResponseCodeRow> buildResponseCodes(StatsResult statsResult) {
        if (statsResult.responseCodes().isEmpty()) {
            return new SectionData<>(List.of(new ResponseCodeRow("-", "-", formatInteger(0), true)), false);
        }
        List<ResponseCodeRow> rows = statsResult.responseCodes().stream()
                .map(stat -> new ResponseCodeRow(
                        String.valueOf(stat.code()),
                        statusDescription(stat.code()),
                        formatInteger(stat.totalResponsesCount()),
                        false))
                .toList();
        return new SectionData<>(rows, true);
    }

    private SectionData<RequestsPerDateRow> buildRequestsPerDate(StatsResult statsResult) {
        if (statsResult.requestsPerDate().isEmpty()) {
            return new SectionData<>(
                    List.of(new RequestsPerDateRow("-", "-", formatInteger(0), formatDecimal(BigDecimal.ZERO), true)),
                    false);
        }
        List<RequestsPerDateRow> rows = statsResult.requestsPerDate().stream()
                .map(stat -> new RequestsPerDateRow(
                        stat.date().toString(),
                        stat.weekday(),
                        formatInteger(stat.totalRequestsCount()),
                        formatDecimal(stat.totalRequestsPercentage()),
                        false))
                .toList();
        return new SectionData<>(rows, true);
    }

    private SectionData<String> buildProtocols(StatsResult statsResult) {
        if (statsResult.uniqueProtocols().isEmpty()) {
            return new SectionData<>(List.of("-"), false);
        }
        return new SectionData<>(List.copyOf(statsResult.uniqueProtocols()), true);
    }

    protected enum GeneralMetric {
        FILES,
        FIRST_DATE,
        LAST_DATE,
        TOTAL_REQUESTS,
        UNIQUE_PROTOCOLS,
        RESPONSE_SIZE_AVERAGE,
        RESPONSE_SIZE_MAX,
        RESPONSE_SIZE_P95
    }

    protected record GeneralInfoRow(GeneralMetric metric, String value) {}

    protected record ResourceRow(String resource, String totalRequests, boolean placeholder) {}

    protected record ResponseCodeRow(String code, String description, String totalResponses, boolean placeholder) {}

    protected record RequestsPerDateRow(
            String date, String weekday, String totalRequests, String percentage, boolean placeholder) {}

    protected record SectionData<T>(List<T> rows, boolean hasData) {}
}
