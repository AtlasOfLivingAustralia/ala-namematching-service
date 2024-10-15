package au.org.ala.names.ws.api.v2;

import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.Trace;
import au.org.ala.vocab.GeographyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = LocationMatch.NameUsageMatchBuilder.class)
@Value
@Builder
@With
@EqualsAndHashCode
@Schema(
        name = "Location Search Result",
        description = "A matching location (or not) from a search."
)
public class LocationMatch {
    @Schema(
            description = "Found/not found flag. A not-found result may be because the query is ambigious",
            nullable = false
    )
    boolean success;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7797584",
            nullable = true,
            description = "The location identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    String locationID;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7797584, http://vocab.getty.edu/tgn/7001830, http://vocab.getty.edu/tgn/7000490",
            nullable = true,
            description = "The complete set of location identifiers associated with this locality. http://rs.tdwg.org/dwc/terms/locationID"
    )
    Set<String> locationIDs;
    @Schema(
            example = "Bedarra Island",
            nullable = true,
            description = "The most specific locality name. http://rs.tdwg.org/dwc/terms/locality"
    )
    String locality;
    @Schema(
            example = "http://vocab.getty.edu/tgn/1000006",
            description = "The continent identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String continentID;
    @Schema(
            example = "Oceania",
            description = "The continent name. http://rs.tdwg.org/dwc/terms/continent"
    )
    private String continent;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7000490",
            description = "The country identifier. http://rs.tdwg.org/dwc/terms/country"
    )
    private String countryID;
    @Schema(
            example = "Australia",
            description = "The country name. http://rs.tdwg.org/dwc/terms/country"
    )
    private String country;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7001830",
            description = "The state or province identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String stateProvinceID;
    @Schema(
            example = "Queensland",
            description = "The state or province name. http://rs.tdwg.org/dwc/terms/stateProvince"
    )
    private String stateProvince;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7855007",
            description = "The island group identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String islandGroupID;
    @Schema(
            example = "Family Islands",
            description = "The island group. http://rs.tdwg.org/dwc/terms/islandGroup"
    )
    private String islandGroup;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7797584",
            description = "The island identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String islandID;
    @Schema(
            example = "Bedarra Island",
            description = "The island. http://rs.tdwg.org/dwc/terms/island"
    )
    private String island;
    @Schema(
            example = "http://vocab.getty.edu/tgn/7002114",
            description = "The water body identifier. http://rs.tdwg.org/dwc/terms/locationID"
    )
    private String waterBodyID;
    @Schema(
            example = "Coral Sea",
            description = "The water body, anything from an ocean to a bay. http://rs.tdwg.org/dwc/terms/waterBody"
    )
    private String waterBody;
    @Schema(
            example = "waterBody",
            description = "TThe type of geographical feature, from http://ala.org.au/vocabulary/1.0/geographyType"
    )
    private GeographyType geographyType;
    @Schema(
            example = "0.9986",
            nullable = true,
            description = "The probability that this match is a correct match out of the possible candidates."
    )
    Double probability;
    @Schema(
            example = "0.54",
            nullable = true,
            description = "A measure of the closeness of the resulting match to the supplied information."
    )
    Double fidelity;
    @ArraySchema(schema = @Schema(
            example = "Animals, Mammals",
            nullable = true,
            description = "Species groups for the taxon. http://id.ala.org.au/terms/1.0/speciesGroup"
    ))
    List<String> issues;
    @ArraySchema(schema = @Schema(
            nullable = true,
            description = "A trace showing the matching process"
    ))
    Trace trace;
    @ArraySchema(schema = @Schema(
            nullable = true,
            description = "Metrics about the match performance"
    ))
    MatchMeasurement metrics;


    /**
     * Return information about the error that has occurred.
     *
     * @param ex The error
     * @param search The search that caused the error (may be null)
     *
     * @return An error match.
     */
    public static LocationMatch forException(Exception ex, LocationSearch search) {
        StringBuffer message = new StringBuffer();

        message.append("Internal error ");
        message.append(ex.getClass());
        if (ex.getMessage() != null) {
            message.append(": ");
            message.append(ex.getMessage());
        }
        if (search != null) {
            message.append(" for ");
            message.append(search);
        }
        return LocationMatch.builder()
                .success(false)
                .issues(Collections.singletonList(message.toString()))
                .build();
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final LocationMatch FAIL = LocationMatch.builder().success(false).issues(Collections.singletonList("noMatch")).build();
}

