package au.org.ala.names.ws.core;

import au.org.ala.names.ALANameSearcherConfiguration;
import au.org.ala.names.ws.api.v1.SearchStyle;
import au.org.ala.ws.DataCacheConfiguration;
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
    /** The configuration to use when building the searcher */
    @JsonProperty
    private ALANameSearcherConfiguration searcher;
    /** The location of the species groups. Defaults to <code>file:///data/ala-namematching-service/config/groups.json</code> */
    @JsonProperty
    private URL groups;
    /** The location of the species subgroups. Defaults to <code>file:///data/ala-namematching-service/config/subgroups.json</code> */
    @JsonProperty
    private URL subgroups;
    /** Use hints when matching (true by default) */
    @JsonProperty
    private boolean useHints = true;
    /** Use hints to confirm the matching result (true by default) */
    @JsonProperty
    private boolean checkHints = true;
    /** Allow loose searching on taxon identifier and vernacular name in place of scientific name, if requested */
    @JsonProperty
    private boolean allowLoose = true;
    /** Use the preferred vernacular name supplied by the index, if present. Available in 2023+ namematching indexes */
    @JsonProperty
    private boolean preferredVernacular = true;
    /** The default search style to use, if not specified */
    @JsonProperty
    private SearchStyle defaultStyle = SearchStyle.MATCH;
    /** The cache configuration */
    @JsonProperty
    private DataCacheConfiguration cache = DataCacheConfiguration.builder().build();

    public NameSearchConfiguration() {
        try {
            this.groups = new URL("file:///data/ala-namematching-service/config/groups.json");
            this.subgroups = new URL("file:///data/ala-namematching-service/config/subgroups.json");
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Unable to intialise configuration", ex);
        }
    }
}
