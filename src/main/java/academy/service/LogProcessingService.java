package academy.service;

import academy.log.LogEntry;
import academy.log.LogEntryParser;
import academy.stats.StatsCollector;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogProcessingService {

    private static final Logger LOGGER = LogManager.getLogger(LogProcessingService.class);

    private final LogEntryParser parser;

    public LogProcessingService(LogEntryParser parser) {
        this.parser = parser;
    }

    public void process(
            BufferedReader reader, String sourceName, DateRange dateRange, StatsCollector collector)
            throws IOException {
        String line;
        long lineCounter = 0L;
        while ((line = reader.readLine()) != null) {
            lineCounter++;
            Optional<LogEntry> entry = parser.parse(line);
            if (entry.isEmpty()) {
                LOGGER.warn("Skipped malformed log line {} from {}", lineCounter, sourceName);
                continue;
            }
            LogEntry logEntry = entry.get();
            if (!dateRange.includes(logEntry.timestamp())) {
                continue;
            }
            collector.register(logEntry);
        }
    }
}

