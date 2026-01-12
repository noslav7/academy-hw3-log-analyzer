package academy.formatter;

import academy.stats.RequestPerDateStat;
import academy.stats.ResourceStat;
import academy.stats.ResponseCodeStat;
import academy.stats.ResponseSizeStats;
import academy.stats.StatsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** Формирует отчёт в JSON-формате согласно описанной схеме. */
public class JsonStatsFormatter implements StatsFormatter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public String format(StatsResult statsResult) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();

        ArrayNode files = root.putArray("files");
        statsResult.files().forEach(files::add);
        root.put("totalRequestsCount", statsResult.totalRequestsCount());

        ResponseSizeStats sizeStats = statsResult.responseSizeInBytes();
        ObjectNode responseSizeNode = root.putObject("responseSizeInBytes");
        responseSizeNode.set("average", new DecimalNode(sizeStats.average()));
        responseSizeNode.set("max", new DecimalNode(sizeStats.max()));
        responseSizeNode.set("p95", new DecimalNode(sizeStats.p95()));

        ArrayNode resourcesNode = root.putArray("resources");
        for (ResourceStat resourceStat : statsResult.resources()) {
            ObjectNode resourceNode = resourcesNode.addObject();
            resourceNode.put("resource", resourceStat.resource());
            resourceNode.put("totalRequestsCount", resourceStat.totalRequestsCount());
        }

        ArrayNode responseCodesNode = root.putArray("responseCodes");
        for (ResponseCodeStat responseCodeStat : statsResult.responseCodes()) {
            ObjectNode responseCodeNode = responseCodesNode.addObject();
            responseCodeNode.put("code", responseCodeStat.code());
            responseCodeNode.put("totalResponsesCount", responseCodeStat.totalResponsesCount());
        }

        ArrayNode requestsPerDateNode = root.putArray("requestsPerDate");
        for (RequestPerDateStat perDateStat : statsResult.requestsPerDate()) {
            ObjectNode perDateNode = requestsPerDateNode.addObject();
            perDateNode.put("date", perDateStat.date().toString());
            perDateNode.put("weekday", perDateStat.weekday());
            perDateNode.put("totalRequestsCount", perDateStat.totalRequestsCount());
            perDateNode.set("totalRequestsPercentage", new DecimalNode(perDateStat.totalRequestsPercentage()));
        }

        root.put("uniqueProtocolsCount", statsResult.uniqueProtocolsCount());

        ArrayNode protocolsNode = root.putArray("uniqueProtocols");
        statsResult.uniqueProtocols().forEach(protocolsNode::add);

        try {
            return OBJECT_MAPPER.writeValueAsString(root);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize statistics to JSON", ex);
        }
    }
}
