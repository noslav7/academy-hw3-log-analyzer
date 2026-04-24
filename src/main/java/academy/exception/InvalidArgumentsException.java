package academy.exception;

/** Ошибка валидации входных аргументов приложения. */
public class InvalidArgumentsException extends RuntimeException {

    /** Создаёт исключение с человекочитаемым сообщением. */
    public InvalidArgumentsException(String message) {
        super(message);
    }

    /** Создаёт исключение с сообщением и исходной причиной. */
    public InvalidArgumentsException(String message, Throwable cause) {
        super(message, cause);
    }
}
