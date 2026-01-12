package academy.stats;

/**
 * Статистика по одному ресурсу.
 *
 * @param resource путь ресурса
 * @param totalRequestsCount количество запросов
 */
public record ResourceStat(String resource, long totalRequestsCount) {}
