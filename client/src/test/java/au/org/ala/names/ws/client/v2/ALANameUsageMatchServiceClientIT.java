package au.org.ala.names.ws.client.v2;

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

public class ALANameUsageMatchServiceClientIT extends TestUtils {

    private ClientConfiguration configuration;
    private au.org.ala.names.ws.client.v1.ALANameUsageMatchServiceClient client;
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
        this.client = new ALANameUsageMatchServiceClient(configuration);
    }

    @After
    public void tearDown() throws Exception {
        if (this.client != null) {
            this.client.close();
        }
    }

    /** Simple request/response */
    @Test
    public void testMatchNameSearch1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = client.match(search);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Plantae", match.getKingdom());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    /** Fail on unresolved homonym */
    @Test
    public void testMatchNameSearch2() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Thalia").build();
        NameUsageMatch match = client.match(search);

        assertFalse(match.isSuccess());
        assertNull(match.getKingdom());
        assertEquals(Collections.singletonList("homonym"), match.getIssues());
    }

    /** Supply hints */
    @Test
    public void testMatchNameSearch3() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Animalia"));
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").hints(hints).build();
        NameUsageMatch match = client.match(search);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals(Collections.singletonList("hintMismatch"), match.getIssues());
    }

    /** Supply style */
    @Test
    public void testMatchNameSearch4() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").style(SearchStyle.STRICT).build();
        NameUsageMatch match = client.match(search);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    /** Supply style */
    @Test
    public void testMatchNameSearch5() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Akacia dealbata").style(SearchStyle.FUZZY).build();
        NameUsageMatch match = client.match(search);

        assertTrue(match.isSuccess());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    /** Supply style */
    @Test
    public void testMatchNameSearch6() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Akacia dealbata").style(SearchStyle.STRICT).build();
        NameUsageMatch match = client.match(search);

        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("noMatch"), match.getIssues());
    }


    /** Multiple search */
    @Test
    public void testMatchAllNameSearch1() throws Exception {
        List<NameSearch> searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        List<NameUsageMatch> matches = client.matchAll(searches);

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
    }

    /** Multiple search with caching */
    @Test
    public void testMatchAllNameSearch2() throws Exception {
        List<NameSearch> searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        List<NameUsageMatch> matches = client.matchAll(searches);

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

        matches = client.matchAll(searches);
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
    }

    /** Multiple search with caching */
    @Test
    public void testMatchAllNameSearch3() throws Exception {
        List<NameSearch> searches;
        List<NameUsageMatch> matches;
        NameUsageMatch match;

        searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Osphranter rufus").build());
        matches = client.matchAll(searches);

        assertNotNull(matches);
        assertEquals(2, matches.size());

        searches = new ArrayList<>();
        searches.add(NameSearch.builder().scientificName("Acacia dealbata").build());
        searches.add(NameSearch.builder().scientificName("Vachellia nilotica").build());
        searches.add(NameSearch.builder().scientificName("Dalatias licha").build());
        matches = client.matchAll(searches);

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
    }

    /** Simple request/response */
    @Test
    public void testMatchNameParams1() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbata", "Plantae", null, null, null, null, null, null, null, null, null);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
    }

    /** Simple name match */
    @Test
    public void testMatchName1() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbata", null);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
    }


    /** Simple name match with style */
    @Test
    public void testMatchName2() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbata", SearchStyle.STRICT);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
    }

    /** Simple name match with style */
    @Test
    public void testMatchName3() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbatae", SearchStyle.STRICT);

        assertFalse(match.isSuccess());
    }

    /** Simple vernacular name match */
    @Test
    public void testMatchVernacular1() throws Exception {
        NameUsageMatch match = client.matchVernacular("Common Wombat");

        assertTrue(match.isSuccess());
        assertEquals("Vombatus ursinus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://biodiversity.org.au/afd/taxa/66d42847-c556-4fa3-902c-a91d9f517286", match.getTaxonConceptID());
    }

    @Test
    public void testGet1() throws Exception {
        NameUsageMatch match = client.get("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706");
        assertNotNull(match);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
    }

    @Test
    public void testGet2() throws Exception {
        NameUsageMatch match = client.get("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f");
        assertNotNull(match);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", match.getTaxonConceptID());
        assertEquals("Caretta esculenta", match.getScientificName());
    }

    @Test
    public void testGet3() throws Exception {
        NameUsageMatch match = client.get("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", false);
        assertNotNull(match);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", match.getTaxonConceptID());
        assertEquals("Caretta esculenta", match.getScientificName());
    }

    @Test
    public void testGet4() throws Exception {
        NameUsageMatch match = client.get("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", true);
        assertNotNull(match);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
    }

    @Test
    public void testGet5() throws Exception {
        NameUsageMatch match = client.get("Well, this is silly", true);
        assertNotNull(match);
        assertFalse(match.isSuccess());
    }

    @Test
    public void testGetAll1() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706",
                "https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f",
                "A random unknown id"
        );
        List<NameUsageMatch> matches = client.getAll(ids, false);
        assertNotNull(matches);
        assertEquals(ids.size(), matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
        match = matches.get(2);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f", match.getTaxonConceptID());
        assertEquals("Caretta esculenta", match.getScientificName());
        match = matches.get(3);
        assertFalse(match.isSuccess());
    }

    @Test
    public void testGetAll2() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706",
                "https://biodiversity.org.au/afd/taxa/a838ad85-6ead-4bd2-8741-75f571d7062f"
        );
        List<NameUsageMatch> matches = client.getAll(ids, true);
        assertNotNull(matches);
        assertEquals(ids.size(), matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
        match = matches.get(2);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
        assertEquals("SYNONYM", match.getSynonymType());
    }
}
