package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.*;
import au.org.ala.location.AlaLocationClassification;
import au.org.ala.location.AlaLocationFactory;
import au.org.ala.names.AlaLinnaeanClassification;
import au.org.ala.names.AlaLinnaeanFactory;
import au.org.ala.names.AlaVernacularClassification;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v2.*;
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
        name = "Location search",
        description = "Search for location information on locality infomation"
)
@Produces(MediaType.APPLICATION_JSON)
@Path("/api/v2/location")
@Slf4j
@Singleton
public class LocationResource extends SearchResource implements LocationMatchService {
    // Cache2k instance for searches
    private final Cache<LocationSearch, LocationMatch> searchCache;
    // Cache2k instance for lookups
    private final Cache<String, LocationMatch> idCache;
    // Cache2k instance for lookups
    private final Cache<String, LocationMatch> idAcceptedCache;

    @Inject
    public LocationResource(NameSearchConfiguration configuration, TaxonomyResource taxonomy) {
        super(configuration, taxonomy);
        try {
            this.searchCache = configuration.getCache().cacheBuilder(LocationSearch.class, LocationMatch.class)
                    .loader(search -> this.search(search, Trace.TraceLevel.NONE)) //auto populating function
                    .build();
            this.idCache = configuration.getCache().cacheBuilder(String.class, LocationMatch.class)
                    .loader(id -> this.lookup(id, false)) //auto populating function
                    .build();
            this.idAcceptedCache = configuration.getCache().cacheBuilder(String.class, LocationMatch.class)
                    .loader(id -> this.lookup(id, true)) //auto populating function
                    .build();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("Unable to initialise location search resource: " + ex.getMessage(), ex);
        }
    }

    /**
     * Make sure that the system is still operating.
     *
     * @return True if things can still be found, the species groups are still working, etc.
     */
    @Override
    public boolean check() {
        if (!super.check())
            return false;
        try {
            AlaLocationClassification classification = new AlaLocationClassification();
            classification.locality = "Australia";
            Match<AlaLocationClassification, MatchMeasurement> match = this.taxonomy.getSearcher().search(classification, MatchOptions.NONE);
            if (!match.isValid())
                return false;
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
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LocationMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @RequestBody(description = "Partially filled out classification", content = @Content(schema = @Schema(implementation = LocationSearch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByClassification")
    public LocationMatch match(
            LocationSearch search,
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
            return LocationMatch.forException(ex, search);
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
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationMatch.class)), mediaType = MediaType.APPLICATION_JSON))
    @RequestBody(description = "List of partially filled out classifications", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationSearch.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("searchAllByClassification")
    public List<LocationMatch> matchAll(
            List<LocationSearch> search,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        return search.stream().map(s -> s == null ? null : this.match(s, trace)).collect(Collectors.toList());
    }

    @Operation(
            summary = "Search by full classification via query parameters",
            description = "Search based on a partially filled out classification. " +
                    "The search will use the parameters supplied to perform as precise a search as is possible."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LocationMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/searchByClassification")
    public LocationMatch match(
            @Parameter(description = "The location identifier.") @QueryParam("locationID") String locationID,
            @Parameter(description = "The location name. If not supplied, inferred from other parameters", example = "Bedarra Island") @QueryParam("locality") String locality,
            @Parameter(description = "The continent name") @QueryParam("continent") String continent,
            @Parameter(description = "The country name", example = "Australia") @QueryParam("country") String country,
            @Parameter(description = "The state or province name") @QueryParam("stateProvince") String stateProvince,
            @Parameter(description = "The island group name") @QueryParam("islandGroup") String islandGroup,
            @Parameter(description = "The island name") @QueryParam("island") String island,
            @Parameter(description = "The water body (ocean, sea, bay, etc.) name") @QueryParam("waterBody") String waterBody,
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        style = style == null ? this.defaultStyle : style;
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        LocationSearch search = LocationSearch.builder()
                .locationID(locationID)
                .locality(locality)
                .continent(continent)
                .country(country)
                .stateProvince(stateProvince)
                .islandGroup(islandGroup)
                .island(island)
                .waterBody(waterBody)
                .style(style)
                .build();

        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + search);
            return LocationMatch.forException(e, search);
        }
    }

    @Operation(
            summary = "Search by name",
            description = "A simple search based only on scientific name. " +
                    "The search will not be able to resolve complications, such as homonyms."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LocationMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/search")
    public LocationMatch match(
            @Parameter(description = "The location name", required = true, example = "New South Wales") @QueryParam("q") String name,
            @Parameter(description = "The search style. If not supplied the server default style is used.", example = "MATCH") @QueryParam("style") SearchStyle style,
            @Parameter(description = "The trace level for debugging. If absent, no trace is returned", example = "NONE") @QueryParam("trace" ) Trace.TraceLevel trace
    ) {
        style = style == null ? this.defaultStyle : style;
        trace = trace == null ? Trace.TraceLevel.NONE : trace;
        LocationSearch search = LocationSearch.builder().locality(name).style(style).build();
        try {
            if (trace == Trace.TraceLevel.NONE)
                return this.searchCache.get(search);
            else
                return this.search(search, trace);
        } catch (Exception e) {
            log.warn("Problem matching name : " + e.getMessage() + " with query: " + name);
            return LocationMatch.forException(e, search);
        }
    }

    @Operation(
            summary = "Get location information by location identifier."
    )
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(schema = @Schema(implementation = LocationMatch.class), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getByLocationID")
    public LocationMatch get(
            @Parameter(description = "The unique location identifier", required = true, example = "http://vocab.getty.edu/tgn/1001991") @QueryParam("locationID") String locationID,
            @Parameter(description = "Follow synonyms to the accepted location") @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        try {
            Cache<String, LocationMatch> cache = follow ? this.idAcceptedCache : this.idCache;
            return cache.get(locationID);
        } catch (Exception ex) {
            String msg = "Problem matching name : " + ex.getMessage() + " with locationID: " + locationID;
            log.error(msg, ex);
            throw new WebApplicationException(msg, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
            summary = "Get bulk location information by a list of location identifiers."
    )
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponse(responseCode = "200", description = "successful operation", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LocationMatch.class)), mediaType = MediaType.APPLICATION_JSON))
    @Timed
    @Path("/getAllByLocationID")
    public List<LocationMatch> getAll(
            @Parameter(description = "The list of unique location identifiers", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("locationIDs") List<String> locationIDs,
            @Parameter(description = "Follow synonyms to the accepted location") @QueryParam("follow") @DefaultValue("false") Boolean follow
    ) {
        List<LocationMatch> matches = new ArrayList<>(locationIDs.size());
        Cache<String, LocationMatch> cache = follow ? this.idAcceptedCache : this.idCache;
        for (String locationID : locationIDs) {
            LocationMatch match = LocationMatch.FAIL;
            try {
                match = cache.get(locationID);
            } catch (Exception ex) {
                log.error("Problem matching name : " + ex.getMessage() + " with locationID: " + locationID, ex);
                match = LocationMatch.forException(ex, null);
            }
            matches.add(match);
        }
        return matches;
    }

    /**
     * Perform a search based on a classification built from various calls.
     *
     * @param search The search classification
     * @param trace The trace level
     * @return A match object, with success=false if there was no valid match
     * @throws Exception if something goes horribly wrong
     */
    private LocationMatch search(LocationSearch search, Trace.TraceLevel trace) throws Exception {
        LocationMatch match = null;
        SearchStyle style = search.getStyle() == null ? this.defaultStyle : search.getStyle();
        MatchOptions strictOptions = SEARCH_STYLE_MAP.get(SearchStyle.STRICT).withTrace(trace).withMeasure(this.searchMetrics);
        MatchOptions options = SEARCH_STYLE_MAP.getOrDefault(style, MatchOptions.NONE).withTrace(trace).withMeasure(this.searchMetrics);

        final LocationSearch nsearch = search.normalised();

        //attempt 1: search via locationID if available
        Match<AlaLocationClassification, MatchMeasurement> result = Match.emptyMatch();

         if (search.getLocationID() != null) {
            result = this.taxonomy.getSearcher().searchLocation(search.getLocationID());
        }

        if (result.isValid()) {
            return create(nsearch, result, true);
        }
        // Start searching by names

        // Get the first result that works with hints
        if (useHints) {
            result = this.findMatch(nsearch, strictOptions, true);
            if (result.isValid())
                return create(nsearch, result,true);
        }

        // Look for an exact match
        result = this.findMatch(nsearch, strictOptions, false);
        if (result.isValid())
            return create(nsearch, result, true);

        // Try fuzzier approaches, if allowed
        if (style != SearchStyle.STRICT) {
            result = this.findMatch(nsearch, options, false);
            if (result.isValid())
                return create(nsearch, result,true);
        }

        // Last try with what we've got
        if (style != SearchStyle.STRICT) {
            result = this.findMatch(nsearch, options, true);
            if (result.isValid())
                return create(nsearch, result, true);
        }

        return LocationMatch.FAIL;
    }

    /**
     * Look for a match to the supplied search
     *
     * @param search The search
     * @param options The search options
     * @param useHints Use hints to guide the search
     *
     * @return Any match
     */
    protected @NonNull Match<AlaLocationClassification, MatchMeasurement> findMatch(LocationSearch search, MatchOptions options, boolean useHints) {
        AlaLocationClassification classification = new AlaLocationClassification();
        classification.locality = search.getLocality();
        classification.continent = search.getContinent();
        classification.country = search.getCountry();
        classification.stateProvince = search.getStateProvince();
        classification.islandGroup = search.getIslandGroup();
        classification.island = search.getIsland();
        classification.waterBody = search.getWaterBody();
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

    /**
     * Find a record based on location id
     *
     * @param locationID The location identifier
     * @return A match object, with success=false if there was no valid match
     * @throws Exception if something goes horribly wrong
     */
    private LocationMatch lookup(String locationID, boolean follow) throws Exception {
        Match<AlaLocationClassification, MatchMeasurement> result = this.taxonomy.getSearcher().searchLocation(locationID);

        if (!result.isValid())
            return LocationMatch.FAIL;
        return create(null, result, follow);
    }

    /**
     * Build a match result out of what we have found.
     *
     * @param search The original search
     * @param match     The search result
     * @param useAccepted Use the accepted concept, rather than the matched concept
     * @return A corresponding match object
     * @throws Exception if unable to build the match, usually as a result of some underlying interface problem
     */
    private LocationMatch create(LocationSearch search, Match<AlaLocationClassification, MatchMeasurement> match, boolean useAccepted) throws Exception {
        Issues mi = match.getIssues() == null ? Issues.of() : match.getIssues();
        List<String> issues = mi.stream().map(i -> i.qualifiedName()).sorted().collect(Collectors.toList());
        if (!match.isValid()) {
            return LocationMatch.builder()
                    .success(false)
                    .issues(issues)
                    .trace(match.getTrace())
                    .metrics(match.getMeasurement())
                    .build();
        }
        AlaLocationClassification matched = match.getMatch();
        AlaLocationClassification accepted = useAccepted ? match.getAccepted() : matched;
        Integer left = match.getLeft();
        Integer right = match.getRight();
        LocationMatch usage = LocationMatch.builder()
                .success(true)
                .locality(accepted.locality)
                .locationID(accepted.locationId)
                .locationIDs(match.getAllIdentifiers())
                .continentID(accepted.continentId)
                .continent(accepted.continent)
                .countryID(accepted.countryId)
                .country(accepted.country)
                .stateProvinceID(accepted.stateProvinceId)
                .stateProvince(accepted.stateProvince)
                .islandGroupID(accepted.islandGroupId)
                .islandGroup(accepted.islandGroup)
                .islandID(accepted.islandId)
                .island(accepted.island)
                .waterBodyID(accepted.waterBodyId)
                .waterBody(accepted.waterBody)
                .geographyType(accepted.geographyType)
                .probability(match.getProbability().getPosterior())
                .fidelity(match.getFidelity().getFidelity())
                .issues(issues)
                .trace(match.getTrace())
                .metrics(match.getMeasurement())
                .build();
        return usage;
    }

    @Override
    protected ClassificationMatcher<?, ?, ?, ?> getMatcher() {
        return this.taxonomy.getSearcher().getLocationMatcher();
    }

}
