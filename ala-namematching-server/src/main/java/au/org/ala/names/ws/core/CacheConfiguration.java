package au.org.ala.names.ws.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.cache2k.Cache2kBuilder;

/**
 * A simple cache configuration for {@link Cache2kBuilder}.
 * <p>
 * By rights, this should be something in the actual cache2k library
 * but this is easy to embed into other config information
 * </p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@EqualsAndHashCode
public class CacheConfiguration {
    /** If true, allow JMX interrogation. False by default */
    @JsonProperty
    private boolean enableJmx = false;
    /** The maximum number of entries. 100000 by default */
    @JsonProperty
    private int entryCapacity = 100000;
    /** If true, entries are never expired. True by default */
    @JsonProperty
    private boolean eternal = true;
    /** Keep data in the cache after it has expired. False by default */
    private boolean keepDataAfterExpired = false;
    /** Permit null values to be cached. True by default */
    @JsonProperty
    private boolean permitNullValues = true;
    /** Suppress, rather than propagate exceptions. False by default */
    @JsonProperty
    private boolean suppressExceptions = false;

    /**
     * Construct a cache builder out of the information in this configuration.
     * <p>
     * The cache builder can be extended or overridden before the cahce is
     * constructed.
     * </p>
     *
     * @param keyClass The type of key for the cache
     * @param valueClass The type of value for the cache
     * @param <K> The key type
     * @param <V> The value type
     *
     * @return A partially initialised builder.
     */
    public <K, V> Cache2kBuilder<K, V> builder(Class<K> keyClass, Class<V> valueClass) {
        return Cache2kBuilder.of(keyClass, valueClass)
                .enableJmx(this.enableJmx)
                .entryCapacity(this.entryCapacity)
                .eternal(this.eternal)
                .keepDataAfterExpired(this.keepDataAfterExpired)
                .permitNullValues(this.permitNullValues)
                .suppressExceptions(this.suppressExceptions);
    }
}
