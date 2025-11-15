package academy.stats;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ProtocolStatisticsTest {

    @Test
    void GivenProtocols_WhenBuild_ThenSortsHttpFirstAndCounts() {
        ProtocolStatistics statistics = new ProtocolStatistics();
        statistics.register("grpc");
        statistics.register("HTTP/2.0");
        statistics.register("HTTP/1.1");
        statistics.register("grpc");
        statistics.register("HTTP/1.1");

        ProtocolStats result = statistics.build();

        assertAll(
                () -> assertEquals(List.of("HTTP/1.1", "HTTP/2.0", "grpc"), result.uniqueProtocols()),
                () -> assertEquals(3L, result.uniqueProtocolsCount()));
    }

    @Test
    void GivenBlankProtocols_WhenRegister_ThenIgnored() {
        ProtocolStatistics statistics = new ProtocolStatistics();
        statistics.register(" ");
        statistics.register(null);

        ProtocolStats result = statistics.build();

        assertAll(
                () -> assertTrue(result.uniqueProtocols().isEmpty()),
                () -> assertEquals(0L, result.uniqueProtocolsCount()));
    }
}
