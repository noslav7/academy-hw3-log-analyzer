package academy.stats;

/**
 * Содержит частоту появления конкретного HTTP-кода.
 *
 * @param code HTTP-код ответа
 * @param totalResponsesCount количество ответов с данным кодом
 */
public record ResponseCodeStat(int code, long totalResponsesCount) {}
