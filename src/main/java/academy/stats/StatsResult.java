package academy.stats;

import java.time.LocalDate;
import java.util.List;

/**
 * Итоговый снимок статистики, который передаётся форматтерам.
 *
 * @param files список обработанных файлов/источников
 * @param totalRequestsCount общее количество валидных запросов
 * @param responseSizeInBytes статистика по размеру ответов
 * @param resources топ запрашиваемых ресурсов
 * @param responseCodes распределение HTTP-кодов ответа
 * @param requestsPerDate распределение запросов по датам
 * @param uniqueProtocols список уникальных протоколов
 * @param uniqueProtocolsCount количество уникальных протоколов
 * @param firstRequestDate дата первого запроса в выборке
 * @param lastRequestDate дата последнего запроса в выборке
 */
public record StatsResult(
        List<String> files,
        long totalRequestsCount,
        ResponseSizeStats responseSizeInBytes,
        List<ResourceStat> resources,
        List<ResponseCodeStat> responseCodes,
        List<RequestPerDateStat> requestsPerDate,
        List<String> uniqueProtocols,
        long uniqueProtocolsCount,
        LocalDate firstRequestDate,
        LocalDate lastRequestDate) {}
