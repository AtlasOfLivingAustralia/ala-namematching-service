package au.org.ala.names.ws.resources;

import au.org.ala.names.model.*;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.ws.NameSearchConfiguration;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import com.codahale.metrics.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.Cache;
import org.cache2k.integration.CacheLoader;
import org.gbif.ecat.voc.NameType;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO add diagnostics to payload - similar to GBIF
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
@Slf4j
@Singleton
public class NameSearchResource {

    private ALANameSearcher searcher;

    private SpeciesGroupsUtil speciesGroupsUtil;

    //Cache2k instance
    private final Cache<NameSearch, NameUsageMatch> cache;

    public NameSearchResource(NameSearchConfiguration configuration){
        try {
            log.info("Initialising NameSearchResource.....");
            this.searcher = new ALANameSearcher(configuration.getIndex());
            this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance();
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

    @POST
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch searchByClassification(NameSearch nameSearch) {
        try {
            return this.cache.get(nameSearch);
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + nameSearch);
        }
        return NameUsageMatch.FAIL;
    }

    /**
     * TODO push this search logic down into ala-name-matching library.
     *
     * @param cl
     * @return
     * @throws Exception
     */
    private NameUsageMatch search(NameSearch cl) throws Exception {
        // First get rid of strange stuff
        cl = cl.normalised();

        //attempt 1: search via taxonConceptID or taxonID if provided
        NameSearchResult idnsr = null;

        if (cl.getTaxonConceptID() != null) {
            idnsr = searcher.searchForRecordByLsid(cl.getTaxonConceptID());
        } else if (cl.getTaxonID() != null) {
            idnsr = searcher.searchForRecordByLsid(cl.getTaxonID());
        }

        if (idnsr != null){
            Set<String> vernacularNames = searcher.getCommonNamesForLSID(idnsr.getLsid(), 1);
            return create(idnsr, vernacularNames, idnsr.getMatchType(), null, null, null);
        }


        LinnaeanRankClassification lrc = new LinnaeanRankClassification();
        lrc.setAuthorship(cl.scientificNameAuthorship);
        lrc.setFamily(cl.family);
        lrc.setGenus(cl.genus);
        lrc.setInfraspecificEpithet(cl.infraspecificEpithet);
        lrc.setKingdom(cl.kingdom);
        lrc.setKlass(cl.clazz);
        lrc.setOrder(cl.order);
        lrc.setPhylum(cl.phylum);
        lrc.setScientificName(cl.scientificName);
        lrc.setSpecificEpithet(cl.specificEpithet);
        lrc.setRank(cl.rank);

        // Make an annotated version of the classification with whatever extra information we have
        LinnaeanRankClassification alrc = new LinnaeanRankClassification(lrc);
        boolean annotated = false;
        String inferredScientificName = null;
        RankType inferredRank = null;
        //set the scientificName using available elements of the higher classification
        if (cl.genus != null && cl.specificEpithet != null && cl.infraspecificEpithet != null) {
            inferredScientificName = cl.genus + " " + cl.specificEpithet + " " + cl.infraspecificEpithet;
            inferredRank = RankType.SUBSPECIES;
        } else if (cl.genus != null && cl.specificEpithet != null) {
            inferredScientificName = cl.genus + " " + cl.specificEpithet;
            inferredRank = RankType.SPECIES;
        } else if (cl.genus != null) {
            inferredScientificName = cl.genus;
            inferredRank = RankType.GENUS;
        } else if (cl.family != null) {
            inferredScientificName = cl.family;
            inferredRank = RankType.FAMILY;
        } else if (cl.order != null) {
            inferredScientificName = cl.order;
            inferredRank = RankType.ORDER;
        } else if (cl.clazz != null) {
            inferredScientificName = cl.clazz;
            inferredRank = RankType.CLASS;
        } else if (cl.phylum != null) {
            inferredScientificName = cl.phylum;
            inferredRank = RankType.PHYLUM;
        } else if (cl.kingdom != null) {
            inferredScientificName = cl.kingdom;
            inferredRank = RankType.KINGDOM;
        }
        if (cl.rank == null && inferredRank != null) {
            alrc.setRank(inferredRank.getRank());
            annotated = true;
        }
        if (cl.scientificName == null && inferredScientificName != null) {
            alrc.setScientificName(inferredScientificName);
            annotated = true;
        }

        // First try the annotated version, then try the raw version if that dioesn't work out
        MetricsResultDTO metrics = searcher.searchForRecordMetrics(alrc, true, true);
        NameSearchResult result = metrics.getResult();
        if (result == null && annotated) {
            metrics = searcher.searchForRecordMetrics(lrc, true, true);
            result = metrics.getResult();
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
            return create(metrics.getResult(), vernacularNames, matchType, metrics.getNameType(), synonymType, metrics.getErrors());
        } else {
            return create(metrics.getResult(), null, null, metrics.getNameType(), null, metrics.getErrors());
        }

    }

//        //attempt 2: search using the taxonomic classification if provided
//        Object resultMetric = {
//        try {
//            if(idnsr != null){
//                val metric = new MetricsResultDTO
//                metric.setResult(idnsr)
//                metric
//            } else if(hash.contains("|")) {
//                val lrcl = new LinnaeanRankClassification(
//                        stripStrayQuotes(cl.getKingdom()),
//                        stripStrayQuotes(cl.phylum),
//                        stripStrayQuotes(cl.classs),
//                        stripStrayQuotes(cl.order),
//                        stripStrayQuotes(cl.family),
//                        stripStrayQuotes(cl.genus),
//                        stripStrayQuotes(cl.species),
//                        stripStrayQuotes(cl.specificEpithet),
//                        stripStrayQuotes(cl.subspecies),
//                        stripStrayQuotes(cl.infraspecificEpithet),
//                        stripStrayQuotes(cl.scientificName))
//                lrcl.setRank(cl.taxonRank)
//                nameIndex.searchForRecordMetrics(lrcl, true, true)
//                //fuzzy matching is enabled because we have taxonomic hints to help prevent dodgy matches
//            } else {
//                null
//            }
//        } catch {
//            case e:Exception => {
//                logger.debug(e.getMessage + ", hash =  " + hash, e)
//            }
//            null
//        }
//      }
//
//        //attempt 3: last resort, search using  vernacular name
//        if(resultMetric == null) {
//            val cnsr = nameIndex.searchForCommonName(cl.getVernacularName)
//            if(cnsr != null){
//                resultMetric = new MetricsResultDTO
//                resultMetric.setResult(cnsr)
//            }
//        }
//    }
//
    @GET
    @Timed
    @Path("/search")
    public NameUsageMatch search(@QueryParam("q") String name) {
        try {
            NameSearchResult nsr = searcher.searchForRecord(name);
            if (nsr != null){
                MatchType matchType = nsr.getMatchType();
                if (nsr.getAcceptedLsid() != null && !nsr.getLsid().equals(nsr.getAcceptedLsid())){
                    nsr = searcher.searchForRecordByLsid(nsr.getAcceptedLsid());
                }
                Set<String> vernacularNames = searcher.getCommonNamesForLSID(nsr.getLsid(), 1);
                return create(nsr, vernacularNames, matchType, null, null, null);
            } else {
                return NameUsageMatch.FAIL;
            }
        } catch (Exception e){
            log.warn("Problem matching name : " + e.getMessage() + " with name: " + name);
        }
        return NameUsageMatch.FAIL;
    }

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
}
