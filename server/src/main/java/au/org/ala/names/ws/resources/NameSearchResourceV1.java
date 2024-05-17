package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.*;
import au.org.ala.bayesian.Observable;
import au.org.ala.names.*;
import au.org.ala.vocab.TaxonomicStatus;
import au.org.ala.names.ws.api.v1.NameMatchService;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.api.v1.SearchStyle;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import old.au.org.ala.names.model.ErrorType;
import old.au.org.ala.names.model.MatchType;
import old.au.org.ala.names.model.SynonymType;
import org.apache.commons.lang3.StringUtils;
import org.cache2k.Cache;
import org.cache2k.operation.CacheControl;
import org.cache2k.operation.CacheStatistics;
import org.gbif.dwc.terms.Term;
import org.gbif.nameparser.api.NameType;
import org.gbif.nameparser.api.Rank;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource that implements the name search API.
 * <p>
 * TODO add diagnostics to payload - similar to GBIF
 */
@Tag(
        name = "Taxonomy search",
        description = "Search for taxonomic information on names, classifications or identifiers"
)
@Produces(MediaType.APPLICATION_JSON)
@Path("/v1/api")
@Slf4j
@Singleton
public class NameSearchResourceV1 implements NameMatchService {
    /**
     * Searcher for names
     */
    private final ALANameSearcher searcher;
    /**
     * Rank name to value analysis
     */
    private final RankAnalysis rankAnalysis = new RankAnalysis();
    /**
     * Map taxa onto species groups
     */
    private final SpeciesGroupsUtil speciesGroupsUtil;
    /**
     * Use hints to guide search
     */
    private final boolean useHints;
    /**
     * Use hints to check search
     */
    private final boolean checkHints;
    /**
     * Allow loose searched
     */
    private final boolean allowLoose;
    /**
     * Use the index-supplied preferred vernacular name
     */
    private final boolean preferredVernacular;
    /**
     * Default search style
     */
    private final SearchStyle defaultStyle;

    // Cache2k instance for searches
    private final Cache<NameSearch, NameUsageMatch> searchCache;
    // Cache2k instance for lookups
    private final Cache<String, NameUsageMatch> idCache;
    // Cache2k instance for derefereced lookups
    private final Cache<String, NameUsageMatch> idAcceptedCache;

    /**
     * Search style map
     */
    private static final Map<SearchStyle, MatchOptions> SEARCH_STYLE_MAP = Map.of(
            SearchStyle.STRICT, MatchOptions.NONE,
            SearchStyle.FUZZY, MatchOptions.NONE.withFuzzyDerivations(true).withCanonicalDerivations(true).withModifyConsistency(true).withUseHints(true),
            SearchStyle.MATCH, MatchOptions.ALL
    );

    private static final Map<Term, ErrorType> ERROR_MAP = Collections.unmodifiableMap(buildErrorMap());

    private static final Map<Term, MatchType> MATCH_TYPE_MAP = Collections.unmodifiableMap(buildMatchTypeMap());

    private static final Map<TaxonomicStatus, SynonymType> SYNONYM_TYPE_MAP = Collections.unmodifiableMap(buildSynonymTypeMap());

    private static final List<MatchType> MATCH_TYPE_ORDER = Arrays.asList(
            MatchType.RECURSIVE,
            MatchType.VERNACULAR,
            MatchType.SOUNDEX,
            MatchType.PHRASE,
            MatchType.CANONICAL,
            MatchType.EXACT
    );

    private static final Map<String, Observable<String>> HINT_OBSERVABLE_MAP = Collections.unmodifiableMap(buildHintObservableMap());

    private static Map<Term, ErrorType> buildErrorMap() {
        Map<Term, ErrorType> errorMap = new HashMap<>();
        // errorMap.put(AlaLinnaeanFactory.ACCEPTED_AND_SYNONYM, "");
        errorMap.put(AlaLinnaeanFactory.AFFINITY_SPECIES_NAME, ErrorType.AFFINITY_SPECIES);
        // errorMap.put(AlaLinnaeanFactory.BARE_PHRASE_NAME, "");
        // errorMap.put(AlaLinnaeanFactory.CANONICAL_NAME, "");
        errorMap.put(AlaLinnaeanFactory.CONFER_SPECIES_NAME, ErrorType.CONFER_SPECIES);
        errorMap.put(AlaLinnaeanFactory.EXCLUDED_NAME, ErrorType.EXCLUDED);
        // errorMap.put(AlaLinnaeanFactory.EXPANDED_KINGDOM, "");
        // errorMap.put(AlaLinnaeanFactory.HIGHER_ORDER_MATCH, "");
        errorMap.put(AlaLinnaeanFactory.INDETERMINATE_NAME, ErrorType.INDETERMINATE_SPECIES);
        // errorMap.put(AlaLinnaeanFactory.INFERRED_KINGDOM, "");
        // errorMap.put(AlaLinnaeanFactory.INFERRED_NOMENCLATURAL_CODE, "");
        errorMap.put(AlaLinnaeanFactory.INVALID_KINGDOM, ErrorType.GENERIC);
        errorMap.put(AlaLinnaeanFactory.LOCATION_OUT_OF_SCOPE, ErrorType.GENERIC);
        errorMap.put(AlaLinnaeanFactory.MISAPPLIED_NAME, ErrorType.MISAPPLIED);
        // errorMap.put(AlaLinnaeanFactory.MISSPELLED_SCIENTIFIC_NAME, "");
        // errorMap.put(AlaLinnaeanFactory.MULTIPLE_MATCHES, "");
        errorMap.put(AlaLinnaeanFactory.PARENT_CHILD_SYNONYM, ErrorType.PARENT_CHILD_SYNONYM);
        errorMap.put(AlaLinnaeanFactory.PARTIALLY_EXCLUDED_NAME, ErrorType.ASSOCIATED_EXCLUDED);
        errorMap.put(AlaLinnaeanFactory.PARTIALLY_MISAPPLIED_NAME, ErrorType.MATCH_MISAPPLIED);
        // errorMap.put(AlaLinnaeanFactory.REMOVED_AUTHORSHIP, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_CLASS, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_CULTIVAR, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_FAMILY, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_KINGDOM, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_LOCATION, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_ORDER, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_PHRASENAME, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_PHYLUM, "");
        // errorMap.put(AlaLinnaeanFactory.REMOVED_RANK, "");
        errorMap.put(AlaLinnaeanFactory.UNPARSABLE_NAME, ErrorType.GENERIC);
        errorMap.put(AlaLinnaeanFactory.UNRESOLVED_HOMONYM, ErrorType.HOMONYM);
        // errorMap.put(AlaLinnaeanFactory.VERNACULAR_MATCH, "");
        return errorMap;
    }

    private static Map<Term, MatchType> buildMatchTypeMap() {
        Map<Term, MatchType> matchTypeMap = new HashMap<>();
        // matchTypeMap.put(AlaLinnaeanFactory.ACCEPTED_AND_SYNONYM, "");
        // matchTypeMap.put(AlaLinnaeanFactory.AFFINITY_SPECIES_NAME, "");
        matchTypeMap.put(AlaLinnaeanFactory.BARE_PHRASE_NAME, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.CANONICAL_NAME, MatchType.CANONICAL);
        // matchTypeMap.put(AlaLinnaeanFactory.CONFER_SPECIES_NAME, "");
        // matchTypeMap.put(AlaLinnaeanFactory.EXCLUDED_NAME, "");
        matchTypeMap.put(AlaLinnaeanFactory.EXPANDED_KINGDOM, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.HIGHER_ORDER_MATCH, MatchType.RECURSIVE);
        // matchTypeMap.put(AlaLinnaeanFactory.INDETERMINATE_NAME, "");
        matchTypeMap.put(AlaLinnaeanFactory.INFERRED_KINGDOM, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.INFERRED_NOMENCLATURAL_CODE, MatchType.CANONICAL);
        // matchTypeMap.put(AlaLinnaeanFactory.INVALID_KINGDOM, "");
        matchTypeMap.put(AlaLinnaeanFactory.LOCATION_OUT_OF_SCOPE, MatchType.CANONICAL);
        // matchTypeMap.put(AlaLinnaeanFactory.MISAPPLIED_NAME, "");
        matchTypeMap.put(AlaLinnaeanFactory.MISSPELLED_SCIENTIFIC_NAME, MatchType.SOUNDEX);
        // matchTypeMap.put(AlaLinnaeanFactory.MULTIPLE_MATCHES, "");
        // matchTypeMap.put(AlaLinnaeanFactory.PARENT_CHILD_SYNONYM, "");
        // matchTypeMap.put(AlaLinnaeanFactory.PARTIALLY_EXCLUDED_NAME, "");
        // matchTypeMap.put(AlaLinnaeanFactory.PARTIALLY_MISAPPLIED_NAME, "");
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_AUTHORSHIP, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_CLASS, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_CULTIVAR, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_FAMILY, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_KINGDOM, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_LOCATION, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_ORDER, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_PHRASENAME, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_PHYLUM, MatchType.CANONICAL);
        matchTypeMap.put(AlaLinnaeanFactory.REMOVED_RANK, MatchType.CANONICAL);
        // matchTypeMap.put(AlaLinnaeanFactory.UNPARSABLE_NAME, "");
        // matchTypeMap.put(AlaLinnaeanFactory.UNRESOLVED_HOMONYM, "");
        matchTypeMap.put(AlaLinnaeanFactory.VERNACULAR_MATCH, MatchType.VERNACULAR);
        return matchTypeMap;
    }


    private static Map<TaxonomicStatus, SynonymType> buildSynonymTypeMap() {
        Map<TaxonomicStatus, SynonymType> synonymTypeMap = new HashMap<>();
        synonymTypeMap.put(TaxonomicStatus.synonym, SynonymType.SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.unknown, SynonymType.SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.heterotypicSynonym, SynonymType.SUBJECTIVE_SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.homotypicSynonym, SynonymType.OBJECTIVE_SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.inferredSynonym, SynonymType.SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.accepted, null);
        synonymTypeMap.put(TaxonomicStatus.excluded, SynonymType.EXCLUDES);
        synonymTypeMap.put(TaxonomicStatus.incertaeSedis, SynonymType.INCLUDES_INCERTAE_SEDIS);
        synonymTypeMap.put(TaxonomicStatus.inferredAccepted, null);
        synonymTypeMap.put(TaxonomicStatus.inferredExcluded, SynonymType.EXCLUDES);
        synonymTypeMap.put(TaxonomicStatus.inferredInvalid, SynonymType.INVALID);
        synonymTypeMap.put(TaxonomicStatus.inferredUnplaced, SynonymType.UNPLACED);
        synonymTypeMap.put(TaxonomicStatus.invalid, SynonymType.INVALID);
        synonymTypeMap.put(TaxonomicStatus.misapplied, SynonymType.MISAPPLIED);
        synonymTypeMap.put(TaxonomicStatus.miscellaneousLiterature, SynonymType.MISC_LITERATURE);
        synonymTypeMap.put(TaxonomicStatus.objectiveSynonym, SynonymType.OBJECTIVE_SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.proParteSynonym, SynonymType.PRO_PARTE_SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.unknown, null);
        synonymTypeMap.put(TaxonomicStatus.speciesInquirenda, SynonymType.INCLUDES_SP_INQUIRENDA);
        synonymTypeMap.put(TaxonomicStatus.subjectiveSynonym, SynonymType.SUBJECTIVE_SYNONYM);
        synonymTypeMap.put(TaxonomicStatus.unplaced, SynonymType.UNPLACED);
        synonymTypeMap.put(TaxonomicStatus.unreviewed, null);
        synonymTypeMap.put(TaxonomicStatus.unreviewedSynonym, SynonymType.SYNONYM);
        return synonymTypeMap;
    }

    private static Map<String, Observable<String>> buildHintObservableMap() {
        Map<String, Observable<String>> hoMap = new HashMap<>();
        for (Observable<?> observable: AlaLinnaeanFactory.OBSERVABLES) {
            if (observable.getType() == String.class)
                hoMap.put(observable.getTerm().simpleName(), (Observable<String>) observable);
        }
        return hoMap;
    }


    @Inject
    public NameSearchResourceV1(NameSearchConfiguration configuration) {
        try {
            log.info("Initialising NameSearchResource.....");
            this.searcher = new ALANameSearcher(configuration.getSearcher());
            this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(configuration, this.searcher);
            this.useHints = configuration.isUseHints();
            this.checkHints = configuration.isCheckHints();
            this.allowLoose = configuration.isAllowLoose();
            this.preferredVernacular = configuration.isPreferredVernacular();
            this.defaultStyle = configuration.getDefaultStyle();
            this.searchCache = configuration.getCache().cacheBuilder(NameSearch.class, NameUsageMatch.class)
                    .loader(nameSearch -> this.search(nameSearch)) //auto populating function
                    .build();
            this.idCache = configuration.getCache().cacheBuilder(String.class, NameUsageMatch.class)
                    .loader(id -> this.lookup(id, false)) //auto populating function
                    .build();
            this.idAcceptedCache = configuration.getCache().cacheBuilder(String.class, NameUsageMatch.class)
                    .loader(id -> this.lookup(id, true)) //auto populating function
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Unable to initialise searcher: " + e.getMessage(), e);
        }
    }

    /**
     * Make sure that the system is still operating.
     *
     * @return True if things can still be found, the species groups are still working, etc.
     */
    public boolean check() {
        try {
            AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
            classification.scientificName = "Animalia";
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification, MatchOptions.NONE);
            if (!match.isValid())
                return false;
            this.speciesGroupsUtil.getSpeciesGroups(1);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Operation(
            summary = "Search by full classification",
            description = "Search based on a partially filled out classification. " +
                    "The search will use the parameters contained in the body to perform as precise a search as is possible."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @RequestBody(description = "Partially filled out classification", content = @Content(schema = @Schema(implementation = NameSearch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch match(NameSearch search) {
        try {
            return this.searchCache.get(search);
        } catch (Exception ex) {
            log.warn("Problem matching name : " + ex.getMessage() + " with nameSearch: " + search, ex);
            return NameUsageMatch.forException(ex, search);
        }
    }


    @Operation(
            summary = "Bulk search by full classification",
            description = "Search based on a list of partially filled out classifications. " +
                    "The result is a list of matches. " +
                    "Nulls are allowed in the list of searches. " +
                    "If a null is present, then no search is conducted and a null returned. " +
                    "This allows a client to send a partially cached list of " +
                    "requests to the server and just get matches on the specific " +
                    "elements needed."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NameUsageMatch.class)), mediaType = MediaType.APPLICATION_JSON))
    @RequestBody(description = "List of partially filled out classifications", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NameSearch.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("searchAllByClassification")
    public List<NameUsageMatch> matchAll(List<NameSearch> search) {
        return search.stream().map(s -> s == null ? null : this.match(s)).collect(Collectors.toList());
    }

    @Operation(
            summary = "Search by full classification via query parameters",
            description = "Search based on a partially filled out classification. " +
                    "The search will use the parameters supplied to perform as precise a search as is possible."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch match(
            @Parameter(description = "The scientific name. If not supplied, inferred from other parameters", example = "Dentimitrella austrina") @QueryParam("scientificName") String scientificName,
            @Parameter(description = "The kingdom name", example = "Animalia") @QueryParam("kingdom") String kingdom,
            @Parameter(description = "The phylum name") @QueryParam("phylum") String phylum,
            @Parameter(description = "The class name") @QueryParam("class") String clazz,
            @Parameter(description = "The order name") @QueryParam("order") String order,
            @Parameter(description = "The family name", example = "Columbellidae") @QueryParam("family") String family,
            @Parameter(description = "The genus name") @QueryParam("genus") String genus,
            @Parameter(description = "The specific epithet, the species part of a binomial name") @QueryParam("specificEpithet") String specificEpithet,
            @Parameter(description = "The below species (subspecies, variety, form etc.) epithet") @QueryParam("infraspecificEpithet") String infraspecificEpithet,
            @Parameter(description = "The taxon rank. If not supplied, it may be inferred from other parameters", example = "species") @QueryParam("rank") String rank,
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style
    ) {
        style = style == null ? this.defaultStyle : style;
        NameSearch search = NameSearch.builder()
                .scientificName(scientificName)
                .kingdom(kingdom)
                .phylum(phylum)
                .clazz(clazz)
                .order(order)
                .family(family)
                .genus(genus)
                .specificEpithet(specificEpithet)
                .infraspecificEpithet(infraspecificEpithet)
                .rank(rank)
                .loose(style.isLoose())
                .style(style)
                .build();
        try {
            return this.searchCache.get(search);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + search);
            return NameUsageMatch.forException(e, search);
        }
    }

    @Operation(
            summary = "Search by name",
            description = "A simple search based only on scientific name. " +
                    "The search will not be able to resolve complications, such as homonyms."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/search")
    public NameUsageMatch match(
            @Parameter(description = "The scientific name", required = true, example = "Acacia dealbata") @QueryParam("q") String name,
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style
    ) {
        style = style == null ? this.defaultStyle : style;
        NameSearch search = NameSearch.builder().scientificName(name).loose(true).style(style).build();
        try {
            return this.searchCache.get(search);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with query: " + name);
            return NameUsageMatch.forException(e, search);
        }
    }


    @Operation(
            summary = "Get taxon information by by vernacular (common) name.",
            description = "The same Vernacular name may be given to multiple taxa with different scientific names. The result returned is a best-effort match."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByVernacularName")
    public NameUsageMatch matchVernacular(
            @Parameter(description = "The common name", required = true, example = "Red Kangaroo") @QueryParam("vernacularName") String vernacularName
    ) {
        NameSearch search = NameSearch.builder().vernacularName(vernacularName).build();
        try {
            return this.searchCache.get(search);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with vernacularName: " + vernacularName);
            return NameUsageMatch.forException(e, search);
        }
    }

    @Operation(
            summary = "Get taxon information by taxon identifier."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getByTaxonID")
    public NameUsageMatch get(
            @Parameter(description = "The unique taxon identifier", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("taxonID") String taxonID,
            @Parameter(description = "Follow synonyms to the accepted taxon") @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        try {
            Cache<String, NameUsageMatch> cache = follow ? this.idAcceptedCache : this.idCache;
            return cache.get(taxonID);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with taxonID: " + taxonID);
            return NameUsageMatch.forException(e, null);
        }
    }

    @Operation(
            summary = "Get bulk taxon information by a list of taxon identifiers."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = NameUsageMatch.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getAllByTaxonID")
    public List<NameUsageMatch> getAll(
            @Parameter(description = "The list of unique taxon identifiers", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("taxonIDs") List<String> taxonIDs,
            @Parameter(description = "Follow synonyms to the accepted taxon") @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        List<NameUsageMatch> matches = new ArrayList<>(taxonIDs.size());
        Cache<String, NameUsageMatch> cache = follow ? this.idAcceptedCache : this.idCache;
        for (String taxonID : taxonIDs) {
            NameUsageMatch match = NameUsageMatch.FAIL;
            try {
                match = cache.get(taxonID);
            } catch (Exception e) {
                log.warn("Problem matching name : " + e.getMessage() + " with taxonID: " + taxonID);
                match = NameUsageMatch.forException(e, null);
            }
            matches.add(match);
        }
        return matches;
    }

    @Operation(
            summary = "Get the taxon scientific name by taxon identifier."
    )
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN))
    @Timed
    @Path("/getNameByTaxonID")
    public String getName(
            @Parameter(description = "The unique taxon identifier", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("taxonID") String taxonID,
            @Parameter(description = "Follow synonyms to the accepted taxon") @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        try {
            Cache<String, NameUsageMatch> cache = follow ? this.idAcceptedCache : this.idCache;
            NameUsageMatch match = cache.get(taxonID);
            return match != null && match.isSuccess() ? match.getScientificName() : null;
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with taxonID: " + taxonID);
        }
        return null;
    }

    @Operation(
            summary = "Get bulk taxon scientific names from a list of taxon identifiers."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getAllNamesByTaxonID")
    public List<String> getAllNames(
            @Parameter(description = "The list of unique taxon identifiers", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("taxonIDs") List<String> taxonIDs,
            @Parameter(description = "Follow synonyms to the accepted taxon", required = false) @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        return taxonIDs.stream().map(id -> this.getName(id, follow)).collect(Collectors.toList());
    }

    @Operation(
            summary = "Check a name/rank combination and see if it is valid.",
            description = "Returns true if the result is valuid, false if not and null (empty) if unable to check because of an error (usually something like a homonym)"
    )
    @ApiResponse(responseCode = "204", description = "Unable to check due to search error")
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(examples = @ExampleObject(value = "true")))
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Timed
    @Path("/check")
    public Boolean check(
            @Parameter(description = "The scientific name", required = true, example = "Animalia") @QueryParam("name") String name,
            @Parameter(description = "The Linnaean rank", required = true, example = "kingdom") @QueryParam("rank") String rank
    ) {
        if (name == null || rank == null)
            return false;
        Rank rk = rankAnalysis.fromString(rank, null);
        if (rk == null)
            throw new IllegalArgumentException("No matching rank for " + rank);
        try {
            AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
            classification.scientificName = name;
            classification.taxonRank = rk;
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification, MatchOptions.NONE);
            return match.isValid();
        } catch (BayesianException ex) {
            log.debug("Error searching for " + name + " and rank " + rk + " " + ex.getMessage());
            return null;
        }
    }

    @Operation(
            summary = "Autocomplete search with the beginning of a scientific or common name.",
            description = "Returns a list of matches. Up to 2 * max matches are returned."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Map.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/autocomplete")
    public List<Map> autocomplete(
            @Parameter(description = "The query", required = true, example = "eucalypt")
            @QueryParam("q") String query,

            @Parameter(description = "Maximum results to return")
            @QueryParam("max") @DefaultValue("10") Integer max,

            @Parameter(description = "Include synonyms")
            @QueryParam("includeSynonyms") @DefaultValue("true") Boolean includeSynonyms) {

        try {
            List<Autocomplete> autocomplete = this.searcher.autocomplete(query, max, includeSynonyms);
            List<Map> mapped = autocomplete.stream().map(Autocomplete::asMap).collect(Collectors.toList());
            return mapped;
        } catch (BayesianException ex) {
            log.warn("Unable to match autocomplete query " + query, ex);
            return Collections.emptyList();
        }
    }

    @Operation(
            summary = "Search for an LSID by ID"
    )
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN))
    @Timed
    @Path("/searchForLsidById")
    public String searchForLsidById(
            @Parameter(description = "The ID", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("id") String id
    ) {
        try {
            Match<AlaLinnaeanClassification, MatchMeasurement> m = this.searcher.search(id);
            return m.isValid() ? m.getAccepted().taxonId : null;
        } catch (BayesianException ex) {
            log.warn("Error looking for taxon id " + id, ex);
            return null;
        }
    }

    @Operation(
            summary = "Search for an LSID with a scientific name."
    )
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = String.class), mediaType = MediaType.TEXT_PLAIN))
    @Timed
    @Path("/searchForLSID")
    public String searchForLSID(
            @Parameter(description = "The name", required = true, example = "Acacia dealbata") @QueryParam("name") String name
    ) {
        try {
            AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
            classification.scientificName = name;
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.searcher.search(classification, MatchOptions.NONE);
            if (match.isValid())
                return match.getAccepted().taxonId;
        } catch (BayesianException ex) {
            log.warn("Problem matching LSID for name: " + name, ex);
        }
        return null;
    }

    @Operation(
            summary = "Search for a list of LSIDs with a list of scientificName or scientificName(kingdom)."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)), mediaType = MediaType.APPLICATION_JSON))
    @RequestBody(description = "List of taxa", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getGuidsForTaxa")
    public List<String> getGuidsForTaxa(List<String> taxa) {
        try {
            return taxa.stream().map(this::searchForLSID).collect(Collectors.toList());
        } catch (Exception ex) {
            log.warn("Problem matching guids with taxa: " + taxa, ex);
        }
        return Collections.emptyList();
    }

    @Operation(
            summary = "Get taxon information by by vernacular (common) name.",
            description = "The same Vernacular name may be given to multiple taxa with different scientific names. The result returned is a best-effort match."
    )
    @GET
    @Timed
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = String.class)), mediaType = MediaType.APPLICATION_JSON))
    @Path("/getCommonNamesForLSID")
    public Set<String> getCommonNamesForLSID(
            @Parameter(required = true, example = "Red Kangaroo") @QueryParam("lsid") String lsid,
            @Parameter(required = true, example = "10") @QueryParam("max") Integer max
    ) {
        try {
            Set<String> names = new HashSet<>(max);
            for (String name : this.searcher.getVernacularNames(lsid)) {
                if (names.size() >= max)
                    break;
                names.add(name);
            }
            return names;
        } catch (Exception ex) {
            log.warn("Problem gettting vernacular names for " + lsid, ex);
        }
        return Collections.emptySet();
    }

    private boolean possibleMatch(Match<AlaLinnaeanClassification, MatchMeasurement> match) {
        return match != null && ((match.isValid() || match.getIssues().contains(AlaLinnaeanFactory.UNRESOLVED_HOMONYM)));
    }

    /**
     * Perform a search based on a classification built from various calls.
     *
     * @param search The search classification
     * @return A match object, with success=false if there was no valid match
     * @throws Exception if something goes horribly wrong
     */
    private NameUsageMatch search(NameSearch search) throws Exception {
        NameUsageMatch match = null;
        SearchStyle style = search.getStyle() == null ? this.defaultStyle : search.getStyle();
        //attempt 1: search via taxonConceptID or taxonID if provided
        Optional<Match<AlaLinnaeanClassification, MatchMeasurement>> result = Optional.empty();

        if (search.getTaxonConceptID() != null) {
            result = Optional.ofNullable(this.searcher.search(search.getTaxonConceptID()));
        } else if (search.getTaxonID() != null) {
            result = Optional.ofNullable(this.searcher.search(search.getTaxonID()));
        }

        if (result.isPresent()) {
            return create(result.get(), null, true);
        }
        // Start searching by names
        final NameSearch nsearch = search.normalised();

        // Get the first result that works with hints
        if (useHints) {
            result = nsearch.bareStream()
                    .map(s -> this.findMatch(s, SearchStyle.STRICT, true))
                    .filter(m -> m != null && m.isValid())
                    .findFirst();
        }

        // Look for an exact match
        if (!result.isPresent()) {
            result = nsearch.bareStream()
                    .filter(s -> !s.equals(nsearch))
                    .map(s -> this.findMatch(s, SearchStyle.STRICT, false))
                    .filter(this::possibleMatch)
                    .findFirst();
        }

        // Try fuzzier approaches, if allowed
        if (!result.isPresent() && style != SearchStyle.STRICT) {
            result = nsearch.bareStream()
                    .filter(s -> !s.equals(nsearch))
                    .map(s -> this.findMatch(s, style, true))
                    .filter(this::possibleMatch)
                    .findFirst();
        }

        // Last try with what we've got
        if (!result.isPresent()) {
            Match<AlaLinnaeanClassification, MatchMeasurement> m = this.findMatch(nsearch, style, true);
            if (this.possibleMatch(m))
                result = Optional.of(m);
        }

        if (result.isPresent()) {
            match = create(result.get(), null, true);
            if (this.checkHints && !match.check(nsearch)) {
                List<String> is = new ArrayList<>(match.getIssues());
                is.remove("noIssue");
                is.add("hintMismatch");
                match = match.withIssues(is);
            }
            return match;
        }

        // See if the scientific name is actually a LSID
        if (!result.isPresent() && this.allowLoose && search.isLoose()) {
            Match<AlaLinnaeanClassification, MatchMeasurement> m = searcher.search(search.getScientificName());
            if (m.isValid()) {
                return create(m, "taxonIdMatch", true);
            }
        }

        // Last resort, search using a vernacular name in either the vernacular or scientific slots
        if (!result.isPresent() && nsearch.getVernacularName() != null) {
            Match<AlaLinnaeanClassification, MatchMeasurement> m = this.findVernacular(nsearch.getVernacularName());
            if (m.isValid())
                return create(m, "vernacularMatch", true);
        }
        if (!result.isPresent() && nsearch.getScientificName() != null && this.allowLoose && search.isLoose()) {
            Match<AlaLinnaeanClassification, MatchMeasurement> m = this.findVernacular(nsearch.getScientificName());
            if (m.isValid())
                return create(m, "vernacularMatch", true);
        }

        return NameUsageMatch.FAIL;
    }

    private @NonNull Match<AlaLinnaeanClassification, MatchMeasurement> findMatch(NameSearch search, SearchStyle style, boolean useHints) {
        if (search.getScientificName() == null)
            return Match.invalidMatch();
        AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
        classification.scientificNameAuthorship = search.getScientificNameAuthorship();
        classification.family = search.getFamily();
        classification.genus = search.getGenus();
        // classification. = search.getInfraspecificEpithet();
        classification.kingdom = search.getKingdom();
        classification.class_ = search.getClazz();
        classification.order = search.getOrder();
        classification.phylum = search.getPhylum();
        classification.scientificName = search.getScientificName();
        classification.specificEpithet = search.getSpecificEpithet();
        classification.taxonRank = this.rankAnalysis.fromString(search.getRank(), null);
        if (useHints && search.getHints() != null) {
            for (Map.Entry<String, List<String>> he: search.getHints().entrySet()) {
                Observable<String> observable = HINT_OBSERVABLE_MAP.get(he.getKey());
                if (observable != null) {
                    for (String hint: he.getValue())
                        classification.addHint(observable, hint);
                }
            }
        }
        MatchOptions options = SEARCH_STYLE_MAP.getOrDefault(style, MatchOptions.NONE);
        if (useHints)
            options = options.withUseHints(true);

        try {
            return this.searcher.search(classification, options);
        } catch (BayesianException ex) {
            log.warn("Unable to search for " + search, ex);
            return Match.invalidMatch();
        }
    }

    private @NonNull Match<AlaLinnaeanClassification, MatchMeasurement> findVernacular(String name) {
        if (name == null)
            return Match.invalidMatch();
        AlaVernacularClassification classification = new AlaVernacularClassification();
        classification.vernacularName = name;
        MatchOptions options = MatchOptions.ALL;
        try {
            Match<AlaVernacularClassification, MatchMeasurement> v = this.searcher.search(classification, options);
            if (v.isValid())
                return this.searcher.search(v.getAccepted().taxonId);
            return Match.emptyMatch();
        } catch (BayesianException ex) {
            log.warn("Unable to search for " + name, ex);
            return Match.invalidMatch();
        }
    }


    /**
     * Find a record based on taxon id
     *
     * @param taxonID The taxon guid/lsid
     * @return A match object, with success=false if there was no valid match
     * @throws Exception if something goes horribly wrong
     */
    private NameUsageMatch lookup(String taxonID, boolean follow) throws Exception {
        Match<AlaLinnaeanClassification, MatchMeasurement> result = this.searcher.search(taxonID);

        if (!result.isValid())
            return NameUsageMatch.FAIL;
        return create(result, "taxonIdMatch", follow);
    }

    /**
     * Build a match result out of what we have found.
     *
     * @param match     The search result
     * @param matchType An optional supplied match type, if null this is deduced from the issues list
     * @param useAccepted Use the accepted concept, rather than the matched concept
     * @return A corresponding match object
     * @throws Exception if unable to build the match, usually as a result of some underlying interface problem
     */
    private NameUsageMatch create(Match<AlaLinnaeanClassification, MatchMeasurement> match, String matchType, boolean useAccepted) throws Exception {
        Issues mi = match.getIssues() == null ? Issues.of() : match.getIssues();
        List<String> issues = mi.stream().map(i -> ERROR_MAP.get(i)).filter(Objects::nonNull).sorted().map(ErrorType::toString).collect(Collectors.toList());
        if (matchType == null) {
            final Set<MatchType> mt = mi.stream().map(i -> MATCH_TYPE_MAP.get(i)).filter(Objects::nonNull).collect(Collectors.toSet());
            matchType = MATCH_TYPE_ORDER.stream().filter(t -> mt.contains(t)).findFirst().orElse(MatchType.EXACT).toString();
        }
        if (!match.isValid()) {
            return NameUsageMatch.builder()
                    .success(false)
                    .matchType(matchType)
                    .nameType(null)
                    .synonymType(null)
                    .issues(issues.isEmpty() ? Collections.singletonList("noMatch") : issues)
                    .build();
        }
        AlaLinnaeanClassification matched = match.getMatch();
        AlaLinnaeanClassification accepted = useAccepted ? match.getAccepted() : matched;
        Integer left = match.getLeft();
        Integer right = match.getRight();
        NameType nameType = matched.nameType;
        SynonymType synonymType = SYNONYM_TYPE_MAP.get(matched.taxonomicStatus);
        return NameUsageMatch.builder()
                .success(true)
                .scientificName(accepted.scientificName)
                .scientificNameAuthorship(accepted.scientificNameAuthorship)
                .taxonConceptID(accepted.taxonId)
                .rank(this.rankAnalysis.toStore(accepted.taxonRank))
                .rankID(accepted.rankId)
                .matchType(matchType)
                .nameType(nameType != null ? nameType.toString() : null)
                .synonymType(synonymType == null ? null : synonymType.toString())
                .lft(left)
                .rgt(right)
                .kingdom(accepted.kingdom)
                .kingdomID(accepted.kingdomId)
                .phylum(accepted.phylum)
                .phylumID(accepted.phylumId)
                .classs(accepted.class_)
                .classID(accepted.classId)
                .order(accepted.order)
                .orderID(accepted.orderId)
                .family(accepted.family)
                .familyID(accepted.familyId)
                .genus(accepted.genus)
                .genusID(accepted.genusId)
                .species(null)
                .speciesID(accepted.speciesId)
                .vernacularName(accepted.vernacularName)
                .speciesGroup(speciesGroupsUtil.getSpeciesGroups(left))
                .speciesSubgroup(speciesGroupsUtil.getSpeciesSubGroups(left))
                .issues(issues.isEmpty() ? Collections.singletonList("noIssue") : issues)
                .build();
    }

    /**
     * Get information about the name search resource.
     *
     * @return A suitably jsonable map of configuration and metrics
     */
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap();
        Map<String, Object> config = new HashMap<>();
        config.put("useHints", this.useHints);
        config.put("checkHints", this.checkHints);
        config.put("allowLoose", this.allowLoose);
        config.put("defaultStyle", this.defaultStyle);
        metrics.put("config", config);
        CacheStatistics stats = CacheControl.of(this.searchCache).sampleStatistics();
        Map<String, Object> cache = new HashMap<>();
        cache.put("hitRate", stats.getHitRate());
        cache.put("getCount", stats.getGetCount());
        cache.put("loadCount", stats.getLoadCount());
        cache.put("millisPerLoad", stats.getMillisPerLoad());
        metrics.put("cache", cache);
        return metrics;
    }

    /**
     * Close the resource.
     */
    @Override
    public void close() {
        try {
            this.searcher.close();
        } catch (Exception ex) {
            log.error("Unable to close searcher", ex);
        }
    }

}
