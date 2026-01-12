package academy.stats;

/** Содержит частоту появления конкретного HTTP-кода. */
public record ResponseCodeStat(int code, long totalResponsesCount) {}
