package dev.reformator.loomoroutines.common.internal.kotlinstdlibstub;

public class KotlinException extends RuntimeException {
    public KotlinException() {
    }

    public KotlinException(String message) {
        super(message);
    }

    public KotlinException(String message, Throwable cause) {
        super(message, cause);
    }

    public KotlinException(Throwable cause) {
        super(cause);
    }
}
