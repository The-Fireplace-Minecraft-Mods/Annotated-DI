package dev.the_fireplace.annotateddi.impl.di;

public class ImplementationException extends IllegalStateException {
    public ImplementationException() {
        super();
    }

    public ImplementationException(String s) {
        super(s);
    }

    public ImplementationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImplementationException(Throwable cause) {
        super(cause);
    }
}
