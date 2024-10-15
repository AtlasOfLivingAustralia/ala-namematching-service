package au.org.ala.names.ws.resources;

import au.org.ala.names.ALANameSearcherConfiguration;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class LookupResourceTest {
    private TaxonomyResource taxonomy;
    private NameSearchConfiguration configuration;
    private LookupResource resource;

    @Before
    public void setUp() throws Exception {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO); // Stop logging insanity
        ALANameSearcherConfiguration sc = ALANameSearcherConfiguration.builder()
                .index(new File("/data/lucene"))
                .version("20230725-5")
                .build();
        this.configuration = new NameSearchConfiguration();
        this.configuration.setSearcher(sc);
        this.configuration.setGroups(this.getClass().getResource("../core/test-groups-1.json"));
        this.configuration.setSubgroups(this.getClass().getResource("../core/test-subgroups-1.json"));
        this.taxonomy = new TaxonomyResource(this.configuration);
        this.resource = new LookupResource(this.configuration, this.taxonomy);
    }

    @After
    public void tearDown() throws Exception {
        this.taxonomy.close();
        this.resource.close();
    }

    @Test
    public void testGetNameTaxonID1() throws Exception {
        String name = this.resource.getName("https://id.biodiversity.org.au/taxon/apni/51286863", false);
        assertEquals("Acacia dealbata", name);
    }

    @Test
    public void testGetNameByTaxonID2() throws Exception {
        String name = this.resource.getName("NZOR-6-99065", false);
        assertEquals("Lens esculenta", name);
    }

    @Test
    public void testGetNameByTaxonID3() throws Exception {
        String name = this.resource.getName("NZOR-6-99065", true);
        assertEquals("Lens culinaris", name);
    }


    @Test
    public void testGetAllNamesByTaxonID1() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "NZOR-6-99065",
                "Nothing to be ashamed of"
        );
        List<String> matches = this.resource.getAllNames(ids, true);
        assertEquals(ids.size(), matches.size());
        assertEquals("Acacia dealbata", matches.get(0));
        assertEquals("Lens culinaris", matches.get(1));
        assertNull(matches.get(2));
    }

    @Test
    public void testCheck1() throws Exception {
        Boolean result = this.resource.check("Animalia", "kingdom");
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testCheck2() throws Exception {
        Boolean result = this.resource.check("Totally wired", "kingdom");
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testCheck3() throws Exception {
        Boolean result = this.resource.check("Thalia", "genus");
        assertNotNull(result);
        assertFalse(result);
    }

    @Test
    public void testCheck4() throws Exception {
        Boolean result = this.resource.check("Aves", "class");
        assertNotNull(result);
        assertTrue(result);
    }

    @Test
    public void testAutocomplete1() throws Exception {
        // incomplete scientific name match
        List<Map> result = this.resource.autocomplete("eucaly", 10, false);
        assertNotNull(result);
        assertTrue(result.size() > 0 && result.size() <= 20);
    }

    @Test
    public void testAutocomplete2() throws Exception {
        // incomplete vernacular name match
        List<Map> result = this.resource.autocomplete("womb", 10, false);
        assertNotNull(result);
        assertTrue(result.size() > 0 && result.size() <= 20);
    }

    @Test
    public void testAutocomplete3() throws Exception {
        // failed match
        List<Map> result = this.resource.autocomplete("abcdefg123", 10, false);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testAutocomplete4() throws Exception {
        // Get Elusor first
        List<Map> result = this.resource.autocomplete("Elusor", 10, false);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        Map first = result.get(0);
        assertEquals("Elusor", first.get("name"));
    }

    @Test
    public void testSearchForLsidById1() throws Exception {
        // taxonID -> acceptedID
        String result = this.resource.searchForLsidById("https://id.biodiversity.org.au/instance/apni/889096");
        assertNotNull(result);
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738745", result);
    }

    @Test
    public void testSearchForLsidById2() throws Exception {
        // invalid LSID
        String result = this.resource.searchForLsidById("invalid LSID");
        assertNull(result);
    }

    @Test
    public void testSearchForLSID1() throws Exception {
        // Genus
        String result = this.resource.searchForLSID("Eucalyptus");
        assertNotNull(result);
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738743", result);
    }

    @Test
    public void testSearchForLSID2() throws Exception {
        // Invalid match
        String result = this.resource.searchForLSID("invalid match");
        assertNull(result);
    }

    @Test
    public void testGetGuidsForTaxa1() throws Exception {
        // 1 genus match
        List<String> result = this.resource.getGuidsForTaxa(Collections.singletonList("Eucalyptus"));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51738743", result.get(0));
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = this.resource.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertNotNull(result.get(1));
        assertEquals( "https://biodiversity.org.au/afd/taxa/44f915ad-6090-42ba-a341-a11e47555f04", result.get(1));
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = this.resource.getCommonNamesForLSID("https://biodiversity.org.au/afd/taxa/66d42847-c556-4fa3-902c-a91d9f517286", 10);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Common Wombat"));
    }

    @Test
    public void testGetCommonNamesForLSID2() throws Exception {
        // invalid LSID
        Set<String> result = this.resource.getCommonNamesForLSID("invalid LSID", 10);
        assertNotNull(result);
        assertEquals(result.size(), 0);
    }

}
