package au.org.ala.names.ws.resources;

import au.org.ala.names.ALANameSearcherConfiguration;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.api.SearchStyle;
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

public class NameTaxonomyResourceV1Test {
    private TaxonomyResource taxonomy;
    private NameSearchConfiguration configuration;
    private NameSearchResourceV1 resource;

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
        this.resource = new NameSearchResourceV1(this.configuration, this.taxonomy);
    }

    @After
    public void tearDown() throws Exception {
        this.taxonomy.close();
        this.resource.close();
    }

    @Test
    public void testSearch1() throws Exception {
        NameUsageMatch match = this.resource.match("Acacia dealbata", SearchStyle.MATCH);
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
        NameUsageMatch match = this.resource.match("Slender Violet-bush", SearchStyle.MATCH);
        assertTrue(match.isSuccess());
        assertEquals("Hybanthus monopetalus", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/node/apni/2887517", match.getTaxonConceptID());
        assertEquals("vernacularMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearch3() throws Exception {
        NameUsageMatch match = this.resource.match("Acacia dealbata", SearchStyle.STRICT);
        assertTrue(match.isSuccess());
        assertEquals("Acacia dealbata", match.getScientificName());
        assertEquals("species", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51286863", match.getTaxonConceptID());
        assertEquals("exactMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearch4() throws Exception {
        NameUsageMatch match = this.resource.match("Acacia otherwise", SearchStyle.MATCH);
        assertTrue(match.isSuccess());
        assertEquals("Acacia", match.getScientificName());
        assertEquals("genus", match.getRank());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51471290", match.getTaxonConceptID());
        assertEquals("higherMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearch5() throws Exception {
        NameUsageMatch match = this.resource.match("Acacia otherwise", SearchStyle.STRICT);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("noMatch"), match.getIssues());
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
        NameUsageMatch match = this.resource.match("Osphranter rufus", null, null, null, null, null, null, null, null, null, null);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification5() throws Exception {
        NameUsageMatch match = this.resource.match(null, null, null, null, null, null, "Osphranter", "rufus", null, null, null);
        assertTrue(match.isSuccess());
        assertEquals("Osphranter rufus", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("Osphranter", match.getGenus());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByClassification6() throws Exception {
        NameUsageMatch match = this.resource.match(null, null, null, null, null, "Pterophoridae", null, null, null, null, null);
        assertTrue(match.isSuccess());
        assertEquals("PTEROPHORIDAE", match.getScientificName());
        assertEquals("Animalia", match.getKingdom());
        assertEquals("PTEROPHORIDAE", match.getFamily());
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
        NameSearch search = NameSearch.builder().scientificName("https://id.biodiversity.org.au/taxon/apni/51440555").loose(true).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("Eucalyptus globulus", match.getScientificName());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51440555", match.getTaxonConceptID());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testSearchByClassification11() throws Exception {
        NameUsageMatch match = this.resource.match("Eucalyptus globula", null, null, null, null, null, null, null, null, null, SearchStyle.MATCH);
        assertTrue(match.isSuccess());
        assertEquals("Eucalyptus globulus", match.getScientificName());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51440555", match.getTaxonConceptID());
        assertEquals("fuzzyMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByClassification12() throws Exception {
        NameUsageMatch match = this.resource.match("Eucalyptus globula", null, null, null, null, null, null, null, null, null, SearchStyle.FUZZY);
        assertTrue(match.isSuccess());
        assertEquals("Eucalyptus globulus", match.getScientificName());
        assertEquals("https://id.biodiversity.org.au/taxon/apni/51440555", match.getTaxonConceptID());
        assertEquals("fuzzyMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }


    @Test
    public void testSearchByClassification13() throws Exception {
        NameUsageMatch match = this.resource.match("Eucalyptus globula", null, null, null, null, null, null, null, null, null, SearchStyle.STRICT);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("noMatch"), match.getIssues());
    }



    @Test
    public void testSearchByClassification14() throws Exception {
        NameUsageMatch match = this.resource.match("Eucalyptus rightmessofaname", null, null, null, null, null, null, null, null, null, SearchStyle.FUZZY);
        assertFalse(match.isSuccess());
        assertEquals(Collections.singletonList("noMatch"), match.getIssues());
    }

    @Test
    public void testSearchByClassificationVernacular1() throws Exception {
        NameUsageMatch match = this.resource.match("Osphranter rufus", null, null, null, null, null, null, null, null, null, SearchStyle.FUZZY);
        assertTrue(match.isSuccess());
        assertEquals("Red Kangaroo", match.getVernacularName());
    }

    // Test without preferred name in result
    @Test
    public void testSearchByClassificationVernacular2() throws Exception {
        this.resource.close();
        this.configuration.setPreferredVernacular(false);
        this.resource = new NameSearchResourceV1(this.configuration, this.taxonomy);
        NameUsageMatch match = this.resource.match("Osphranter rufus", null, null, null, null, null, null, null, null, null, SearchStyle.FUZZY);
        assertTrue(match.isSuccess());
        assertEquals("Red Kangaroo", match.getVernacularName());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/10357297-d91b-4c4c-8582-2cade1cf46d6", match.getTaxonConceptID());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/10357297-d91b-4c4c-8582-2cade1cf46d6", match.getTaxonConceptID());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHints2() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("phylum", Arrays.asList("Chordata", "Arthropoda"));
        NameSearch search = NameSearch.builder().scientificName("Agathis").hints(hints).build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/10357297-d91b-4c4c-8582-2cade1cf46d6", match.getTaxonConceptID());
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
        assertEquals("https://biodiversity.org.au/afd/taxa/10357297-d91b-4c4c-8582-2cade1cf46d6", match.getTaxonConceptID());
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
        assertEquals(Collections.singletonList("hintMismatch"), match.getIssues());
    }

    @Test
    public void testMisapplied1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Bertya rosmarinifolia").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/node/apni/2893214", match.getTaxonConceptID());
        assertEquals(Arrays.asList("matchedToMisappliedName"), match.getIssues());
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
        assertEquals("SCIENTIFIC", match.getNameType());
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
        assertEquals("SCIENTIFIC", match.getNameType());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testGetByTaxonID3() throws Exception {
        NameUsageMatch match = this.resource.get("NZOR-6-99065", true);
        assertTrue(match.isSuccess());
        assertEquals("NZOR-6-131797", match.getTaxonConceptID());
        assertEquals("Lens culinaris", match.getScientificName());
        assertEquals("SCIENTIFIC", match.getNameType());
        assertEquals("taxonIdMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
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
    public void testSearchByVerncaularName1() throws Exception {
        NameUsageMatch match = this.resource.matchVernacular("Common Wombat");
        assertTrue(match.isSuccess());
        assertEquals("https://biodiversity.org.au/afd/taxa/66d42847-c556-4fa3-902c-a91d9f517286", match.getTaxonConceptID());
        assertEquals("Vombatus ursinus", match.getScientificName());
        assertEquals("SCIENTIFIC", match.getNameType());
        assertEquals("vernacularMatch", match.getMatchType());
        assertEquals(Collections.singletonList("noIssue"), match.getIssues());
    }

    @Test
    public void testHybrid1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Eucalyptus globulus x Eucalyptus ovata").build();
        NameUsageMatch match = this.resource.match(search);
        assertTrue(match.isSuccess());
        assertEquals("https://id.biodiversity.org.au/name/apni/152298", match.getTaxonConceptID());
        assertEquals("INFORMAL", match.getNameType());
        assertEquals(Collections.singletonList("genericError"), match.getIssues()); // Unparsable hybrid name
    }


    @Test
    public void testGroups1() throws Exception {
        NameSearch search = NameSearch.builder().scientificName("Asparagus officinalis").build();
        NameUsageMatch match = this.resource.match(search);
        assertNotNull(match);
        assertTrue(match.isSuccess());
        assertEquals(2, match.getSpeciesGroup().size());
        assertEquals("Plants", match.getSpeciesGroup().get(0));
        assertEquals("Flowering Plants", match.getSpeciesGroup().get(1));
    }

}
