package au.org.ala.names.ws.resources;

import au.org.ala.bayesian.BayesianException;
import au.org.ala.bayesian.Match;
import au.org.ala.bayesian.MatchMeasurement;
import au.org.ala.bayesian.MatchOptions;
import au.org.ala.names.AlaLinnaeanClassification;
import au.org.ala.names.Autocomplete;
import au.org.ala.names.ws.api.LookupService;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.health.Checkable;
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
import org.gbif.nameparser.api.Rank;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Resource that implements the identifier lookup porttion of the API
 */
@Tag(
        name = "Scientific name and taxon identifier lookup",
        description = "Find names or identifiers based on identifiers or names"
)
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
@Slf4j
@Singleton
public class LookupResource implements LookupService, Checkable {
    /** The taxonomy resource supplier */
    private final TaxonomyResource taxonomy;

    @Inject
    public LookupResource(NameSearchConfiguration configuration, TaxonomyResource taxonomy) {
        try {
            this.taxonomy = taxonomy;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new RuntimeException("Unable to initialise lookup: " + ex.getMessage(), ex);
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
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.taxonomy.getSearcher().search(taxonID);
            if (!match.isValid())
                return null;
            else if (follow != null && follow.booleanValue())
                return match.getAccepted().scientificName;
            else
                return match.getMatch().scientificName;
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
        Rank rk = this.taxonomy.getRankAnalysis().fromString(rank, null);
        if (rk == null)
            throw new IllegalArgumentException("No matching rank for " + rank);
        try {
            AlaLinnaeanClassification classification = new AlaLinnaeanClassification();
            classification.scientificName = name;
            classification.taxonRank = rk;
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.taxonomy.getSearcher().search(classification, MatchOptions.NONE);
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
            List<Autocomplete> autocomplete = this.taxonomy.getSearcher().autocomplete(query, max, includeSynonyms);
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
            Match<AlaLinnaeanClassification, MatchMeasurement> m = this.taxonomy.getSearcher().search(id);
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
            Match<AlaLinnaeanClassification, MatchMeasurement> match = this.taxonomy.getSearcher().search(classification, MatchOptions.NONE);
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
            summary = "Get all common names for a taxon",
            description = "Get the list of vernacular names that might apply to this taxon."
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
            for (String name : this.taxonomy.getSearcher().getVernacularNames(lsid)) {
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


    /**
     * Get information about the name search resource.
     *
     * @return A suitably jsonable map of configuration and metrics
     */
    @Override
    public Map<String, Object> getMetrics() {
        Map<String, Object> metrics = new HashMap();
        return metrics;
    }

    /**
     * Close the resource.
     */
    @Override
    public void close() {
    }

}
