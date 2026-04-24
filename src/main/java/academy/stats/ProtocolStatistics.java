package academy.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/** Собирает список уникальных протоколов и их частоту появления в логах. */
public class ProtocolStatistics {

    /** Компаратор сортировки протоколов для итогового отчёта. */
    private static final Comparator<Map.Entry<String, LongAdder>> PROTOCOL_COMPARATOR =
            Comparator.<Map.Entry<String, LongAdder>, Boolean>comparing(
                            entry -> !entry.getKey().startsWith("HTTP/"))
                    .thenComparing(
                            (Map.Entry<String, LongAdder> entry) ->
                                    entry.getValue().sum(),
                            Comparator.reverseOrder())
                    .thenComparing(Map.Entry::getKey);

    /** Счётчики количества запросов по каждому протоколу. */
    private final Map<String, LongAdder> protocolCounters = new ConcurrentHashMap<>();

    /** Учитывает очередное значение протокола, если оно непустое. */
    public void register(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            return;
        }
        protocolCounters.computeIfAbsent(protocol, key -> new LongAdder()).increment();
    }

    /** Возвращает итоговую статистику по протоколам в заранее определённом порядке. */
    public ProtocolStats build() {
        List<Map.Entry<String, LongAdder>> entries = new ArrayList<>(protocolCounters.entrySet());
        entries.sort(PROTOCOL_COMPARATOR);

        List<String> protocols = entries.stream().map(Map.Entry::getKey).toList();

        return new ProtocolStats(List.copyOf(protocols), protocols.size());
    }
}
