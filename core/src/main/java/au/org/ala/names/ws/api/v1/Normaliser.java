package au.org.ala.names.ws.api.v1;

/**
 * Normalise a string in a consistent way.
 */
public interface Normaliser {
    /**
     * Normalise a string.
     * <p>
     * If the string is null or empty, the normaliser should return null.
     * </p>
     *
     * @param s The string to normalise, may be null
     *
     * @return The normalised string
     */
    public String normalise(String s);
}
