package academy.stats;

import java.math.BigDecimal;

public record ResponseSizeStats(BigDecimal average, BigDecimal max, BigDecimal p95) {}
