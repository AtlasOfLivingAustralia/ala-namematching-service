package au.org.ala.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.cache2k.Cache2kBuilder;
import org.cache2k.addon.UniversalResiliencePolicy;
import org.cache2k.extra.jmx.JmxSupport;

/**
 * A simple cache configuration for {@link Cache2kBuilder}.
 * <p>
 * By rights, this should be something in the actual cache2k library
 * but this is easy to embed into other config information
 * </p>
 * <p>
 * Note that to actually use this in anger, you need to have
 * a cache2k implmentation for <code>org.cache2k.spi.Cache2kCoreProvider</code>
 * in the classplath.
 * </p>
 */
@Value
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = DataCacheConfiguration.DataCacheConfigurationBuilder.class)
public class DataCacheConfiguration {
    /** If true, allow JMX interrogation. False by default */
    @JsonProperty
    @Builder.Default
    private boolean enableJmx = false;
    /** The maximum number of entries. 100000 by default */
    @JsonProperty
    @Builder.Default
    private int entryCapacity = 100000;
    /** If true, entries are never expired. True by default */
    @JsonProperty
    @Builder.Default
    private boolean eternal = true;
    /** Keep data in the cache after it has expired. False by default */
    @JsonProperty
    @Builder.Default
    private boolean keepDataAfterExpired = false;
    /** Permit null values to be cached. True by default */
    @JsonProperty
    @Builder.Default
    private boolean permitNullValues = true;
    /** Suppress, rather than propagate exceptions. False by default */
    @JsonProperty
    @Builder.Default
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
    public <K, V> Cache2kBuilder<K, V> cacheBuilder(Class<K> keyClass, Class<V> valueClass) {
        Cache2kBuilder<K, V> builder = Cache2kBuilder.of(keyClass, valueClass)
                .entryCapacity(this.entryCapacity)
                .eternal(this.eternal)
                .keepDataAfterExpired(this.keepDataAfterExpired)
                .permitNullValues(this.permitNullValues);
        if (enableJmx)
            builder.enable(JmxSupport.class);
        if (this.suppressExceptions)
            builder.setup(UniversalResiliencePolicy::enable);
        return builder;
    }
}
