package academy.stats;

import java.util.List;

/**
 * Содержит список уникальных протоколов и их количество.
 *
 * @param uniqueProtocols уникальные протоколы в порядке вывода
 * @param uniqueProtocolsCount количество уникальных протоколов
 */
public record ProtocolStats(List<String> uniqueProtocols, long uniqueProtocolsCount) {}
