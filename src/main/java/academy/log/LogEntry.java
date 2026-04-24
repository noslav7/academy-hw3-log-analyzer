package academy.log;

import java.time.ZonedDateTime;

/**
 * Домашняя модель строки access-лога NGINX.
 *
 * @param remoteAddress IP-адрес клиента
 * @param remoteUser пользователь из лога (если отсутствует, хранится пустая строка)
 * @param timestamp время запроса
 * @param method HTTP-метод запроса
 * @param resource путь запрашиваемого ресурса
 * @param protocol версия/значение протокола
 * @param statusCode код ответа сервера
 * @param responseSize размер ответа в байтах
 */
public record LogEntry(
        String remoteAddress,
        String remoteUser,
        ZonedDateTime timestamp,
        String method,
        String resource,
        String protocol,
        int statusCode,
        long responseSize) {}
