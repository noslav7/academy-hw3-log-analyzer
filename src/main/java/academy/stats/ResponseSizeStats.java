package academy.stats;

import java.math.BigDecimal;

/**
 * Сводная статистика по размерам ответов.
 *
 * @param average средний размер
 * @param max максимальный размер
 * @param p95 95-процентиль
 */
public record ResponseSizeStats(BigDecimal average, BigDecimal max, BigDecimal p95) {}
