package au.org.ala.names.ws.client;

import au.org.ala.names.ws.ALANameMatchingServiceApplication;
import au.org.ala.names.ws.ALANameMatchingServiceConfiguration;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.util.TestUtils;
import au.org.ala.ws.ClientConfiguration;
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
        this.configuration = ClientConfiguration.builder().baseUrl(new URL(NAMEMATCHING_SERVER_URL)).build();
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

    /** Simple request/response */
    @Test
    public void testMatchNameParams1() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbata", "Plantae", null, null, null, null, null, null, null, null);

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
    }

    /** Simple name match */
    @Test
    public void testMatchName1() throws Exception {
        NameUsageMatch match = client.match("Acacia dealbata");

        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
    }

    /** Simple vernacular name match */
    @Test
    public void testMatchVernacular1() throws Exception {
        NameUsageMatch match = client.matchVernacular("Common Wombat");

        assertTrue(match.isSuccess());
        assertEquals("Vombatus ursinus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:e079f94d-3d7f-4deb-ae29-053fec4d1b53", match.getTaxonConceptID());
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
        List<Map> result = client.autocomplete("common w", 10, false);
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
        String result = client.searchForLsidById("urn:lsid:biodiversity.org.au:afd.name:05691642-5191-426a-b469-f1514b880481");
        assertNotNull(result);
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:462548c3-6464-4e35-b71f-f4ad3fff3ebb", result);
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
        assertEquals(result, "https://id.biodiversity.org.au/taxon/apni/51302291");
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
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51302291", result.get(0));
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = client.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertNull(result.get(0));
        assertEquals(result.get(1), "urn:lsid:biodiversity.org.au:afd.taxon:462548c3-6464-4e35-b71f-f4ad3fff3ebb");
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = client.getCommonNamesForLSID("urn:lsid:biodiversity.org.au:afd.taxon:e079f94d-3d7f-4deb-ae29-053fec4d1b53", 10);
        assertNotNull(result);
        assertEquals(result.size(), 2);
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
