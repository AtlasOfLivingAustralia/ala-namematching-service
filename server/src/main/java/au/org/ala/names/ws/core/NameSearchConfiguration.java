package au.org.ala.names.ws.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Configuration for name search operations.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@EqualsAndHashCode
public class NameSearchConfiguration {
    /** The path to the name matching index. Defaults to <code>/data/lucene/namematching</code> */
    @JsonProperty
    private String index = "/data/lucene/namematching";
    /** The locatioon of the species groups. Defaults to <code>file:///data/ala-namematching-service/config/groups.json</code> */
    @JsonProperty
    private URL groups;
    /** The location of the species subgroups. Defaults to <code>file:///data/ala-namematching-service/config/groups.json</code> */
    @JsonProperty
    private URL subgroups;
    /** The cache configuration */
    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    public NameSearchConfiguration() {
        try {
            this.groups = new URL("file:///data/ala-namematching-service/config/groups.json");
            this.subgroups = new URL("file:///data/ala-namematching-service/config/subgroups.json");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Unable to intialise configuration", ex);
        }
    }
}
