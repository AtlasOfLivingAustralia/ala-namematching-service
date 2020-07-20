package au.org.ala.ws;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * A generic set of configuration parameters for a web client.
 * This configuration is esigned to create retrofit/okhttp clients for
 * use by other services.
 */
@Value
@Builder
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Slf4j
public class ClientConfiguration {
    /** The base url to use when accessing the API */
    @JsonProperty
    private URL baseUrl;
    /** The timeout for connection in milliseconds (defaults to 30 seconds) */
    @JsonProperty
    @Builder.Default
    private long timeOut = (long) 30 * 1000;
    /** Cache results (defaults to true) */
    @JsonProperty
    @Builder.Default
    private boolean cache = true;
    /** Cache directory (defaults to a temporary directory if null) */
    @JsonProperty
    private Path cacheDir;
    /** Maxiumum cache size (defaults to 50Mb) */
    @JsonProperty
    @Builder.Default
    private long cacheSize = (long) 50 * 1024 * 1024;

    /**
     * Get the timeout duration.
     *
     * @return The timeout duration
     */
    @JsonIgnore
    public Duration getTimeoutDuration() {
        return Duration.of(this.timeOut, ChronoUnit.MILLIS);
    }

    /**
     * Create an HTTP client corresponding to this configuration.
     *
     * @return The client
     *
     * @throws IOException if unable to build a cache directory
     */
    public OkHttpClient createClient() throws IOException {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(this.getTimeoutDuration())
        .readTimeout(this.getTimeoutDuration());
        if (this.cache) {
            Path dir = this.cacheDir != null ? this.cacheDir : Files.createTempDirectory("cache");
            log.debug("Cache location for {} is {}", this.baseUrl, dir);
            Cache cache = new Cache(dir.toFile(), this.cacheSize);
            builder.cache(cache);
        }
        return builder.build();
    }

    /**
     * Construct a retrofit REST client for this configuration.
     *
     * @param client The HTTP client
     * @param service The service class
     *
     * @param <S> The service class, derived from the service parameter
     *
     * @return A REST client for the supplied service class
     */
    public <S> S createRetrofitClient(OkHttpClient client, Class<S> service) {
        return new Retrofit.Builder()
        .client(client)
        .baseUrl(this.baseUrl)
        .addConverterFactory(JacksonConverterFactory.create())
        .validateEagerly(true)
        .build().create(service);
    }
}
