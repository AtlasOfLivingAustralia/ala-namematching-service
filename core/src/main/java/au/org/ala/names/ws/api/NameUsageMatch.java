package au.org.ala.names.ws.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "Name Search Result",
        description = "A matching taxon (or not) from a search."
)
public class NameUsageMatch {
    /** Field lookup based on ranks, corresponding to {@link NameSearch#HINT_ORDER} */
    private static final String[] RANK_FIELD_NAMES = {"phylum", "genus", "order", "classs", "family", "kingdom"};
    private static final Field[] RANK_FIELDS = new Field[RANK_FIELD_NAMES.length];

    {
        try {
            for (int i = 0; i < RANK_FIELD_NAMES.length; i++)
                RANK_FIELDS[i] = NameUsageMatch.class.getDeclaredField(RANK_FIELD_NAMES[i]);
        } catch (Exception ex) {
            throw new IllegalStateException("Also seriously, this is impossible!" , ex);
        }
    }

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
            allowableValues = "exactMatch,canonicalMatch,phraseMatch,fuzzyMatch,vernacularMatch,higherMatch,taxonIdMatch",
            example = "fuzzyMatch",
            nullable = true,
            description = "The type of taxon match. au.org.ala.names.model.MatchType"
    )
    String matchType;
    @Schema(
            allowableValues = "SCIENTIFIC,VIRUS,HYBRID,INFORMAL,CULTIVAR,CANDIDATUS,OTU,DOUBTFUL,PLACEHOLDER,NO_NAME",
            example = "SCIENTIFIC",
            nullable = true,
            description = "The type of supplied name. org.gbif.api.vocabulary.NameType"
    )
    String nameType;
    @Schema(
            example = "SUBJECTIVE_SYNONYM",
            nullable = true,
            description = "The type of synonymy, if the supplied name was a synonym of the matched name. au.org.ala.names.model.SynonymType"
    )
    String synonymType;
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
            example = "homonym, misappliedName",
            nullable = true,
            description = "Any issues with the matching process. A successful match will return noMatch. Otherwise a list of problems or possible problems with the match. au.org.ala.names.model.ErrorType"
    ))
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

