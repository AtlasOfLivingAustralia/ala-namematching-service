package au.org.ala.names.ws.api.v2;

import au.org.ala.bayesian.Normaliser;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.util.BasicNormaliser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonDeserialize(builder = LocationSearch.LocationSearchBuilder.class)
@Value
@Builder
@With
@EqualsAndHashCode
@Schema(
    name = "Location search Parameters",
    description = "A set of parameters that can be used to search for localities."
)
public class LocationSearch {
    /**
     * Used to normalise request strings before processing
     */
    private static final Normaliser NORMALISER = new BasicNormaliser("v2", true, false, true, false, false, false);

    @Schema(
            example = "http://vocab.getty.edu/tgn/7797584",
            description = "The location identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String locationID;
    @Schema(
            example = "Bedarra Island",
            description = "The name of the location. http://rs.tdwg.org/dwc/terms/locality"
    )
    private String locality;
    @Schema(
            example = "Oceania",
            description = "The continent name. http://rs.tdwg.org/dwc/terms/continent"
    )
    private String continent;
    @Schema(
            example = "Australia",
            description = "The country name. http://rs.tdwg.org/dwc/terms/country"
    )
    private String country;
    @Schema(
            example = "Queensland",
            description = "The state or province name. http://rs.tdwg.org/dwc/terms/stateProvince"
    )
    private String stateProvince;
    @Schema(
            description = "The island group. http://rs.tdwg.org/dwc/terms/islandGroup"
    )
    private String islandGroup;
    @Schema(
            example = "Bedarra Island",
            description = "The island. http://rs.tdwg.org/dwc/terms/island"
    )
    private String island;
    @Schema(
            description = "The water body, anything from an ocean to a bay. http://rs.tdwg.org/dwc/terms/waterBody"
    )
    private String waterBody;
    @Schema(
        example = "{ \"continent\": [ \"Oceania\", \"Asia\" ] }",
        description = "Location hints. A map of locality elements possible values if there is difficulty looking up a location."
    )
    private Map<String, List<String>> hints;
    @Schema(
            example = "MATCH",
            defaultValue = "MATCH",
            description = "The style of search to perform. If absent, the server default style is used."
    )
    private SearchStyle style;

    private String normalise(String s) {
        return StringUtils.trimToNull(NORMALISER.normalise(s));
    }


    /**
     * Get a version of this that has been normalised.
     * <p>
     * Normalised means no strange characters, surrounding quotes stripped
     * and everything neatly cleaned up and nice.
     * </p>
     *
     * @return A normalised copy of the search
     */
    public LocationSearch normalised() {
        return LocationSearch.builder()
                .locationID(StringUtils.trimToNull(this.locationID))
                .locality(this.normalise(this.locality))
                .continent(this.normalise(this.continent))
                .country(this.normalise(this.country))
                .stateProvince(this.normalise(this.stateProvince))
                .islandGroup(this.normalise(this.islandGroup))
                .island(this.normalise(this.island))
                .waterBody(this.normalise(this.waterBody))
                .hints(
                        this.hints == null ? null :
                                this.hints.keySet().stream().collect(Collectors.toMap(
                                        k -> this.normalise(k),
                                        k -> this.hints.get(k).stream().map(v -> this.normalise(v)).collect(Collectors.toList()))
                                )
                )
                .style(this.style)
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "" )
    public static class LocationSearchBuilder {
    }
}
