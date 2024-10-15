package au.org.ala.names.ws.client;

import au.org.ala.names.ws.ALANameMatchingServiceApplication;
import au.org.ala.names.ws.ALANameMatchingServiceConfiguration;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.client.v1.ALANameUsageMatchServiceClient;
import au.org.ala.util.TestUtils;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.DataCacheConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.*;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class ALALookupServiceClientIT extends TestUtils {

    private ClientConfiguration configuration;
    private ALALookupServiceClient client;
    private String NAMEMATCHING_SERVER_URL = "http://localhost:9179";

    public static final DropwizardTestSupport<ALANameMatchingServiceConfiguration> SUPPORT =
            new DropwizardTestSupport<ALANameMatchingServiceConfiguration>(ALANameMatchingServiceApplication.class,
                    ResourceHelpers.resourceFilePath("config.yml")
            );

    @BeforeClass
    public static void setUpClass() throws Exception {
        SUPPORT.before();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (SUPPORT != null) {
            SUPPORT.after();
        }
    }

    @Before
    public void setUp() throws Exception {
        DataCacheConfiguration dataCacheConfig = DataCacheConfiguration.builder().build();
        this.configuration = ClientConfiguration.builder()
                .baseUrl(new URL(NAMEMATCHING_SERVER_URL))
                .dataCache(dataCacheConfig)
                .build();
        this.client = new ALALookupServiceClient(configuration);
    }

    @After
    public void tearDown() throws Exception {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Test
    public void testGetName1() throws Exception {
        String name = client.getName("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", false);
        assertNotNull(name);
        assertEquals("Caretta esculenta", name);
    }

    @Test
    public void testGetName2() throws Exception {
        String name = client.getName("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", true);
        assertNotNull(name);
         assertEquals("Chelonia mydas", name);
    }


    @Test
    public void testGetName3() throws Exception {
        String name = client.getName("Moudly old dough", true);
        assertNull(name);
     }

    @Test
    public void testGetAllNames1() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706",
                "https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f"
        );
        List<String> names = client.getAllNames(ids, true);
        assertNotNull(names);
        assertEquals(ids.size(), names.size());
        assertEquals("Acacia dealbata", names.get(0));
        assertEquals("Chelonia mydas", names.get(1));
        assertEquals("Chelonia mydas", names.get(2));
    }

    @Test
    public void testCheck1() throws Exception {
        Boolean valid = client.check("Animalia", "kingdom");
        assertNotNull(valid);
        assertEquals(true, valid);
    }

    @Test
    public void testAutocomplete1() throws Exception {
        // incomplete scientific name match
        List<Map> result = client.autocomplete("eucaly", 10, false);
        assertNotNull(result);
        assertTrue(result.size() > 0 && result.size() <= 20);
    }

    @Test
    public void testAutocomplete2() throws Exception {
        // incomplete vernacular name match
        List<Map> result = client.autocomplete("womba", 10, false);
        assertNotNull(result);
        assertTrue(result.size() > 0 && result.size() <= 20);
    }

    @Test
    public void testAutocomplete3() throws Exception {
        // failed match
        List<Map> result = client.autocomplete("abcdefg123", 10, false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testSearchForLsidById1() throws Exception {
        // taxonID -> acceptedID
        String result = client.searchForLsidById("https://id.biodiversity.org.au/instance/apni/889096");
        assertNotNull(result);
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738745", result);
    }

    @Test
    public void testSearchForLsidById2() throws Exception {
        // invalid LSID
        String result = client.searchForLsidById("invalid LSID");
        assertNull(result);
    }

    @Test
    public void testSearchForLSID1() throws Exception {
        // Genus
        String result = client.searchForLSID("Eucalyptus");
        assertNotNull(result);
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738743", result);
    }

    @Test
    public void testSearchForLSID2() throws Exception {
        // Invalid match
        String result = client.searchForLSID("invalid match");
        assertNull(result);
    }

    @Test
    public void testGetGuidsForTaxa1() throws Exception {
        // 1 genus match
        List<String> result = client.getGuidsForTaxa(Collections.singletonList("Eucalyptus"));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738743", result.get(0));
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = client.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertEquals("https://biodiversity.org.au/afd/taxa/44f915ad-6090-42ba-a341-a11e47555f04", result.get(1));
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = client.getCommonNamesForLSID("https://biodiversity.org.au/afd/taxa/66d42847-c556-4fa3-902c-a91d9f517286", 10);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Common Wombat"));
    }

    @Test
    public void testGetCommonNamesForLSID2() throws Exception {
        // invalid LSID
        Set<String> result = client.getCommonNamesForLSID("invalid LSID", 10);
        assertNotNull(result);
        assertEquals(result.size(), 0);
    }


}
