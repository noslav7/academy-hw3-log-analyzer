package academy.http;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Перечень распространённых HTTP-статусов с человекочитаемыми описаниями. */
public enum HttpStatus {
    CONTINUE(100, "Continue"),
    SWITCHING_PROTOCOLS(101, "Switching Protocols"),
    OK(200, "OK"),
    CREATED(201, "Created"),
    ACCEPTED(202, "Accepted"),
    NO_CONTENT(204, "No Content"),
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    FOUND(302, "Found"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not Found"),
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    CONFLICT(409, "Conflict"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    NOT_IMPLEMENTED(501, "Not Implemented"),
    BAD_GATEWAY(502, "Bad Gateway"),
    SERVICE_UNAVAILABLE(503, "Service Unavailable");

    /** Быстрый индекс: код статуса -> enum-значение. */
    private static final Map<Integer, HttpStatus> INDEX = EnumSet.allOf(HttpStatus.class).stream()
            .collect(Collectors.toUnmodifiableMap(HttpStatus::code, Function.identity()));

    /** Числовой код HTTP-статуса. */
    private final int code;
    /** Текстовое описание HTTP-статуса. */
    private final String description;

    HttpStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    /** Возвращает числовой код статуса. */
    public int code() {
        return code;
    }

    /** Возвращает текстовое описание статуса. */
    public String description() {
        return description;
    }

    /**
     * Возвращает описание по коду HTTP-статуса.
     *
     * @param code HTTP-код ответа
     * @return известное описание или {@code "Unknown"}
     */
    public static String descriptionFor(int code) {
        HttpStatus status = INDEX.get(code);
        return status == null ? "Unknown" : status.description();
    }
}
