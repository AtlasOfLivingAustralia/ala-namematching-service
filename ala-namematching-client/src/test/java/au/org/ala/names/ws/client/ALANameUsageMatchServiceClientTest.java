package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.util.TestUtils;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.gbif.rest.client.configuration.ClientConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStreamReader;

import static org.junit.Assert.*;

public class ALANameUsageMatchServiceClientTest extends TestUtils {
    private MockWebServer server;
    private ClientConfiguration configuration;
    private ALANameUsageMatchServiceClient client;

    @Before
    public void setUp() throws Exception {
        this.server = new MockWebServer();
        server.start();

        this.configuration = ClientConfiguration.builder().withBaseApiUrl(server.url("").toString()).build();
        this.client = new ALANameUsageMatchServiceClient(configuration);
    }

    @After
    public void tearDown() throws Exception {
        this.server.shutdown();
        this.client.close();
    }

    /** Simple request/response */
    @Test
    public void testSuccess1() throws Exception {
        String request = this.getResource("request-1.json");
        String response = this.getResource("response-1.json");

        server.enqueue(new MockResponse().setBody(response));
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = client.match(search);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/searchByClassification", req.getPath());
        assertEquals(request, req.getBody().readUtf8());
    }

    /** Fail on unresolved homonym */
    @Test
    public void testHomonym1() throws Exception {
        String request = this.getResource("request-2.json");
        String response = this.getResource("response-2.json");

        server.enqueue(new MockResponse().setBody(response));
        NameSearch search = NameSearch.builder().scientificName("Macropus").build();
        NameUsageMatch match = client.match(search);

        assertFalse(match.isSuccess());
        assertEquals(1, server.getRequestCount());
        RecordedRequest req = server.takeRequest();
        assertEquals("/api/searchByClassification", req.getPath());
        assertEquals(request, req.getBody().readUtf8());
    }

}
