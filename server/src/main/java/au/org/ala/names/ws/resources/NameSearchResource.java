package au.org.ala.names.ws.resources;

import au.org.ala.names.model.*;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.search.SearchResultException;
import au.org.ala.names.ws.api.NameMatchService;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.api.SearchStyle;
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
import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.operation.CacheControl;
import org.cache2k.operation.CacheStatistics;
import org.gbif.api.vocabulary.NameType;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource that implements the name search API.
 *
 * TODO add diagnostics to payload - similar to GBIF
 */
@Tag(
        name = "Taxonomy search",
        description = "Search for taxonomic information on names, classifications or identifiers"
)
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
@Slf4j
@Singleton
public class NameSearchResource implements NameMatchService {
    /** Searcher for names */
    private final ALANameSearcher searcher;
    /** Map taxa onto species groups */
    private final SpeciesGroupsUtil speciesGroupsUtil;
    /** Use hints to guide search */
    private final boolean useHints;
    /** Use hints to check search */
    private final boolean checkHints;
    /** Allow loose searched */
    private final boolean allowLoose;
    /** Use the index-supplied preferred vernacular name */
    private final boolean preferredVernacular;
    /** Default search style */
    private final SearchStyle defaultStyle;

    // Cache2k instance for searches
    private final Cache<NameSearch, NameUsageMatch> searchCache;
    // Cache2k instance for lookups
    private final Cache<String, NameUsageMatch> idCache;
    // Cache2k instance for derefereced lookups
    private final Cache<String, NameUsageMatch> idAcceptedCache;

    @Inject
    public NameSearchResource(NameSearchConfiguration configuration){
        try {
            log.info("Initialising NameSearchResource.....");
            this.searcher = new ALANameSearcher(configuration.getIndex());
            this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(configuration);
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
        } catch (Exception e){
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
            this.searcher.searchForRecord("Animalia");
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
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + search);
            return NameUsageMatch.forException(e, search);
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
        } catch (Exception e){
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
        } catch (Exception e){
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
        } catch (Exception e){
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
         } catch (Exception e){
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
        for (String taxonID: taxonIDs) {
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
        } catch (Exception e){
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
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(examples = @ExampleObject(value="true")))
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
        RankType rk = RankType.getForName(rank);
        if (rk == null)
            throw new IllegalArgumentException("No matching rank for " + rank);
        try {
            NameSearchResult result = this.searcher.searchForRecord(name, rk);
            return result != null;
        } catch (SearchResultException ex) {
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

        return this.searcher.autocomplete(query, max, includeSynonyms);
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
        return this.searcher.searchForLsidById(id);
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
            return this.searcher.searchForLSID(name);
        } catch (SearchResultException e){
            log.warn("Problem matching LSID : " + e.getMessage() + " for name: " + name);
        }
        return "";
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
            log.error("getGuisForTaxa:" + taxa.size());
            List<String> guids = this.searcher.getGuidsForTaxa(taxa);
            return guids;
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with name: " + taxa);
        }
        return new ArrayList();
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
            Set<String> vernacularNames = this.searcher.getCommonNamesForLSID(lsid, max);
            return vernacularNames;
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with name: " + lsid);
        }
        return new HashSet<>();
    }

    /**
     * Perform a search based on a classification built from various calls.
     *
     * @param search The search classification
     *
     * @return A match object, with success=false if there was no valid match
     *
     * @throws Exception if something goes horribly wrong
     */
    private NameUsageMatch search(NameSearch search) throws Exception {
        NameUsageMatch match = null;
        SearchStyle style = search.getStyle() == null ? this.defaultStyle : search.getStyle();
        //attempt 1: search via taxonConceptID or taxonID if provided
        NameSearchResult idnsr = null;

        if (search.getTaxonConceptID() != null) {
            idnsr = searcher.searchForRecordByLsid(search.getTaxonConceptID());
        } else if (search.getTaxonID() != null) {
            idnsr = searcher.searchForRecordByLsid(search.getTaxonID());
        }

        if (idnsr != null){
             return create(idnsr, idnsr.getMatchType(), null, null, null);
        }
        // Start searching by names
        final NameSearch nsearch = search.normalised();
        MetricsResultDTO metrics = null;
        NameSearchResult result = null;

        // Get the first result that works with hints
        if (useHints) {
            metrics = nsearch.hintStream().map(s -> this.findMetrics(s, SearchStyle.STRICT)).filter(m -> m != null && m.getResult() != null).findFirst().orElse(null);
            result = metrics == null ? null : metrics.getResult();
        }

        // Look for an exact match
        if (result == null) {
            metrics = nsearch.bareStream().filter(s -> !s.equals(nsearch)).map(s -> this.findMetrics(s, SearchStyle.STRICT)).filter(m -> m != null && m.getResult() != null).findFirst().orElse(null);
            result = metrics == null ? null : metrics.getResult();
        }

        // Try fuzzier approaches, if allowed
        if (result == null && style != SearchStyle.STRICT) {
            metrics = nsearch.bareStream().filter(s -> !s.equals(nsearch)).map(s -> this.findMetrics(s, style)).filter(m -> m != null && m.getResult() != null).findFirst().orElse(null);
            result = metrics == null ? null : metrics.getResult();
        }

        // Last try with what we've got
        if (result == null) {
            metrics = this.findMetrics(nsearch, style);
            result = metrics == null ? null : metrics.getResult();
        }

        // See if the scientific name is actually a LSID
        if (result == null && this.allowLoose && search.isLoose()) {
            idnsr = searcher.searchForRecordByLsid(search.getScientificName());
            if (idnsr != null){
                return create(idnsr, idnsr.getMatchType(), null, null, null);
            }
        }

        // Last resort, search using a vernacular name in either the vernacular or scientific slots
        if (metrics == null)
            metrics = new MetricsResultDTO();
        if (result  == null && nsearch.getVernacularName() != null) {
            result = this.searcher.searchForCommonName(nsearch.getVernacularName());
            if (result != null) {
                metrics.setNameType(NameType.INFORMAL);
                metrics.setResult(result);
            }
        }
        if (result == null && nsearch.getScientificName() != null && this.allowLoose && search.isLoose()) {
            result = this.searcher.searchForCommonName(nsearch.getScientificName());
            if (result != null) {
                metrics.setNameType(NameType.INFORMAL);
                metrics.setResult(result);
            }
        }

        if (result != null) {
            MatchType matchType = result.getMatchType();
            SynonymType synonymType = result.getSynonymType();
            if (result.getAcceptedLsid() != null && !result.getLsid().equals(result.getAcceptedLsid())) {
                result = searcher.searchForRecordByLsid(result.getAcceptedLsid());
                if (result != null)
                    metrics.setResult(result);
            }
            match = create(metrics.getResult(), matchType, metrics.getNameType(), synonymType, metrics.getErrors());
        } else {
            match = create(metrics.getResult(), null, metrics.getNameType(), null, metrics.getErrors());
        }
        if (this.checkHints && !match.check(nsearch)) {
            match.getIssues().remove("noIssue");
            match.getIssues().add("hintMismatch" );
        }
        return match;
    }

    private MetricsResultDTO findMetrics(NameSearch search, SearchStyle style) {
        if (search.getScientificName() == null)
            return null;
        LinnaeanRankClassification lrc = new LinnaeanRankClassification();
        lrc.setAuthorship(search.getScientificNameAuthorship());
        lrc.setFamily(search.getFamily());
        lrc.setGenus(search.getGenus());
        lrc.setInfraspecificEpithet(search.getInfraspecificEpithet());
        lrc.setKingdom(search.getKingdom());
        lrc.setKlass(search.getClazz());
        lrc.setOrder(search.getOrder());
        lrc.setPhylum(search.getPhylum());
        lrc.setScientificName(search.getScientificName());
        lrc.setSpecificEpithet(search.getSpecificEpithet());
        lrc.setRank(search.getRank());

        MetricsResultDTO metrics = null;
        try {
            metrics = searcher.searchForRecordMetrics(lrc, style.isHisherOrder(), style.isFuzzy());
        } catch (SearchResultException ex) {
            log.warn("Unable to complete search for " + lrc, ex);
        }
        return metrics;
    }


    /**
     * Find a record based on taxon id
     *
     * @param taxonID The taxon guid/lsid
     *
     * @return A match object, with success=false if there was no valid match
     *
     * @throws Exception if something goes horribly wrong
     */
    private NameUsageMatch lookup(String taxonID, boolean follow) throws Exception {
        NameSearchResult result = this.searcher.searchForRecordByLsid(taxonID);

        if (result == null)
            return NameUsageMatch.FAIL;

        MatchType matchType = result.getMatchType();
        SynonymType synonymType = result.getSynonymType();
        if (follow) {
            if (result.getAcceptedLsid() != null && !result.getLsid().equals(result.getAcceptedLsid())) {
                result = searcher.searchForRecordByLsid(result.getAcceptedLsid());
            }
        }
         return create(result, matchType, null, synonymType, null);
    }

    /**
     * Get the preferred vernacular name for a search result.
     * <p>
     * If {@link #preferredVernacular} is true then use the
     * precomputed version in the index.
     * Otherwise retrieve a vernacular name.
     * </p>
     *
     * @param nsr The search result
     *
     * @return The preferred vernacular name or null for none
     */
    private String preferredVernacularName(NameSearchResult nsr) {
        if (this.preferredVernacular) {
            return nsr.getVernacularName();
        }
        return searcher.getCommonNameForLSID(nsr.getLsid());
    }

    /**
     * Build a match result out of what we have found.
     *
     * @param nsr The search result
     * @param matchType The name match type
     * @param nameType The name type
     * @param synonymType Any synonym information
     * @param issues Any issues generated during matching
     *
     * @return A corresponding match object
     *
     * @throws Exception if unable to build the match, usually as a result of some underlying interface problem
     */
    private NameUsageMatch create(NameSearchResult nsr, MatchType matchType, NameType nameType, SynonymType synonymType, Set<ErrorType> issues) throws Exception {
        if(nsr != null && nsr.getRankClassification() != null)  {
            LinnaeanRankClassification lrc = nsr.getRankClassification();
            Integer lft = nsr.getLeft() != null ? Integer.parseInt(nsr.getLeft()) : null;
            Integer rgt = nsr.getRight() != null ? Integer.parseInt(nsr.getRight()) : null;
            return NameUsageMatch.builder()
                    .success(true)
                    .scientificName(lrc.getScientificName())
                    .scientificNameAuthorship(lrc.getAuthorship())
                    .taxonConceptID(nsr.getLsid())
                    .rank(nsr.getRank() != null ? nsr.getRank().getRank() : null)
                    .rankID(nsr.getRank() != null ? nsr.getRank().getId() : null)
                    .matchType(matchType != null ? matchType.toString() : "")
                    .nameType(nameType != null ? nameType.toString() : null)
                    .synonymType(synonymType != null ? synonymType.name() : null)
                    .lft(lft)
                    .rgt(rgt)
                    .kingdom(lrc.getKingdom())
                    .kingdomID(lrc.getKid())
                    .phylum(lrc.getPhylum())
                    .phylumID(lrc.getPid())
                    .classs(lrc.getKlass())
                    .classID(lrc.getCid())
                    .order(lrc.getOrder())
                    .orderID(lrc.getOid())
                    .family(lrc.getFamily())
                    .familyID(lrc.getFid())
                    .genus(lrc.getGenus())
                    .genusID(lrc.getGid())
                    .species(lrc.getSpecies())
                    .speciesID(lrc.getSid())
                    .vernacularName(this.preferredVernacularName(nsr))
                    .speciesGroup(speciesGroupsUtil.getSpeciesGroups(lft))
                    .speciesSubgroup(speciesGroupsUtil.getSpeciesSubGroups(lft))
                    .issues(issues != null ? issues.stream().map(ErrorType::toString).sorted().collect(Collectors.toList()) : Collections.singletonList("noIssue"))
                    .build();
        } else {
            issues.remove(ErrorType.NONE);
            return NameUsageMatch.builder()
                    .success(false)
                    .matchType(matchType != null ? matchType.toString() : "")
                    .nameType(nameType != null ? nameType.toString() : null)
                    .synonymType(synonymType != null ? synonymType.name() : null)
                    .issues(issues != null && !issues.isEmpty() ? issues.stream().map(ErrorType::toString).sorted().collect(Collectors.toList()) : Collections.singletonList("noMatch"))
                    .build();

        }
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
    public void close()  {
    }

}
