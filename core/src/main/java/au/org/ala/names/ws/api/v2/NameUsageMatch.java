package au.org.ala.names.ws.api.v2;

import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.Trace;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = NameUsageMatch.NameUsageMatchBuilder.class)
@Value
@Builder
@With
@EqualsAndHashCode
@Schema(
        name = "Name Search Result",
        description = "A matching taxon (or not) from a search."
)
public class NameUsageMatch {
    @Schema(
            description = "Found/not found flag. A not-found result may be because the query is ambigious",
            nullable = false
    )
    boolean success;
    @Schema(
            example = "Anas superciliosa superciliosa",
            nullable = true,
            description = "The scientific name. http://rs.tdwg.org/dwc/terms/scientificName"
    )
    String scientificName;
    @Schema(
            example = "Gmelin, 1789",
            nullable = true,
            description = "The scientific name authorship (with the scientific name, corresponds to the taxon concept). http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"
    )
    String scientificNameAuthorship;
    @Schema(
            example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
            nullable = true,
            description = "The taxon concept identifier (placement in a taxonomy). http://rs.tdwg.org/dwc/terms/taxonConceptID"
    )
    String taxonConceptID;
    @Schema(
            example = "subspecies",
            nullable = true,
            description = "The Linnaean rank (kingdom, family, species, etc.) of the taxon. http://rs.tdwg.org/dwc/terms/taxonRank"
    )
    String rank;
    @Schema(
            example = "8000",
            nullable = true,
            description = "The identifier for the Linnaean rank. http://id.ala.org.au/terms/1.0/rankID"
    )
    Integer rankID;
    @Schema(
            example = "892340",
            nullable = true,
            description = "The left-value for the position of this taxon in the taxonomic tree. http://id.ala.org.au/terms/1.0/left"
    )
    Integer lft;
    @Schema(
            example = "892345",
            nullable = true,
            description = "The right-value for the position of this taxon in the taxonomic tree. http://id.ala.org.au/terms/1.0/right"
    )
    Integer rgt;
    @Schema(
            allowableValues = "accepted,excluded,heterotypicSynonym,homotypicSynonym,incertaeSedis,inferredAccepted,inferredExcluded,inferredInvalid,inferredSynonym,inferredUnplaced,invalid,misapplied,miscellaneousLiterature,objectiveSynonym,proParteSynonym,speciesInquirenda,subjectiveSynonym,synonym,unknown,unplaced,unreviewed,unreviewedSynonym",
            example = "subjectiveSynonym",
            nullable = true,
            description = "The the taxonomic status of the name.  http://rs.tdwg.org/dwc/terms/taxonomicStatus"
    )
    String taxonomicStatus;
    @Schema(
            example = "Animalia",
            nullable = true,
            description = "The Linnaean kingdom. http://rs.tdwg.org/dwc/terms/kingdom"
    )
    String kingdom;
    @Schema(
            example = "urn:lsid:biodiversity.org.au:afd.taxon:4647863b-760d-4b59-aaa1-502c8cdf8d3c",
            nullable = true,
            description = "The kingdom identifier. http://id.ala.org.au/terms/1.0/kingdomID"
    )
    String kingdomID;
    @Schema(
            example = "Chordata",
            nullable = true,
            description = "The Linnaean phylum. http://rs.tdwg.org/dwc/terms/phylum"
    )
    String phylum;
    @Schema(
            nullable = true,
            description = "The phylum identifier. http://id.ala.org.au/terms/1.0/phylumID"
    )
    String phylumID;
    @Schema(
            example = "Aves",
            nullable = true,
            description = "The Linnaean class. http://rs.tdwg.org/dwc/terms/class"
    )
    String classs;
    @Schema(
            nullable = true,
            description = "The class identifier. http://id.ala.org.au/terms/1.0/classID"
    )
    String classID;
    @Schema(
            example = "Anseriformes",
            nullable = true,
            description = "The Linnaean order. http://rs.tdwg.org/dwc/terms/order"
    )
    String order;
    @Schema(
            nullable = true,
            description = "The order identifier. http://id.ala.org.au/terms/1.0/orderID"
    )
    String orderID;
    @Schema(
            example = "Anatidae",
            nullable = true,
            description = "The Linnaean family. http://rs.tdwg.org/dwc/terms/family"
    )
    String family;
    @Schema(
            nullable = true,
            description = "The family identifier. http://id.ala.org.au/terms/1.0/familyID"
    )
    String familyID;
    @Schema(
            example = "Anas",
            nullable = true,
            description = "The Linnaean genus. http://rs.tdwg.org/dwc/terms/genus"
    )
    String genus;
    @Schema(
            nullable = true,
            description = "The genus identifier. http://id.ala.org.au/terms/1.0/genusID"
    )
    String genusID;
    @Schema(
            example = "Osphranter rufus",
            nullable = true,
            description = "The species name. http://id.ala.org.au/terms/1.0/species"
    )
    String species;
    @Schema(
            name = "speciesID",
            nullable = true,
            description = "The species identifier. http://id.ala.org.au/terms/1.0/speciesID"
    )
    String speciesID;
    @Schema(
            example = "Red Kangaroo",
            nullable = true,
            description = "The main vernacular (common) name. http://rs.tdwg.org/dwc/terms/vernacularName"
    )
    String vernacularName;
    @Schema(
            example = "Red Kangaroo",
            nullable = true,
            description = "The location identifier, if locality information is supplied. http://rs.tdwg.org/dwc/terms/locationID"
    )
    String locationID;
    @Schema(
            example = "Victoria",
            nullable = true,
            description = "The most specific locality name, if locality information is supplied. http://rs.tdwg.org/dwc/terms/locality"
    )
    String locality;
    @Schema(
            example = "Red Kangaroo",
            nullable = true,
            description = "Location identifiers for the locations this taxon is found in. http://rs.tdwg.org/dwc/terms/locationID"
    )
    Set<String> distributionIDs;
    @Schema(
            example = "Victoria, South Australia",
            nullable = true,
            description = "The location names that this taxon is found in. http://rs.tdwg.org/dwc/terms/locality"
    )
    Set<String> distribution;
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
    List<String> speciesGroup;
    @ArraySchema(schema = @Schema(
            example = "Herbivorous Marsupials",
            nullable = true,
            description = "Species sub-groups for the taxon. http://id.ala.org.au/terms/1.0/speciesSubgroup"
    ))
    List<String> speciesSubgroup;
    @ArraySchema(schema = @Schema(
            example = "http://ala.org.au/issues/1.0/unresolvedHomonym",
            nullable = true,
            description = "Any issues with the matching process as URIs."
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
    public static NameUsageMatch forException(Exception ex, NameSearch search) {
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
        return NameUsageMatch.builder()
                .success(false)
                .issues(Collections.singletonList(message.toString()))
                .build();
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final NameUsageMatch FAIL = NameUsageMatch.builder().success(false).issues(Collections.singletonList("noMatch")).build();
}

