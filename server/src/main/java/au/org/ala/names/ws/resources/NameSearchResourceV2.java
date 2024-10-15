package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.*;
import au.org.ala.location.AlaLocationClassification;
import au.org.ala.names.AlaLinnaeanClassification;
import au.org.ala.names.AlaLinnaeanFactory;
import au.org.ala.names.AlaVernacularClassification;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v2.LocationSearch;
import au.org.ala.names.ws.api.v2.NameMatchService;
import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.health.Checkable;
import com.codahale.metrics.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cache2k.Cache;
import org.cache2k.operation.CacheControl;
import org.cache2k.operation.CacheStatistics;
import org.gbif.nameparser.api.NameType;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Resource that implements the name search API.
 */
@Tag(
        name = "Taxonomy search, version 2",
        description = "Search for taxonomic information on names, classifications or identifiers"
)
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v2/taxonomy")
@Slf4j
@Singleton
public class NameSearchResourceV2 implements NameMatchService, Checkable {
    private final TaxonomyResource taxonomy;
    /** The location resource */
    private final LocationResource locations;
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
     * Default search style
     */
    private final SearchStyle defaultStyle;
    /**
     * Measure performance
     */
    private final boolean searchMetrics;

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

    @Inject
    public NameSearchResourceV2(NameSearchConfiguration configuration, TaxonomyResource taxonomy, LocationResource locations) {
        try {
            this.taxonomy = taxonomy;
            this.locations = locations;
            this.useHints = configuration.isUseHints();
            this.checkHints = configuration.isCheckHints();
            this.allowLoose = configuration.isAllowLoose();
            this.defaultStyle = configuration.getDefaultStyle();
            this.searchMetrics = configuration.isSearchMetrics();
            this.searchCache = configuration.getCache().cacheBuilder(NameSearch.class, NameUsageMatch.class)
                    .loader(nameSearch -> this.search(nameSearch, Trace.TraceLevel.NONE)) //auto populating function
                    .build();
            this.idCache = configuration.getCache().cacheBuilder(String.class, NameUsageMatch.class)
                    .loader(id -> this.lookup(id, false)) //auto populating function
                    .build();
            this.idAcceptedCache = configuration.getCache().cacheBuilder(String.class, NameUsageMatch.class)
                    .loader(id -> this.lookup(id, true)) //auto populating function
                    .build();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("Unable to initialise name search resource: " + ex.getMessage(), ex);
        }
    }

    /**
     * Make sure that the system is still operating.
     *
     * @return True if things can still be found, the species groups are still working, etc.
     */
    @Override
    public boolean check() {
        try {
            AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
            classification.scientificName = "Animalia";
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.taxonomy.getSearcher().search(classification, MatchOptions.NONE);
            if (!match.isValid())
                return false;
            this.taxonomy.getSpeciesGroups().getSpeciesGroups(1);
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
    public NameUsageMatch match(
            NameSearch search,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
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
    public List<NameUsageMatch> matchAll(
            List<NameSearch> search,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        return search.stream().map(s -> s == null ? null : this.match(s, trace)).collect(Collectors.toList());
    }

    @Operation(
            summary = "Search by full classification via query parameters",
            description = "Search based on a partially filled out classification. " +
                    "The search will use the parameters supplied to perform as precise a search as is possible. " +
                    "Location information can be given to disambiguate taxonomic concepts that apply to specific regions."
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
            @Parameter(description = "The continent for distribution-aware matching", example = "Oceania") @QueryParam("continent") String continent,
            @Parameter(description = "The country for distribution-aware matching", example = "Australia") @QueryParam("country") String country,
            @Parameter(description = "The state or province for distribution-aware matching", example = "South Australia") @QueryParam("stateProvince") String stateProvince,
            @Parameter(description = "The island group for distribution-aware matching") @QueryParam("islandGroup") String islandGroup,
            @Parameter(description = "The island for distribution-aware matching", example = "Kangaroo Island") @QueryParam("island") String island,
            @Parameter(description = "The water body for distribution-aware matching") @QueryParam("waterBody") String waterBody,
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        style = style == null ? this.defaultStyle : style;
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        continent = StringUtils.trimToNull(continent);
        country = StringUtils.trimToNull(country);
        stateProvince = StringUtils.trimToNull(stateProvince);
        islandGroup = StringUtils.trimToNull(islandGroup);
        island = StringUtils.trimToNull(island);
        waterBody = StringUtils.trimToNull(waterBody);
        LocationSearch location = null;
        if (continent != null || country != null || stateProvince != null || islandGroup != null || island != null || waterBody != null) {
            location = LocationSearch.builder()
                    .continent(continent)
                    .country(country)
                    .stateProvince(stateProvince)
                    .islandGroup(islandGroup)
                    .island(island)
                    .waterBody(waterBody)
                    .build();
        }
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
                .location(location)
                .loose(style.isLoose())
                .style(style)
                .build();

        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
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
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        style = style == null ? this.defaultStyle : style;
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        NameSearch search = NameSearch.builder().scientificName(name).loose(true).style(style).build();
        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with query: " + name);
            return NameUsageMatch.forException(e, search);
        }
    }


    @Operation(
            summary = "Get taxon information by vernacular (common) name.",
            description = "The same vernacular name may be given to multiple taxa with different scientific names. The result returned is a best-effort match."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = NameUsageMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByVernacularName")
    public NameUsageMatch matchVernacular(
            @Parameter(description = "The common name", required = true, example = "Red Kangaroo") @QueryParam("vernacularName") String vernacularName,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        NameSearch search = NameSearch.builder().vernacularName(vernacularName).build();
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
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
        } catch (Exception ex) {
            String msg = "Problem matching name : " + ex.getMessage() + " with taxonID: " + taxonID;
            log.error(msg, ex);
            throw new WebApplicationException(msg, Response.Status.INTERNAL_SERVER_ERROR);
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
            } catch (Exception ex) {
                log.error("Problem matching name : " + ex.getMessage() + " with taxonID: " + taxonID, ex);
                match = NameUsageMatch.forException(ex, null);
            }
            matches.add(match);
        }
        return matches;
    }

    private boolean possibleMatch(Match<AlaLinnaeanClassification, MatchMeasurement> match) {
        return match != null && ((match.isValid() || match.getIssues().contains(AlaLinnaeanFactory.UNRESOLVED_HOMONYM)));
    }

    /**
     * Perform a search based on a classification built from various calls.
     *
     * @param search The search classification
     * @param trace The trace level
     * @return A match object, with success=false if there was no valid match
     * @throws Exception if something goes horribly wrong
     */
    private NameUsageMatch search(NameSearch search, Trace.TraceLevel trace) throws Exception {
        NameUsageMatch match = null;
        SearchStyle style = search.getStyle() == null ? this.defaultStyle : search.getStyle();
        MatchOptions strictOptions = SEARCH_STYLE_MAP.get(SearchStyle.STRICT).withTrace(trace).withMeasure(this.searchMetrics);
        MatchOptions options = SEARCH_STYLE_MAP.getOrDefault(style, MatchOptions.NONE).withTrace(trace).withMeasure(this.searchMetrics);

        final NameSearch nsearch = search.normalised();

        // First, get a locality, if available
        final Match<AlaLocationClassification, MatchMeasurement> location = search.getLocation() == null ? null : this.locations.findMatch(search.getLocation(), options, false);

        //attempt 1: search via taxonConceptID or taxonID if provided
        Match<AlaLinnaeanClassification, MatchMeasurement> result = Match.emptyMatch();

         if (search.getTaxonConceptID() != null) {
            result = this.taxonomy.getSearcher().search(search.getTaxonConceptID());
        } else if (search.getTaxonID() != null) {
            result = this.taxonomy.getSearcher().search(search.getTaxonID());
        }

        if (this.possibleMatch(result)) {
            return create(nsearch, result, location, true);
        }
        // Start searching by names

        // Get the first result that works with hints
        if (useHints) {
            result = this.findMatch(nsearch, location, strictOptions, true);
            if (this.possibleMatch(result))
                return create(nsearch, result, location, true);
        }

        // Look for an exact match
        result = this.findMatch(nsearch, location, strictOptions, false);
        if (this.possibleMatch(result))
            return create(nsearch, result, location, true);

        // Try fuzzier approaches, if allowed
        if (style != SearchStyle.STRICT) {
            result = this.findMatch(nsearch, location, options, false);
            if (this.possibleMatch(result))
                return create(nsearch, result, location, true);
        }

        // Last try with what we've got
        if (style != SearchStyle.STRICT) {
            result = this.findMatch(nsearch, location, options, true);
            if (this.possibleMatch(result))
                return create(nsearch, result, location, true);
        }

        // See if the scientific name is actually a LSID
        if (this.allowLoose && search.isLoose()) {
            result = this.taxonomy.getSearcher().search(nsearch.getScientificName());
            if (this.possibleMatch(result)) {
                result = result.with(AlaLinnaeanFactory.VERNACULAR_MATCH);
                return create(nsearch, result, location, true);
            }
        }

        // Last resort, search using a vernacular name in either the vernacular or scientific slots
        if (nsearch.getVernacularName() != null) {
            result = this.findVernacular(nsearch.getVernacularName());
            if (this.possibleMatch(result)) {
                result = result.with(AlaLinnaeanFactory.VERNACULAR_MATCH);
                return create(nsearch, result, location, true);
            }
        }
        if (nsearch.getScientificName() != null && this.allowLoose && search.isLoose()) {
            result = this.findVernacular(nsearch.getVernacularName());
            if (this.possibleMatch(result)) {
                result = result.with(AlaLinnaeanFactory.VERNACULAR_MATCH);
                return create(nsearch, result, location, true);
            }
        }
        return NameUsageMatch.FAIL;
    }


    private @NonNull Match<AlaLinnaeanClassification, MatchMeasurement> findMatch(NameSearch search, Match<AlaLocationClassification, MatchMeasurement> location, MatchOptions options, boolean useHints) {
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
        classification.taxonRank = this.taxonomy.getRankAnalysis().fromString(search.getRank(), null);
        classification.locationId = location != null && location.isValid() ? location.getAllIdentifiers() : null;
        if (useHints && search.getHints() != null) {
            for (Map.Entry<String, List<String>> he: search.getHints().entrySet()) {
                Observable<String> observable = this.taxonomy.getObservable(he.getKey());
                if (observable != null) {
                    for (String hint: he.getValue())
                        classification.addHint(observable, hint);
                }
            }
        }
        if (useHints)
            options = options.withUseHints(true);

        try {
            return this.taxonomy.getSearcher().search(classification, options);
        } catch (BayesianException ex) {
            String msg = "Unable to search for " + search;
            log.error(msg, ex);
            throw new WebApplicationException(msg, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private @NonNull Match<AlaLinnaeanClassification, MatchMeasurement> findVernacular(String name) {
        if (name == null)
            return Match.invalidMatch();
        AlaVernacularClassification classification = new AlaVernacularClassification();
        classification.vernacularName = name;
        MatchOptions options = MatchOptions.ALL;
        try {
            Match<AlaVernacularClassification, MatchMeasurement> v = this.taxonomy.getSearcher().search(classification, options);
            if (v.isValid())
                return this.taxonomy.getSearcher().search(v.getAccepted().taxonId);
            return Match.emptyMatch();
        } catch (BayesianException ex) {
            String msg = "Unable to search for " + name;
            log.error(msg, ex);
            throw new WebApplicationException(msg, Response.Status.INTERNAL_SERVER_ERROR);
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
        Match<AlaLinnaeanClassification, MatchMeasurement> result = this.taxonomy.getSearcher().search(taxonID);

        if (!result.isValid())
            return NameUsageMatch.FAIL;
        return create(null, result, null, follow);
    }

    /**
     * Build a match result out of what we have found.
     *
     * @param search The original search
     * @param match     The search result
     * @param location The location used when searching
     * @param useAccepted Use the accepted concept, rather than the matched concept
     * @return A corresponding match object
     * @throws Exception if unable to build the match, usually as a result of some underlying interface problem
     */
    private NameUsageMatch create(NameSearch search, Match<AlaLinnaeanClassification, MatchMeasurement> match, Match<AlaLocationClassification, MatchMeasurement> location, boolean useAccepted) throws Exception {
        Issues mi = match.getIssues() == null ? Issues.of() : match.getIssues();
        if (location != null) {
            if (!location.isValid())
                mi = mi.with(AlaLinnaeanFactory.INVALID_LOCATION);
            else if (location.getIssues() != null)
                mi = mi.merge(location.getIssues());
        }
        List<String> issues = mi.stream().map(i -> i.qualifiedName()).sorted().collect(Collectors.toList());
        if (!match.isValid()) {
            return NameUsageMatch.builder()
                    .success(false)
                    .issues(issues)
                    .trace(match.getTrace())
                    .build();
        }
        AlaLinnaeanClassification matched = match.getMatch();
        AlaLinnaeanClassification accepted = useAccepted ? match.getAccepted() : matched;
        Integer left = match.getLeft();
        Integer right = match.getRight();
        NameType nameType = matched.nameType;
        NameUsageMatch usage = NameUsageMatch.builder()
                .success(true)
                .scientificName(accepted.scientificName)
                .scientificNameAuthorship(accepted.scientificNameAuthorship)
                .taxonConceptID(accepted.taxonId)
                .rank(this.taxonomy.getRankAnalysis().toStore(accepted.taxonRank))
                .rankID(accepted.rankId)
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
                .probability(match.getProbability().getPosterior())
                .fidelity(match.getFidelity().getFidelity())
                .locationID(location != null && location.isValid() ? location.getAccepted().locationId : null)
                .locality(location != null && location.isValid() ? location.getAccepted().locality : null)
                .distributionIDs(accepted.locationId)
                .distribution(accepted.locationId == null ? null :
                        accepted.locationId.stream()
                                .map(id -> this.locations.get(id, false))
                                .filter(l -> l != null && l.isSuccess())
                                .map(l -> l.getLocality())
                                .collect(Collectors.toSet())
                )
                .speciesGroup(this.taxonomy.getSpeciesGroups().getSpeciesGroups(left))
                .speciesSubgroup(this.taxonomy.getSpeciesGroups().getSpeciesSubGroups(left))
                .issues(issues)
                .trace(match.getTrace())
                .build();
        return usage;
    }

    /**
     * Get information about the name search resource.
     *
     * @return A suitably jsonable map of configuration and metrics
     */
    @Override
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
    }

}
