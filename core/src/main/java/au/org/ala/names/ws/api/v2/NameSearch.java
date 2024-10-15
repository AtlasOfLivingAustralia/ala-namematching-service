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
@JsonDeserialize(builder = NameSearch.NameSearchBuilder.class)
@Value
@Builder
@With
@EqualsAndHashCode
@Schema(
    name = "Search Parameters",
    description = "A set of parameters that can be used to search for taxa. " +
        "The various entries, kingdom etc., refer to names in the Linnaean hierarchy. " +
        "The only strictly required thing is some sort of scientific name. " +
        "However, the name can (sometimes) be deduced from other information if not given. " +
        "Location information can also be given. " +
        "If location information is supplied, it is used to more accurately choose between possible matches based on expected dsitributions."
)
public class NameSearch {
    /**
     * Used to normalise request strings before processing
     */
    private static final Normaliser NORMALISER = new BasicNormaliser("v2", true, false, true, false, false, false);

    @Schema(
        example = "Animalia",
        description = "The Linnaean kingdom. http://rs.tdwg.org/dwc/terms/kingdom"
    )
    private String kingdom;
    @Schema(
        example = "Chordata",
        description = "The Linnaean phylum. http://rs.tdwg.org/dwc/terms/phylum"
    )
    private String phylum;
    @Schema(
        example = "Aves",
        description = "The Linnaean class. http://rs.tdwg.org/dwc/terms/class"
    )
    private String clazz;
    @Schema(
        example = "Anseriformes",
        description = "The Linnaean order. http://rs.tdwg.org/dwc/terms/order"
    )
    private String order;
    @Schema(
        example = "Anatidae",
        description = "The Linnaean family. http://rs.tdwg.org/dwc/terms/family"
    )
    private String family;
    @Schema(
        example = "Anas",
        description = "The Linnaean genus. http://rs.tdwg.org/dwc/terms/genus"
    )
    private String genus;
    @Schema(
        example = "superciliosa",
        description = "The species part of a Linnaean binomial name. http://rs.tdwg.org/dwc/terms/specificEptithet"
    )
    private String specificEpithet;
    @Schema(
        example = "superciliosa",
        description = "The below-species (subspecies, variety, form etc.) part of a Linnaean binomial name. http://rs.tdwg.org/dwc/terms/infraspecificEptithet"
    )
    private String infraspecificEpithet;
    @Schema(
        example = "subspecies",
        description = "The Linnaean rank of the expected result. http://rs.tdwg.org/dwc/terms/taxonRank"
    )
    private String rank;
    @Schema(
        example = "SubSpecies",
        description = "The Linnaean rank of the expected result, as supplied. http://rs.tdwg.org/dwc/terms/verbatimTaxonRank"
    )
    private String verbatimTaxonRank;
    @Schema(
        example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
        description = "The expected taxon concept (placement in a taxonomy). http://rs.tdwg.org/dwc/terms/taxonConceptID"
    )
    private String taxonConceptID;
    @Schema(
        example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
        description = "The expected taxon identifier. http://rs.tdwg.org/dwc/terms/taxonID"
    )
    private String taxonID;
    @Schema(
        example = "Gmelin, 1789",
        description = "The scientific name authorship (with the scientific name, corresponds to the taxon concept). http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"
    )
    private String scientificNameAuthorship;
    @Schema(
        example = "Anas superciliosa superciliosa",
        description = "The scientific name. http://rs.tdwg.org/dwc/terms/scientificName"
    )
    private String scientificName;
    @Schema(
        example = "Grey Duck",
        description = "The vernacular name. http://rs.tdwg.org/dwc/terms/vernacularName"
    )
    private String vernacularName;
    @Schema(
            description = "Any location information. http://rs.tdwg.org/dwc/terms/Location"
    )
    private LocationSearch location;
    @Schema(
        example = "{ \"kingdom\": [ \"Plantae\", \"Fungi\" ] }",
        description = "Taxonomic hints. A map of Linnaean rank names onto possible values if there is difficulty looking up a name. This also acts as a sanity check on the returned results."
    )
    private Map<String, List<String>> hints;
    @Schema(
        description = "Allow a loose search. Loose searches will treat the scientific name as a vernacular name or a taxon identifier if the name cannot be found."
    )
    private boolean loose;
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
    public NameSearch normalised() {
        return NameSearch.builder()
                .kingdom(this.normalise(this.kingdom))
                .phylum(this.normalise(this.phylum))
                .clazz(this.normalise(this.clazz))
                .order(this.normalise(this.order))
                .family(this.normalise(this.family))
                .genus(this.normalise(this.genus))
                .specificEpithet(this.normalise(this.specificEpithet))
                .infraspecificEpithet(this.normalise(this.infraspecificEpithet))
                .rank(this.normalise(this.rank))
                .verbatimTaxonRank(this.normalise(this.verbatimTaxonRank))
                .taxonConceptID(this.taxonConceptID) // Not text
                .taxonID(this.taxonID)
                .scientificNameAuthorship(this.normalise(this.scientificNameAuthorship))
                .scientificName(this.normalise(this.scientificName))
                .vernacularName(this.normalise(this.vernacularName))
                .location(this.location == null ? null : this.location.normalised())
                .hints(
                        this.hints == null ? null :
                                this.hints.keySet().stream().collect(Collectors.toMap(
                                        k -> this.normalise(k),
                                        k -> this.hints.get(k).stream().map(v -> this.normalise(v)).collect(Collectors.toList()))
                                )
                )
                .loose(this.loose)
                .style(this.style)
                .build();
    }

    @JsonPOJOBuilder(withPrefix = "" )
    public static class NameSearchBuilder {
    }
}
