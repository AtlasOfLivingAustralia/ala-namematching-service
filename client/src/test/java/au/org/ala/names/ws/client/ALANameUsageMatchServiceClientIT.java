package au.org.ala.names.ws.client;

import au.org.ala.names.ws.ALANameMatchingServiceApplication;
import au.org.ala.names.ws.ALANameMatchingServiceConfiguration;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.util.TestUtils;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.DataCacheConfiguration;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

public class ALANameUsageMatchServiceClientIT extends TestUtils {

    private ClientConfiguration configuration;
    private ALANameUsageMatchServiceClient client;
    private String NAMEMATCHING_SERVER_URL = "http://localhost:9179";

        public static final DropwizardTestSupport<ALANameMatchingServiceConfiguration> SUPPORT =
            new DropwizardTestSupport<ALANameMatchingServiceConfiguration>(ALANameMatchingServiceApplication.class,
                    ResourceHelpers.resourceFilePath("config.yml")
            );

    @Before
    public void setUp() throws Exception {
        SUPPORT.before();
        DataCacheConfiguration dataCacheConfig = DataCacheConfiguration.builder().build();
        this.configuration = ClientConfiguration.builder()
                .baseUrl(new URL(NAMEMATCHING_SERVER_URL))
                .dataCache(dataCacheConfig)
                .build();
        this.client = new ALANameUsageMatchServiceClient(configuration);
    }

    @After
    public void tearDown() throws Exception {
        if (SUPPORT != null) {
            SUPPORT.after();
        }
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
        NameSearch search = NameSearch.builder().scientificName("Macropus").build();
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
        assertEquals("https://biodiversity.org.au/afd/taxa/e6aff6af-ff36-4ad5-95f2-2dfdcca8caff", match.getTaxonConceptID());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/e6aff6af-ff36-4ad5-95f2-2dfdcca8caff", match.getTaxonConceptID());

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
        assertEquals("https://biodiversity.org.au/afd/taxa/e6aff6af-ff36-4ad5-95f2-2dfdcca8caff", match.getTaxonConceptID());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/e079f94d-3d7f-4deb-ae29-053fec4d1b53", match.getTaxonConceptID());
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
        String result = client.searchForLsidById("https://biodiversity.org.au/afd/taxa/05691642-5191-426a-b469-f1514b880481");
        assertNotNull(result);
        assertEquals("https://biodiversity.org.au/afd/taxa/462548c3-6464-4e35-b71f-f4ad3fff3ebb", result);
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
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51360942", result);
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
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51360942", result.get(0));
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = client.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertEquals(result.get(1), "https://biodiversity.org.au/afd/taxa/462548c3-6464-4e35-b71f-f4ad3fff3ebb");
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = client.getCommonNamesForLSID("https://biodiversity.org.au/afd/taxa/e079f94d-3d7f-4deb-ae29-053fec4d1b53", 10);
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
