package academy.stats;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class ProtocolStatistics {

    private static final Comparator<Map.Entry<String, LongAdder>> PROTOCOL_COMPARATOR =
            Comparator.<Map.Entry<String, LongAdder>, Boolean>comparing(
                            entry -> !entry.getKey().startsWith("HTTP/"))
                    .thenComparing(
                            (Map.Entry<String, LongAdder> entry) ->
                                    entry.getValue().sum(),
                            Comparator.reverseOrder())
                    .thenComparing(Map.Entry::getKey);

    private final Map<String, LongAdder> protocolCounters = new ConcurrentHashMap<>();

    public void register(String protocol) {
        if (protocol == null || protocol.isBlank()) {
            return;
        }
        protocolCounters.computeIfAbsent(protocol, key -> new LongAdder()).increment();
    }

    public ProtocolStats build() {
        List<Map.Entry<String, LongAdder>> entries = new ArrayList<>(protocolCounters.entrySet());
        entries.sort(PROTOCOL_COMPARATOR);

        List<String> protocols = entries.stream().map(Map.Entry::getKey).toList();

        return new ProtocolStats(List.copyOf(protocols), protocols.size());
    }
}
