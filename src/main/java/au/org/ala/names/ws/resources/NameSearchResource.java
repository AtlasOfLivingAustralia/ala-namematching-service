package au.org.ala.names.ws.resources;

import au.org.ala.names.model.LinnaeanRankClassification;
import au.org.ala.names.model.MatchType;
import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.core.SpeciesGroup;
import au.org.ala.names.ws.core.SpeciesGroupsUtil;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
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

//    @GET
//    @Timed
//    public List<SpeciesGroup> groups() throws Exception {
//        return speciesGroupsUtil.getSpeciesGroups();
//    }

//    @GET
//    @Timed
//    public List<SpeciesGroup> subgroups() throws Exception {
//        return speciesGroupsUtil.getSpeciesSubgroups();
//    }

    @GET
    @Timed
    public NameSearch search(@QueryParam("q") String name) {
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
                return NameSearch.FAIL;
            }
        } catch (Exception e){
            LOG.warn("Problem matching name : " + e.getMessage() + " with name: " + name);
        }
        return NameSearch.FAIL;
    }

    private NameSearch create(NameSearchResult nsr, Set<String> vernacularNames, MatchType matchType) throws Exception {
        if(nsr != null && nsr.getRankClassification() != null)  {


            speciesGroupsUtil.getSpeciesGroups(Integer.parseInt(nsr.getLeft()));



            LinnaeanRankClassification lrc = nsr.getRankClassification();
            return NameSearch.builder()
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
            return NameSearch.FAIL;
        }
    }
}
