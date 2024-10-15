package au.org.ala.names.ws.client.v2;

import au.org.ala.bayesian.Trace;
import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import au.org.ala.names.ws.client.v2.ALANameUsageMatchServiceClient;
import au.org.ala.util.TestUtils;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.DataCacheConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.HttpException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class ALANameUsageMatchServiceCachingClientTest extends TestUtils {
    private MockWebServer server;
    private ClientConfiguration configuration;
    private ALANameUsageMatchServiceClient client;

    @Before
    public void setUp() throws Exception {
        this.server = new MockWebServer();
        server.start();

        DataCacheConfiguration dataCacheConfig = DataCacheConfiguration.builder().build();
        this.configuration = ClientConfiguration.builder()
                .baseUrl(server.url("").url())
                .dataCache(dataCacheConfig)
                .build();
        this.client = new ALANameUsageMatchServiceClient(configuration);
    }

    @After
    public void tearDown() throws Exception {
        this.server.shutdown();
        this.client.close();
    }

    /** Simple request/response */
    @Test
    public void testMatchNameSearch1() throws Exception {
        String request = this.getResource("request-1.json");
        String response = this.getResource("response-1.json");

        server.enqueue(new MockResponse().setBody(response));
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = client.match(search, Trace.TraceLevel.NONE);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Plantae", match.getKingdom());
        assertEquals(Collections.singletonList("http://ala.org.au/issues/1.0/acceptedAndSynonym"), match.getIssues());
        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchByClassification?trace=NONE", req.getPath());
        assertEquals(request, req.getBody().readUtf8());
    }


    /** Simple request/response with caching */
    @Test
    public void testMatchNameSearch2() throws Exception {
        String request = this.getResource("request-1.json");
        String response = this.getResource("response-1.json");

        server.enqueue(new MockResponse().setBody(response));
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = client.match(search, Trace.TraceLevel.NONE);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Plantae", match.getKingdom());
        assertEquals(Collections.singletonList("http://ala.org.au/issues/1.0/acceptedAndSynonym"), match.getIssues());
        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchByClassification?trace=NONE", req.getPath());
        assertEquals(request, req.getBody().readUtf8());

        search = NameSearch.builder().scientificName("Acacia dealbata").build();
        match = client.match(search, Trace.TraceLevel.NONE);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals(1, server.getRequestCount());

        search = NameSearch.builder().scientificName("Acacia dealbata").build();
        match = client.match(search, Trace.TraceLevel.NONE);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals(1, server.getRequestCount());
    }

    /** Multiple search */
    @Test
    public void testMatchAllNameSearch1() throws Exception {
        String request = this.getResource("request-all-1.json");
        String response = this.getResource("response-all-1.json");

        server.enqueue(new MockResponse().setBody(response));
        List<NameSearch> searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        List<NameUsageMatch> matches = client.matchAll(searches, Trace.TraceLevel.NONE);

        assertNotNull(matches);
        assertEquals(2, matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://biodiversity.org.au/afd/taxa/7e6e134b-2bc7-43c4-b23a-6e3f420f57ad", match.getTaxonConceptID());

        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchAllByClassification?trace=NONE", req.getPath());
        assertEquals(request, req.getBody().readUtf8());
    }

    /** Multiple search with caching */
    @Test
    public void testMatchAllNameSearch2() throws Exception {
        String request = this.getResource("request-all-1.json");
        String response = this.getResource("response-all-1.json");

        server.enqueue(new MockResponse().setBody(response));
        List<NameSearch> searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        List<NameUsageMatch> matches = client.matchAll(searches, Trace.TraceLevel.NONE);

        assertNotNull(matches);
        assertEquals(2, matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://biodiversity.org.au/afd/taxa/7e6e134b-2bc7-43c4-b23a-6e3f420f57ad", match.getTaxonConceptID());

        matches = client.matchAll(searches, Trace.TraceLevel.NONE);
        assertNotNull(matches);
        assertEquals(2, matches.size());
        match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://biodiversity.org.au/afd/taxa/7e6e134b-2bc7-43c4-b23a-6e3f420f57ad", match.getTaxonConceptID());

        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchAllByClassification?trace=NONE", req.getPath());
        assertEquals(request, req.getBody().readUtf8());
    }

    /** Multiple search with caching */
    @Test
    public void testMatchAllNameSearch3() throws Exception {
        String request1 = this.getResource("request-all-1.json");
        String request2 = this.getResource("request-all-2.json");
        String response1 = this.getResource("response-all-1.json");
        String response2 = this.getResource("response-all-2.json");

        server.enqueue(new MockResponse().setBody(response1));
        server.enqueue(new MockResponse().setBody(response2));
        List<NameSearch> searches;
        List<NameUsageMatch> matches;
        NameUsageMatch match;

        searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        matches = client.matchAll(searches, Trace.TraceLevel.NONE);

        assertNotNull(matches);
        assertEquals(2, matches.size());

        searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Vachellia nilotica").build());
        searches.add(NameSearch.builder().scientificName("Dalatias licha").build());
        matches = client.matchAll(searches, Trace.TraceLevel.NONE);

        assertNotNull(matches);
        assertEquals(3, matches.size());
        match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("Vachellia nilotica", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/node/apni/7384761", match.getTaxonConceptID());
        match = matches.get(2);
        assertTrue(match.isSuccess());
        assertEquals("Dalatias licha", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://biodiversity.org.au/afd/taxa/2bd8e210-9bc2-42a1-a432-9212ce959682", match.getTaxonConceptID());

        assertEquals(2, server.getRequestCount());
        RecordedRequest req1 = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchAllByClassification?trace=NONE", req1.getPath());
        assertEquals(request1, req1.getBody().readUtf8());
        RecordedRequest req2 = server.takeRequest();
        assertEquals("/api/v2/taxonomy/searchAllByClassification?trace=NONE", req2.getPath());
        assertEquals(request2, req2.getBody().readUtf8());
    }

    /** Respond to error */
    @Test
    public void testError1() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("Unable to connect to index"));
        try {
            NameUsageMatch match = client.match("Acacia dealbata", null,null);
            fail("Expecting HttpException");
        } catch (HttpException ex) {
            assertEquals(500, ex.code());
            assertEquals("Unable to connect to index", ex.response().errorBody().string());
        }
        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/v2/taxonomy/search?q=Acacia%20dealbata", req.getPath());
    }
}
