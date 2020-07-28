package au.org.ala.names.ws.resources;

import au.org.ala.names.model.*;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.search.SearchResultException;
import au.org.ala.names.ws.api.NameMatchService;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.integration.CacheLoader;
import org.gbif.api.vocabulary.NameType;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO add diagnostics to payload - similar to GBIF
 */
@Api("Taxonomy search")
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

    //Cache2k instance
    private final Cache<NameSearch, NameUsageMatch> cache;

    public NameSearchResource(NameSearchConfiguration configuration){
        try {
            log.info("Initialising NameSearchResource.....");
            this.searcher = new ALANameSearcher(configuration.getIndex());
            this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(configuration);
            this.useHints = configuration.isUseHints();
            this.checkHints = configuration.isCheckHints();
            this.cache = configuration.getCache().builder(NameSearch.class, NameUsageMatch.class)
                    .loader(new CacheLoader<NameSearch, NameUsageMatch>() {
                        @Override
                        public NameUsageMatch load(NameSearch nameSearch) throws Exception {
                            return search(nameSearch);
                        }
                    }) //auto populating function
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


    @ApiOperation(
            value = "Search by full classification",
            notes = "Search based on a partially filled out classification. " +
                    "The search will use the parameters contained in the body to perform as precise a search as is possible."
    )
    @POST
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch match(NameSearch search) {
        try {
            return this.cache.get(search);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + search);
        }
        return NameUsageMatch.FAIL;
    }

    @ApiOperation(
            value = "Search by full classification via query parameters",
            notes = "Search based on a partially filled out classification. " +
                    "The search will use the parameters supplied to perform as precise a search as is possible."
    )
    @GET
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch match(
            @ApiParam(value = "The scientific name. If not supplied, inferred from other parameters", example = "Dentimitrella austrina") @QueryParam("scientificName") String scientificName,
            @ApiParam(value = "The kingdom name", example = "Animalia") @QueryParam("kingdom") String kingdom,
            @ApiParam(value = "The phylum name") @QueryParam("phylum") String phylum,
            @ApiParam(value = "The class name") @QueryParam("class") String clazz,
            @ApiParam(value = "The order name") @QueryParam("order") String order,
            @ApiParam(value = "The family name", example = "Columbellidae") @QueryParam("family") String family,
            @ApiParam(value = "The genus name") @QueryParam("genus") String genus,
            @ApiParam(value = "The specific epithet, the species part of a binomial name") @QueryParam("specificEpithet") String specificEpithet,
            @ApiParam(value = "The below species (subspecies, variety, form etc.) epithet") @QueryParam("infraspecificEpithet") String infraspecificEpithet,
            @ApiParam(value = "The taxon rank. If not supplied, it may be inferred from other parameters", example = "species") @QueryParam("rank") String rank
    ) {
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
                .build();
        try {
            return this.cache.get(search);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + search);
        }
        return NameUsageMatch.FAIL;
    }

    @ApiOperation(
            value = "Search by name",
            notes = "A simple search based only on scientific name. " +
                    "The search will not be able to resolve complications, such as homonyms."
    )
    @GET
    @Timed
    @Path("/search")
    public NameUsageMatch match(
            @ApiParam(value = "The scientific name", required = true, example = "Acacia dealbata") @QueryParam("q") String name
    ) {
        try {
            NameSearch cl = NameSearch.builder().scientificName(name).build();
            return this.cache.get(cl);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with query: " + name);
        }
        return NameUsageMatch.FAIL;
    }


    @ApiOperation(
            value = "Get taxon information by by vernacular (common) name.",
            notes = "The same Vernacular name may be given to multiple taxa with different scientific names. The result returned is a best-effort match."
    )
    @GET
    @Timed
    @Path("/searchByVernacularName")
    public NameUsageMatch matchVernacular(
            @ApiParam(value = "The common name", required = true, example = "Red Kangaroo") @QueryParam("vernacularName") String vernacularName
    ) {
        try {
            NameSearch cl = NameSearch.builder().vernacularName(vernacularName).build();
            return this.cache.get(cl);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with vernacularName: " + vernacularName);
        }
        return NameUsageMatch.FAIL;
    }

    @ApiOperation(
            value = "Get taxon information by by taxon identifier."
    )
    @GET
    @Timed
    @Path("/getByTaxonID")
    public NameUsageMatch get(
            @ApiParam(value = "The unique taxon identifier", required = true, example = "https://id.biodiversity.org.au/node/apni/2908670") @QueryParam("taxonID") String taxonID
    ) {
        try {
            NameSearch cl = NameSearch.builder().taxonID(taxonID).build();
            return this.cache.get(cl);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with taxonID: " + taxonID);
        }
        return NameUsageMatch.FAIL;
    }

    @ApiOperation(
        value = "Check a name/rank combination and see if it is valid"
    )
    @GET
    @Timed
    @Path("/check")
    public Boolean check(
        @ApiParam(value = "The scientific name", required = true, example = "Animalia") @QueryParam("name") String name,
        @ApiParam(value = "The Linnaean rank", required = true, example = "kingdom") @QueryParam("rank") String rank
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
        //attempt 1: search via taxonConceptID or taxonID if provided
        NameSearchResult idnsr = null;

        if (search.getTaxonConceptID() != null) {
            idnsr = searcher.searchForRecordByLsid(search.getTaxonConceptID());
        } else if (search.getTaxonID() != null) {
            idnsr = searcher.searchForRecordByLsid(search.getTaxonID());
        }

        if (idnsr != null){
            Set<String> vernacularNames = searcher.getCommonNamesForLSID(idnsr.getLsid(), 1);
            return create(idnsr, vernacularNames, idnsr.getMatchType(), null, null, null);
        }
        // Start searching by names
        final NameSearch nsearch = search.normalised();
        MetricsResultDTO metrics = null;
        NameSearchResult result = null;
        // Get the first result that works
        if (useHints) {
            metrics = nsearch.hintStream().map(s -> this.findMetrics(s, false)).filter(m -> m != null && m.getResult() != null).findFirst().orElse(null);
            result = metrics == null ? null : metrics.getResult();
        }

        // Try fuzzier approaches
        if (result == null) {
            metrics = nsearch.bareStream().filter(s -> !s.equals(nsearch)).map(s -> this.findMetrics(s, true)).filter(m -> m != null && m.getResult() != null).findFirst().orElseGet(() -> this.findMetrics(nsearch, true));
            result = metrics == null ? null : metrics.getResult();
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
        if (result == null && nsearch.getScientificName() != null) {
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
            Set<String> vernacularNames = searcher.getCommonNamesForLSID(metrics.getResult().getLsid(), 1);
            match = create(metrics.getResult(), vernacularNames, matchType, metrics.getNameType(), synonymType, metrics.getErrors());
        } else {
            match = create(metrics.getResult(), null, null, metrics.getNameType(), null, metrics.getErrors());
        }
        if (this.checkHints && !match.check(nsearch)) {
            match.getIssues().remove("noIssue");
            match.getIssues().add("hintMismatch" );
        }
        return match;
    }

    private MetricsResultDTO findMetrics(NameSearch search, boolean approximate) {
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
            metrics = searcher.searchForRecordMetrics(lrc, approximate, approximate);
        } catch (SearchResultException ex) {
            log.warn("Unable to complete search for " + lrc, ex);
        }
        return metrics;
    }

    /**
     * Build a match result out of what we have found.
     *
     * @param nsr The search result
     * @param vernacularNames Any additional vernacular names
     * @param matchType The name match type
     * @param nameType The name type
     * @param synonymType Any synonym information
     * @param issues Any issues generated during matching
     *
     * @return A corresponding match object
     *
     * @throws Exception if unable to build the match, usually as a result of some underlying interface problem
     */
    private NameUsageMatch create(NameSearchResult nsr, Set<String> vernacularNames, MatchType matchType, NameType nameType, SynonymType synonymType, Set<ErrorType> issues) throws Exception {
        if(nsr != null && nsr.getRankClassification() != null)  {
            speciesGroupsUtil.getSpeciesGroups(Integer.parseInt(nsr.getLeft()));
            LinnaeanRankClassification lrc = nsr.getRankClassification();
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
                    .lft(nsr.getLeft() != null ? Integer.parseInt(nsr.getLeft()) : null)
                    .rgt(nsr.getRight() != null ? Integer.parseInt(nsr.getRight()) : null)
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
                    .vernacularName(!vernacularNames.isEmpty() ? vernacularNames.iterator().next() : null)
                    .speciesGroup(speciesGroupsUtil.getSpeciesGroups(Integer.parseInt(nsr.getLeft())))
                    .speciesSubgroup(speciesGroupsUtil.getSpeciesSubGroups(Integer.parseInt(nsr.getLeft())))
                    .issues(issues != null ? issues.stream().map(ErrorType::toString).sorted().collect(Collectors.toList()) : Collections.singletonList("noIssue"))
                    .build();
        } else {
            return NameUsageMatch.builder()
                    .success(false)
                    .matchType(matchType != null ? matchType.toString() : "")
                    .nameType(nameType != null ? nameType.toString() : null)
                    .synonymType(synonymType != null ? synonymType.name() : null)
                    .issues(issues != null ? issues.stream().map(ErrorType::toString).sorted().collect(Collectors.toList()) : Collections.singletonList("noMatch"))
                    .build();

        }
    }

    /**
     * Close the resource.
     */
    @Override
    public void close()  {
    }
}
