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

    /** Формирует полный отчёт, последовательно отрисовывая все секции статистики. */
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

    /** Отрисовывает секцию общей информации. */
    protected abstract void renderGeneralInfo(StringBuilder builder, List<GeneralInfoRow> rows);

    /** Отрисовывает таблицу запрашиваемых ресурсов. */
    protected abstract void renderResources(StringBuilder builder, SectionData<ResourceRow> section);

    /** Отрисовывает статистику кодов ответа. */
    protected abstract void renderResponseCodes(StringBuilder builder, SectionData<ResponseCodeRow> section);

    /** Отрисовывает статистику запросов по датам. */
    protected abstract void renderRequestsPerDate(StringBuilder builder, SectionData<RequestsPerDateRow> section);

    /** Отрисовывает список используемых протоколов. */
    protected abstract void renderProtocols(StringBuilder builder, SectionData<String> section);

    /** Форматирует ячейку со списком обработанных файлов. */
    protected abstract String formatFilesCell(List<String> files);

    /** Собирает строки секции общей информации из итогового объекта статистики. */
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

    /** Собирает данные секции популярных ресурсов. */
    private SectionData<ResourceRow> buildResources(StatsResult statsResult) {
        if (statsResult.resources().isEmpty()) {
            return new SectionData<>(List.of(new ResourceRow("-", formatInteger(0), true)), false);
        }
        List<ResourceRow> rows = statsResult.resources().stream()
                .map(stat -> new ResourceRow(stat.resource(), formatInteger(stat.totalRequestsCount()), false))
                .toList();
        return new SectionData<>(rows, true);
    }

    /** Собирает данные секции распределения HTTP-кодов. */
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

    /** Собирает данные секции распределения запросов по датам. */
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

    /** Собирает данные секции уникальных протоколов. */
    private SectionData<String> buildProtocols(StatsResult statsResult) {
        if (statsResult.uniqueProtocols().isEmpty()) {
            return new SectionData<>(List.of("-"), false);
        }
        return new SectionData<>(List.copyOf(statsResult.uniqueProtocols()), true);
    }

    /** Перечень метрик, выводимых в общей секции отчёта. */
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

    /** Строка общей секции: метрика и её форматированное значение. */
    protected record GeneralInfoRow(GeneralMetric metric, String value) {}

    /** Строка секции ресурсов. */
    protected record ResourceRow(String resource, String totalRequests, boolean placeholder) {}

    /** Строка секции HTTP-кодов ответа. */
    protected record ResponseCodeRow(String code, String description, String totalResponses, boolean placeholder) {}

    /** Строка секции запросов по датам. */
    protected record RequestsPerDateRow(
            String date, String weekday, String totalRequests, String percentage, boolean placeholder) {}

    /** Универсальный контейнер данных секции и флаг наличия реальных данных. */
    protected record SectionData<T>(List<T> rows, boolean hasData) {}
}
