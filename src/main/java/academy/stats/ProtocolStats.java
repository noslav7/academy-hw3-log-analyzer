package academy.stats;

import java.util.List;

/** Содержит список уникальных протоколов и их количество. */
public record ProtocolStats(List<String> uniqueProtocols, long uniqueProtocolsCount) {}
