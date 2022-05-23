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
        this.configuration.setIndex("/data/lucene/namematching-20210811-3"); // Ensure consistent index
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
        NameUsageMatch match = this.resource.match("Slender Violet-bush");
        assertTrue(match.isSuccess());
        assertEquals("Hybanthus monopetalus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/node/apni/2887517", match.getTaxonConceptID());
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
        assertEquals("PTEROPHORIDAE", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Pterophoridae", match.getFamily());
        assertEquals("family", match.getRank());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByClassification7() throws Exception {
        NameSearch search = NameSearch.builder().genus("Osphranter").specificEpithet("rufus").loose(false).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification8() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Blue gum").loose(false).build();
        NameUsageMatch match = this.resource.match(search);
        assertFalse(match.isSuccess());
    }

    @Test
    public void testSearchByClassification9() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Splendid Telopea").loose(true).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Telopea speciosissima", match.getScientificName());
        assertEquals("Plantae", match.getKingdom());
        assertEquals("vernacularMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification10() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("https://id.biodiversity.org.au/node/apni/5272169").loose(true).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Eucalyptus globulus", match.getScientificName());
        assertEquals("https://id.biodiversity.org.au/node/apni/5272169", match.getTaxonConceptID());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testHomonym1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Thalia").build();
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
        assertEquals("https://biodiversity.org.au/afd/taxa/d02923bc-cf54-4d7f-ae74-aac1d6af1830", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHomonym3() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Thalia").kingdom("Animalia").phylum("Chordata").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/52c68649-47d5-4f2e-9730-417fc54fb080", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints1() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("phylum", Arrays.asList("Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/d02923bc-cf54-4d7f-ae74-aac1d6af1830", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints2() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("phylum", Arrays.asList("Chordata", "Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/d02923bc-cf54-4d7f-ae74-aac1d6af1830", match.getTaxonConceptID());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/d02923bc-cf54-4d7f-ae74-aac1d6af1830", match.getTaxonConceptID());
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
        assertEquals("https://id.biodiversity.org.au/node/fungi/60100871", match.getTaxonConceptID()); // Uses Plantae hint first
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testMisapplied1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Corybas macranthus").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51401037", match.getTaxonConceptID());
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
    public void testGetByTaxonID2() throws Exception {
        // Search by synonym ID
        NameUsageMatch match = this.resource.get("NZOR-6-99065", false);
        assertTrue(match.isSuccess());
        assertEquals("NZOR-6-99065", match.getTaxonConceptID());
        assertEquals("Lens esculenta", match.getScientificName());
        assertNull(match.getNameType());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testGetByTaxonID3() throws Exception {
        NameUsageMatch match = this.resource.get("NZOR-6-99065", true);
        assertTrue(match.isSuccess());
        assertEquals("NZOR-6-131797", match.getTaxonConceptID());
        assertEquals("Lens culinaris", match.getScientificName());
        assertNull(match.getNameType());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
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
    public void testGetAllByTaxonID1() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706"
        );
        List<NameUsageMatch> matches = this.resource.getAll(ids, false);
        assertEquals(ids.size(), matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/2d605472-979b-49b4-aed3-03a384e9f706", match.getTaxonConceptID());
        assertEquals("Chelonia mydas", match.getScientificName());
    }

    @Test
    public void testGetAllByTaxonID2() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "NothingToSeeHere"
        );
        List<NameUsageMatch> matches = this.resource.getAll(ids, false);
        assertEquals(ids.size(), matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        match = matches.get(1);
        assertFalse(match.isSuccess());
    }

    @Test
    public void testGetAllByTaxonID3() throws Exception {
        List<String> ids = Arrays.asList(
                "https://id.biodiversity.org.au/taxon/apni/51286863",
                "NZOR-6-99065"
        );
        List<NameUsageMatch> matches = this.resource.getAll(ids, true);
        assertEquals(ids.size(), matches.size());
        NameUsageMatch match = matches.get(0);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("Acacia dealbata", match.getScientificName());
        match = matches.get(1);
        assertTrue(match.isSuccess());
        assertEquals("NZOR-6-131797", match.getTaxonConceptID());
        assertEquals("Lens culinaris", match.getScientificName());
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
    public void testSearchByVerncaularName1() throws Exception {
        NameUsageMatch match = this.resource.matchVernacular("Common Wombat");
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/e079f94d-3d7f-4deb-ae29-053fec4d1b53", match.getTaxonConceptID());
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
        String result = this.resource.searchForLsidById("https://biodiversity.org.au/afd/taxa/05691642-5191-426a-b469-f1514b880481");
        assertNotNull(result);
        assertEquals("https://biodiversity.org.au/afd/taxa/462548c3-6464-4e35-b71f-f4ad3fff3ebb", result);
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
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51360942", result);
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
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51360942", result.get(0));
    }

    @Test
    public void testGetGuidsForTaxa2() throws Exception {
        // 1 match to fail, 1 species match
        List<String> result = this.resource.getGuidsForTaxa(Arrays.asList("no match", "Macropus agilis"));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertNull(result.get(0));
        assertEquals( "https://biodiversity.org.au/afd/taxa/462548c3-6464-4e35-b71f-f4ad3fff3ebb", result.get(1));
    }

    @Test
    public void testGetCommonNamesForLSID1() throws Exception {
        // LSID with >1 common names
        Set<String> result = this.resource.getCommonNamesForLSID("https://biodiversity.org.au/afd/taxa/e079f94d-3d7f-4deb-ae29-053fec4d1b53", 10);
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
