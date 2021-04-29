package au.org.ala.names.ws.resources;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.core.NameSearchConfiguration;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.junit.Assert.*;

public class NameSearchResourceTest {
    private NameSearchConfiguration configuration;
    private NameSearchResource resource;

    @Before
    public void setUp() throws Exception {
        ((Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME)).setLevel(Level.INFO); // Stop logging insanity
        this.configuration = new NameSearchConfiguration();
        this.configuration.setIndex("/data/lucene/namematching-20200214"); // Ensure consistent index
        this.configuration.setGroups(this.getClass().getResource("../core/test-groups-1.json"));
        this.configuration.setSubgroups(this.getClass().getResource("../core/test-subgroups-1.json"));
        this.resource = new NameSearchResource(this.configuration);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSearch1() throws Exception {
        NameUsageMatch match = this.resource.match("Acacia dealbata");
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("exactMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    // Vernacular name in scientific name
    @Test
    public void testSearch2() throws Exception {
        NameUsageMatch match = this.resource.match("Violet Daisy-Bush");
        assertTrue(match.isSuccess());
        assertEquals("Olearia iodochroa", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/node/apni/2908670", match.getTaxonConceptID());
        assertEquals("vernacularMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Acacia dealbata").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification2() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Osphranter rufus").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification3() throws Exception {
        NameSearch search = NameSearch.builder().genus("Osphranter").specificEpithet("rufus").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification4() throws Exception {
        NameUsageMatch match = this.resource.match("Osphranter rufus", null, null, null, null, null, null, null, null, null);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification5() throws Exception {
        NameUsageMatch match = this.resource.match(null, null, null, null, null, null, "Osphranter", "rufus", null, null);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByClassification6() throws Exception {
        NameUsageMatch match = this.resource.match(null, null, null, null, null, "Pterophoridae", null, null, null, null);
        assertTrue(match.isSuccess());
        assertEquals("Pterophoridae", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Pterophoridae", match.getFamily());
        assertEquals("family", match.getRank());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testHomonym1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Codium sp.").genus("Codium").family("Alga").build();
        NameUsageMatch match = this.resource.match(search);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("homonym"), match.getIssues());
    }

    @Test
    public void testHomonym2() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Agathis").build();
        NameUsageMatch match = this.resource.match(search);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("homonym"), match.getIssues());
        search = NameSearch.builder().scientificName("Agathis").phylum("Arthropoda").build();
        match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:a4109d9e-723c-491a-9363-95df428fe230", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints1() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("phylum", Arrays.asList("Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:a4109d9e-723c-491a-9363-95df428fe230", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints2() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("phylum", Arrays.asList("Chordata", "Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:a4109d9e-723c-491a-9363-95df428fe230", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints3() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Protista", "Fungi"));
        hints.put("phylum", Arrays.asList("Chordata", "Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:a4109d9e-723c-491a-9363-95df428fe230", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("hintMismatch"), match.getIssues());
    }

    @Test
    public void testHints4() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae", "Fungi"));
        hints.put("phylum", Arrays.asList("Chordata", "Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51299766", match.getTaxonConceptID()); // Uses Plantae hint first
        assertEquals(Collections.singletonList("hintMismatch"), match.getIssues());
    }

    @Test
    public void testHints5() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae", "Fungi"));
        hints.put("phylum", Arrays.asList("Charophyta", "Basidiomycota"));
        NameSearch search = NameSearch.builder().scientificName("Entorrhiza casparyana").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("65dc3de3-fca6-42d8-895c-d5b161cb4a6c", match.getTaxonConceptID()); // Uses Plantae hint first
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testMisapplied1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Corybas macranthus").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/node/apni/2915977", match.getTaxonConceptID());
        assertEquals(Arrays.asList("misappliedName"), match.getIssues());
    }

    @Test
    public void testSynonym1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Acacia derwentii").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("SCIENTIFIC", match.getNameType());
        assertEquals("SUBJECTIVE_SYNONYM", match.getSynonymType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testGetByTaxonID1() throws Exception {
        NameUsageMatch match = this.resource.get("https://id.biodiversity.org.au/taxon/apni/51286863");
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertNull(match.getNameType());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByVerncaularName1() throws Exception {
        NameUsageMatch match = this.resource.matchVernacular("Common Wombat");
        assertTrue(match.isSuccess());
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:e079f94d-3d7f-4deb-ae29-053fec4d1b53", match.getTaxonConceptID());
        assertEquals("Vombatus ursinus", match.getScientificName());
        assertEquals("INFORMAL", match.getNameType());
        assertEquals("vernacularMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testHybrid1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Eucalyptus globulus x Eucalyptus ovata").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/name/apni/152298", match.getTaxonConceptID());
        assertEquals("HYBRID", match.getNameType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
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
        Boolean result = this.resource.check("Macropus", "genus");
        assertNull(result);
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
        List<Map> result = this.resource.autocomplete("common w", 10, false);
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
    public void testSearchForLsidById1() throws Exception {
        // taxonID -> acceptedID
        String result = this.resource.searchForLsidById("urn:lsid:biodiversity.org.au:afd.name:05691642-5191-426a-b469-f1514b880481");
        assertNotNull(result);
        assertEquals("urn:lsid:biodiversity.org.au:afd.taxon:462548c3-6464-4e35-b71f-f4ad3fff3ebb", result);
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
        assertEquals(result, "https://id.biodiversity.org.au/taxon/apni/51302291");
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
        assertEquals(result.size(), 1);
        assertEquals(result.get(0), "https://id.biodiversity.org.au/taxon/apni/51302291");
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = this.resource.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(result.size(), 2);
        assertNull(result.get(0));
        assertEquals(result.get(1), "urn:lsid:biodiversity.org.au:afd.taxon:462548c3-6464-4e35-b71f-f4ad3fff3ebb");
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = this.resource.getCommonNamesForLSID("urn:lsid:biodiversity.org.au:afd.taxon:e079f94d-3d7f-4deb-ae29-053fec4d1b53", 10);
        assertNotNull(result);
        assertEquals(result.size(), 2);
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
