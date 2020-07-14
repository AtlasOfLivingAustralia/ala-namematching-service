package au.org.ala.names.ws;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Configuration for name search operations.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class NameSearchConfiguration {
    /** The path to the name matching index. Defaults to <code>/data/lucene/namematching</code> */
    @JsonProperty
    private String index = "/data/lucene/namematching";
    /** The path to the species groups. Defaults to <code>/data/ala-namematching-service/config/groups.json</code> */
    @JsonProperty
    private String groups = "/data/ala-namematching-service/config/groups.json";
    /** The path to the species subgroups. Defaults to <code>/data/ala-namematching-service/config/groups.json</code> */
    @JsonProperty
    private String subgroups = "/data/ala-namematching-service/config/subgroups.json";
    /** The cache configuration */
    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();
}
