package au.org.ala.names.ws.resources;

import au.org.ala.names.model.LinnaeanRankClassification;
import au.org.ala.names.model.MatchType;
import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import com.codahale.metrics.annotation.Timed;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Set;

/**
 * TODO add diagnostics to payload - similar to GBIF
 */
@Produces(MediaType.APPLICATION_JSON)
@Path("/api")
public class NameSearchResource {

    private Logger LOG = LoggerFactory.getLogger("mylogger");

    private ALANameSearcher searcher = null;

    private SpeciesGroupsUtil speciesGroupsUtil = null;

    public NameSearchResource(){
        try {
            searcher = new ALANameSearcher("/data/lucene/namematching");
            speciesGroupsUtil = SpeciesGroupsUtil.getInstance();
        } catch (Exception e){
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("Unable to initialise searcher: " + e.getMessage(), e);
        }
    }

    @POST
    @Timed
    @Path("/searchByClassification")
    public NameUsageMatch searchByClassification(NameSearch nameSearch) {
        try {
            return search(nameSearch);
        } catch (Exception e){
            LOG.warn("Problem matching name : " + e.getMessage() + " with nameSearch: " + nameSearch);
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
        //attempt 1: search via taxonConceptID or taxonID if provided
        NameSearchResult idnsr = null;

        if (cl.getTaxonConceptID() != null) {
            idnsr = searcher.searchForRecordByLsid(cl.getTaxonConceptID());
        } else if (cl.getTaxonID() != null) {
            idnsr = searcher.searchForRecordByLsid(cl.getTaxonID());
        }

        if (idnsr != null){
            Set<String> vernacularNames = searcher.getCommonNamesForLSID(idnsr.getLsid(), 1);
            return create(idnsr, vernacularNames, idnsr.getMatchType());
        }

        String scientificName = cl.scientificName;
        //set the scientificName using available elements of the higher classification
        if (cl.scientificName == null) {
            if (cl.subspecies != null) {
                scientificName = cl.subspecies;
            } else if (cl.genus != null && cl.specificEpithet != null && cl.infraspecificEpithet != null) {
                scientificName = cl.genus + " " + cl.specificEpithet + " " + cl.infraspecificEpithet;
            } else if (cl.genus != null && cl.specificEpithet != null) {
                scientificName = cl.genus + " " + cl.specificEpithet;
            } else if (cl.species != null) {
                scientificName = cl.species;
            } else if (cl.genus != null) {
                scientificName = cl.genus;
            } else if (cl.family != null) {
                scientificName = cl.family;
            } else if (cl.clazz != null) {
                scientificName = cl.clazz;
            } else if (cl.order != null) {
                scientificName = cl.order;
            } else if (cl.phylum != null) {
                scientificName = cl.phylum;
            } else if (cl.kingdom != null) {
                scientificName = cl.kingdom;
            }
        }

        LinnaeanRankClassification lrc = new LinnaeanRankClassification();
        BeanUtils.copyProperties(cl, lrc);

        NameSearchResult nsr = searcher.searchForRecord(scientificName, lrc, null, false);
        if (nsr != null){
            MatchType matchType = nsr.getMatchType();
            if (nsr.getAcceptedLsid() != null && nsr.getLsid() != nsr.getAcceptedLsid()){
                nsr = searcher.searchForRecordByLsid(nsr.getAcceptedLsid());
            }
            Set<String> vernacularNames = searcher.getCommonNamesForLSID(nsr.getLsid(), 1);
            return create(nsr, vernacularNames, matchType);
        } else {
            return NameUsageMatch.FAIL;
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
//    private String stripStrayQuotes(String str){
//        if (str == null){
//            null
//        } else {
//            String normalised = str;
//            if (normalised.startsWith("'") || normalised.startsWith("\"")) normalised = normalised.drop(1)
//            if (normalised.endsWith("'") || normalised.endsWith("\"")) normalised = normalised.dropRight(1)
//            return normalised;
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
                if (nsr.getAcceptedLsid() != null && nsr.getLsid() != nsr.getAcceptedLsid()){
                    nsr = searcher.searchForRecordByLsid(nsr.getAcceptedLsid());
                }
                Set<String> vernacularNames = searcher.getCommonNamesForLSID(nsr.getLsid(), 1);
                return create(nsr, vernacularNames, matchType);
            } else {
                return NameUsageMatch.FAIL;
            }
        } catch (Exception e){
            LOG.warn("Problem matching name : " + e.getMessage() + " with name: " + name);
        }
        return NameUsageMatch.FAIL;
    }

    private NameUsageMatch create(NameSearchResult nsr, Set<String> vernacularNames, MatchType matchType) throws Exception {
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
                    .build();
        } else {
            return NameUsageMatch.FAIL;
        }
    }
}
