package au.org.ala.names.ws.health;

import java.util.Map;

/**
 * Something that can be interrogated by a health check
 */
public interface Checkable {
    /**
     * Simple health check
     *
     * @return True if the resource appears to be working, false otherwise
     */
    public boolean check();

    /**
     * Get descriptive metrics as to how the resource is doing
     * @return
     */
    public Map<String, Object> getMetrics();
}
