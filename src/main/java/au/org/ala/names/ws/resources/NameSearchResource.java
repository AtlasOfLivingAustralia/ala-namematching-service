package au.org.ala.names.ws.resources;

import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.names.ws.api.NameSearch;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class NameSearchResource {

    private Logger LOG = LoggerFactory.getLogger("mylogger");

    private ALANameSearcher searcher = null;

    public NameSearchResource(){
        try {
            searcher = new ALANameSearcher("/data/lucene/namematching");
        } catch (Exception e){
            LOG.error(e.getMessage(), e);
            throw new RuntimeException("Unable to initialise searcher: " + e.getMessage(), e);
        }
    }

    @GET
    @Timed
    public NameSearch search(@QueryParam("q") String name) {
        try {
            return create(searcher.searchForRecord(name));
        } catch (Exception e){
            LOG.error(e.getMessage(), e);
        }
        return new NameSearch(false);
    }

    static NameSearch create(NameSearchResult nsr){
        if(nsr != null){
            return new NameSearch(
                    true,
                    nsr.getCleanName(),
                    nsr.getMatchType() != null ? nsr.getMatchType().toString() : "",
                    nsr.getLsid(),
                    nsr.getAcceptedLsid(),
                    nsr.getRank() != null ? nsr.getRank().getRank() : "",
                    nsr.getLeft(),
                    nsr.getRight()
            );
        } else {
            return new NameSearch(false);
        }
    }
}
