package au.org.ala.names.ws.api;

import au.org.ala.util.BasicNormaliser;
import au.org.ala.util.Normaliser;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.With;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
        "However, the name can (sometimes) be deduced from other information if not given."
)
public class NameSearch {
    /**
     * Used to normalise request strings before processing
     */
    private static final Normaliser NORMALISER = new BasicNormaliser(true, true, true, false, false, true);

    /**
     * The order in which hints are applied. Hints are tried in <em>reverse</em> order to the list.
     */
    public static final String[] HINT_ORDER = {"phylum", "genus", "order", "class", "family", "kingdom"};

    /** Quick lookup of fields/methods for hinting */
    private static final String[] HINT_FIELD_NAMES = {"phylum", "genus", "order", "clazz", "family", "kingdom"};
    private static final Field[] HINT_FIELDS;
    private static final Method[] WITH_METHODS;

    static {
        try {
            HINT_FIELDS = new Field[HINT_FIELD_NAMES.length];
            for (int i = 0; i < HINT_FIELD_NAMES.length; i++)
              HINT_FIELDS[i] = NameSearch.class.getDeclaredField(HINT_FIELD_NAMES[i]);
            WITH_METHODS = new Method[HINT_FIELD_NAMES.length];
            for (int i = 0; i < HINT_FIELD_NAMES.length; i++)
                WITH_METHODS[i] = NameSearch.class.getMethod("with" + HINT_FIELD_NAMES[i].substring(0, 1).toUpperCase() + HINT_FIELD_NAMES[i].substring(1), String.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Also seriously, this is impossible!", ex);
        }
    }

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
        example = "{ \"kingdom\": [ \"Plantae\", \"Fungi\" ] }",
        description = "Taxonomic hints. A map of Linnaean rank names onto possible values if there is difficulty looking up a name. This also acts as a sanity check on the returned results. http://rs.tdwg.org/dwc/terms/vernacularName"
    )
    private Map<String, List<String>> hints;
    @Schema(
        description = "Allow a loose search. Loose searches will treat the scientific name as a vernacular name or a taxon identifier if the name cannot be found."
    )
    private boolean loose;
    @Schema(
            example = "MATCH",
            description = "The style of search to perform. If absent, the server default style is used."
    )
    private SearchStyle style;

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
            .kingdom(NORMALISER.normalise(this.kingdom))
            .phylum(NORMALISER.normalise(this.phylum))
            .clazz(NORMALISER.normalise(this.clazz))
            .order(NORMALISER.normalise(this.order))
            .family(NORMALISER.normalise(this.family))
            .genus(NORMALISER.normalise(this.genus))
            .specificEpithet(NORMALISER.normalise(this.specificEpithet))
            .infraspecificEpithet(NORMALISER.normalise(this.infraspecificEpithet))
            .rank(NORMALISER.normalise(this.rank))
            .verbatimTaxonRank(NORMALISER.normalise(this.verbatimTaxonRank))
            .taxonConceptID(this.taxonConceptID) // Not text
            .taxonID(this.taxonID)
            .scientificNameAuthorship(NORMALISER.normalise(this.scientificNameAuthorship))
            .scientificName(NORMALISER.normalise(this.scientificName))
            .vernacularName(NORMALISER.normalise(this.vernacularName))
            .hints(
                this.hints == null ? null :
                    this.hints.keySet().stream().collect(Collectors.toMap(
                        k -> NORMALISER.normalise(k),
                        k -> this.hints.get(k).stream().map(v -> NORMALISER.normalise(v)).collect(Collectors.toList()))
                    )
            )
            .loose(this.loose)
            .style(this.style)
            .build();
    }

    /**
     * Fill out a search with inferred properties.
     * <p>
     * If the scientific name is blank, try to deduce the name and the rank.
     * </p>
     *
     * @return A copy with inferred properties, or this instance if there is no inference to be done.
     */
    public NameSearch inferred() {
        String inferredScientificName = null;
        String inferredRank = null;
        NameSearch template = this;

        if (this.scientificName != null)
            return this;
        //set the scientificName using available elements of the higher classification
        if (this.genus != null && this.specificEpithet != null && this.infraspecificEpithet != null) {
            inferredScientificName = this.genus + " " + this.specificEpithet + " " + this.infraspecificEpithet;
            inferredRank = "subspecies";
        } else if (this.genus != null && this.specificEpithet != null) {
            inferredScientificName = this.genus + " " + this.specificEpithet;
            inferredRank = "species";
        } else if (this.genus != null) {
            inferredScientificName = this.genus;
            inferredRank = "genus";
        } else if (this.family != null) {
            inferredScientificName = this.family;
            inferredRank = "family";
        } else if (this.order != null) {
            inferredScientificName = this.order;
            inferredRank = "order";
        } else if (this.clazz != null) {
            inferredScientificName = this.clazz;
            inferredRank = "class";
        } else if (this.phylum != null) {
            inferredScientificName = this.phylum;
            inferredRank = "phylum";
        } else if (this.kingdom != null) {
            inferredScientificName = this.kingdom;
            inferredRank = "kingdom";
        }
        if (inferredRank != null && template.rank == null)
            template = template.withRank(inferredRank);
        if (inferredScientificName != null && template.scientificName == null)
            template = template.withScientificName(inferredScientificName);
        return template;
    }

    /**
     * Generate a stream of hinted searches.
     * <p>
     * The hinted searches take any supplied hints and gerate a list of possible searches,
     * from the least specific to the most specific.
     * A stream is returned so that we can stop generating after finding one.
     * </p>
     * <p>
     * If there are no hints, the bare stream is returned.
     * Hints are tried in {@link #HINT_ORDER} order, since there are a few elements that can be quickly used
     * to rapidly home in on a suitable name.
     * </p>
     *
     * @return The hint stream
     */
    public Stream<NameSearch> hintStream() {
        if (this.hints == null || this.hints.isEmpty()) {
            return this.bareStream();
        }
        try {
            return this.hinted(0);
        } catch (Exception ex) {
            throw new IllegalStateException("Seriously, this is impossible!", ex);
        }
    }

    /**
     * Return a stream of possible searches without hinting.
     *
     * @return The inferred search (if different to the search) and the search itself
     */
    public Stream<NameSearch> bareStream() {
        NameSearch inferred = this.inferred();
        if (this.equals(inferred))
            return Stream.of(this);
        else
            return Stream.of(inferred, this);
    }

    /**
     * Generate the hinted stream.
     * <p>
     * Hints are applied to the current search, from the rank in the index onwards.
     * The {@link #hints} field is assumed to be non-null and non-empty.
     * </p>
     *
     * @param index The index into the rank list
     *
     * @return A stream of hinted elements
     *
     * @throws IllegalAccessException Impossible! Occurs if a field isn't found
     * @throws InvocationTargetException Inconceivable! Occurs if a method isn't found
     */
    protected Stream<NameSearch> hinted(int index) throws IllegalAccessException, InvocationTargetException {
        if (index >= HINT_ORDER.length) {
            return this.bareStream();
        }
        Stream<NameSearch> output = null;
        String rank = HINT_ORDER[index];
        Field field = HINT_FIELDS[index];
        Method with = WITH_METHODS[index];
        String value = (String) field.get(this);
        if (value == null) {
            List<String> hs = this.hints.get(rank);
            if (hs != null) {
                for (String h : hs) {
                    NameSearch template = (NameSearch) with.invoke(this, h);
                    Stream<NameSearch> hinted = template.hinted(index + 1);
                    output = output == null ? hinted : Stream.concat(output, hinted); // In order of suggestion
                }
            }
        }
        Stream<NameSearch> bare = this.hinted(index + 1);
        return output == null ? bare : Stream.concat(bare, output);
    }

    @JsonPOJOBuilder(withPrefix = "" )
    public static class NameSearchBuilder {
    }
}
