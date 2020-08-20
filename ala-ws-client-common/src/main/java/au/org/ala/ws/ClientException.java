package au.org.ala.ws;

/**
 * An exception from a webservices client indicating that the client
 * failed in some way (eg. IOException trying to contact the service)
 */
public class ClientException extends RuntimeException {
    /**
     * Constructs a new client exception with {@code null} as its
     * detail message.
     */
    public ClientException() {
        super();
    }

    /**
     * Constructs a new client exception with the specified detail message.
     *
     * @param message the detail message.
     */
    public ClientException(String message) {
        super(message);
    }

    /**
     * Constructs a new client exception with the specified detail message and
     * cause.
     *
     * @param message the detail message (
     * @param cause   the cause
     */
    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
