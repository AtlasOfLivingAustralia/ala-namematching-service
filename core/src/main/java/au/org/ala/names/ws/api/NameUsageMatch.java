package au.org.ala.names.ws.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonDeserialize(builder = NameUsageMatch.NameUsageMatchBuilder.class)
@Value
@Builder
@EqualsAndHashCode
@ApiModel(
        value = "Name Search Result",
        description = "A matching taxon (or not) from a search."
)
public class NameUsageMatch {
    /** Field lookup based on ranks, corresponding to {@link NameSearch#HINT_ORDER} */
    private static final String[] RANK_FIELD_NAMES = {"phylum", "genus", "order", "classs", "family", "kingdom"};
    private static final Field[] RANK_FIELDS = new Field[RANK_FIELD_NAMES.length];

    static {
        try {
            for (int i = 0; i < RANK_FIELD_NAMES.length; i++)
                RANK_FIELDS[i] = NameUsageMatch.class.getDeclaredField(RANK_FIELD_NAMES[i]);
        } catch (Exception ex) {
            throw new IllegalStateException("Also seriously, this is impossible!" , ex);
        }
    }

    @ApiModelProperty(
            value = "Found/not found flag. A not-found result may be because the query is ambigious",
            allowEmptyValue = false
    )
    boolean success;
    @ApiModelProperty(
            value = "The scientific name",
            example = "Anas superciliosa superciliosa",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/scientificName"
    )
    String scientificName;
    @ApiModelProperty(
            value = "The scientific name authorship (with the scientific name, corresponds to the taxon concept)",
            example = "Gmelin, 1789",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"
    )
    String scientificNameAuthorship;
    @ApiModelProperty(
            value = "The taxon concept identifier (placement in a taxonomy)",
            example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/taxonConceptID"
    )
    String taxonConceptID;
    @ApiModelProperty(
            value = "The Linnaean rank (kingdom, family, species, etc.) of the taxon",
            example = "subspecies",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/taxonRank"
    )
    String rank;
    @ApiModelProperty(
            value = "The identifier for the Linnaean rank",
            example = "8000",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/rankID"
    )
    Integer rankID;
    @ApiModelProperty(
            value = "The left-value for the position of this taxon in the taxonomic tree",
            example = "892340",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/left"
    )
    Integer lft;
    @ApiModelProperty(
            value = "The right-value for the position of this taxon in the taxonomic tree",
            example = "892345",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/right"
    )
    Integer rgt;
    @ApiModelProperty(
            value = "The type of taxon match",
            allowableValues = "exactMatch,canonicalMatch,phraseMatch,fuzzyMatch,vernacularMatch,higherMatch,taxonIdMatch",
            example = "fuzzyMatch",
            allowEmptyValue = true,
            notes = "au.org.ala.names.model.MatchType"
    )
    String matchType;
    @ApiModelProperty(
            value = "The type of supplied name",
            allowableValues = "SCIENTIFIC,VIRUS,HYBRID,INFORMAL,CULTIVAR,CANDIDATUS,OTU,DOUBTFUL,PLACEHOLDER,NO_NAME",
            example = "SCIENTIFIC",
            allowEmptyValue = true,
            notes = "org.gbif.api.vocabulary.NameType"
    )
    String nameType;
    @ApiModelProperty(
            value = "The type of synonymy, if the supplied name was a synonym of the matched name",
            example = "SUBJECTIVE_SYNONYM",
            allowEmptyValue = true,
            notes = "au.org.ala.names.model.SynonymType"
    )
    String synonymType;
    @ApiModelProperty(
            value = "The Linnaean kingdom",
            example = "Animalia",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/kingdom"
    )
    String kingdom;
    @ApiModelProperty(
            value = "The kingdom identifier",
            example = "urn:lsid:biodiversity.org.au:afd.taxon:4647863b-760d-4b59-aaa1-502c8cdf8d3c",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/kingdomID"
    )
    String kingdomID;
    @ApiModelProperty(
            value = "The Linnaean phylum",
            example = "Chordata",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/phylum"
    )
    String phylum;
    @ApiModelProperty(
            value = "The phylum identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/phylumID"
    )
    String phylumID;
    @ApiModelProperty(
            value = "The Linnaean class",
            example = "Aves",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/class"
    )
    String classs;
    @ApiModelProperty(
            value = "The class identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/classID"
    )
    String classID;
    @ApiModelProperty(
            value = "The Linnaean order",
            example = "Anseriformes",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/order"
    )
    String order;
    @ApiModelProperty(
            value = "The order identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/orderID"
    )
    String orderID;
    @ApiModelProperty(
            value = "The Linnaean family",
            example = "Anatidae",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/family"
    )
    String family;
    @ApiModelProperty(
            value = "The family identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/familyID"
    )
    String familyID;
    @ApiModelProperty(
            value = "The Linnaean genus",
            example = "Anas",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/genus"
    )
    String genus;
    @ApiModelProperty(
            value = "The genus identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/genusID"
    )
    String genusID;
    @ApiModelProperty(
            value = "The species name",
            example = "Osphranter rufus",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/species"
    )
    String species;
    @ApiModelProperty(
            value = "The species identifier",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/speciesID"
    )
    String speciesID;
    @ApiModelProperty(
            value = "The main vernacular (common) name",
            example = "Red Kangaroo",
            allowEmptyValue = true,
            notes = "http://rs.tdwg.org/dwc/terms/vernacularName"
    )
    String vernacularName;
    @ApiModelProperty(
            value = "Species groups for the taxon",
            example = "Animals, Mammals",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/speciesGroup"
    )
    List<String> speciesGroup;
    @ApiModelProperty(
            value = "Species sub-groups for the taxon",
            example = "Herbivorous Marsupials",
            allowEmptyValue = true,
            notes = "http://id.ala.org.au/terms/1.0/speciesSubgroup"
    )
    List<String> speciesSubgroup;
    @ApiModelProperty(
            value = "Any issues with the matching process. A successful match will return noMatch. Otherwise a list of problems or possible problems with the match",
            example = "homonym, misappliedName",
            allowEmptyValue = true,
            notes = "au.org.ala.names.model.ErrorType"
    )
    List<String> issues;


    /**
     * Check this result against a name search.
     * <p>
     * Only hints are checked, since matches can result in a very different taxonomy
     * to the supplied values.
     * Null values for fields mean that hints are not checked.
     * </p>
     *
     * @param search The source search
     *
     * @return True if the search matches the supplied hints.
     */
    public boolean check(NameSearch search) {
        Map<String, List<String>> hints = search.getHints();
        if (hints == null || hints.isEmpty())
            return true;
        for (int i = 0; i < NameSearch.HINT_ORDER.length; i++) {
            try {
                final String value = (String) RANK_FIELDS[i].get(this);
                if (value != null) {
                    String rank = NameSearch.HINT_ORDER[i];
                    List<String> hl = hints.get(rank);
                    if (hl != null && !hl.stream().anyMatch(h -> value.equalsIgnoreCase(h)))
                        return false;
                }
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Right now, I need to go and have a lie-down" , ex);
            }
        }
        return true;
    }


    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final NameUsageMatch FAIL = NameUsageMatch.builder().success(false).issues(Collections.singletonList("noMatch")).build();
}

