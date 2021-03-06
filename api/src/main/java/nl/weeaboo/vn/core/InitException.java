package nl.weeaboo.vn.core;

/**
 * Represents a fatal error during startup or initialisation.
 */
public class InitException extends Exception {

    private static final long serialVersionUID = 1L;

    public InitException(String message) {
        this(message, null);
    }

    public InitException(Throwable cause) {
        this("Fatal error during initialization", cause);
    }

    public InitException(String message, Throwable cause) {
        super(message, cause);
    }

}