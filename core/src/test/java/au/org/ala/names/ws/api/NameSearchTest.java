package au.org.ala.names.ws.api;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class NameSearchTest {
    @Test
    public void testNormalised1() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .build();
        NameSearch search2 = search1.normalised();
        assertEquals(search1, search2);
    }

    @Test
    public void testInferred1() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .build();
        NameSearch search2 = search1.inferred();
        assertEquals(search1, search2);
    }

    @Test
    public void testInferred2() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .genus("Acacia")
            .specificEpithet("dealbata")
            .build();
        NameSearch search2 = search1.inferred();
        assertNotEquals(search1, search2);
        assertEquals("Acacia dealbata", search2.getScientificName());
        assertEquals("species", search2.getRank());
    }

    @Test
    public void testInferred3() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .kingdom("Plantae")
            .genus("Acacia")
            .build();
        NameSearch search2 = search1.inferred();
        assertNotEquals(search1, search2);
        assertEquals("Acacia", search2.getScientificName());
        assertEquals("genus", search2.getRank());
    }

    @Test
    public void testHinted1() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(1, hinted.size());
        assertEquals(search1, hinted.get(0));
    }

    @Test
    public void testHinted2() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae"));
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .hints(hints)
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(2, hinted.size());
        NameSearch result;
        result = hinted.get(0);
        assertEquals(search1, result);
        result = hinted.get(1);
        assertEquals("Plantae", result.getKingdom());
    }

    @Test
    public void testHinted3() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae"));
        hints.put("family", Arrays.asList("Fabaceae"));
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .hints(hints)
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(4, hinted.size());
        NameSearch result;
        result = hinted.get(0);
        assertEquals(search1, result);
        result = hinted.get(1);
        assertEquals("Plantae", result.getKingdom());
        assertNull(result.getFamily());
        result = hinted.get(2);
        assertNull(result.getKingdom());
        assertEquals("Fabaceae", result.getFamily());
        result = hinted.get(3);
        assertEquals("Plantae", result.getKingdom());
        assertEquals("Fabaceae", result.getFamily());
    }

    @Test
    public void testHinted4() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae", "Fungi"));
        NameSearch search1 = NameSearch.builder()
            .scientificName("Acacia dealbata")
            .hints(hints)
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(3, hinted.size());
        NameSearch result;
        result = hinted.get(0);
        assertEquals(search1, result);
        result = hinted.get(1);
        assertEquals("Plantae", result.getKingdom());
        result = hinted.get(2);
        assertEquals("Fungi", result.getKingdom());
    }

    @Test
    public void testHinted5() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae", "Fungi"));
        NameSearch search1 = NameSearch.builder()
            .kingdom("Animalia")
            .scientificName("Osphranter rufus")
            .hints(hints)
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(1, hinted.size());
        NameSearch result;
        result = hinted.get(0);
        assertEquals(search1, result);
    }

    @Test
    public void testHinted6() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae", "Fungi"));
        hints.put("family", Arrays.asList("Fabaceae"));
        NameSearch search1 = NameSearch.builder()
            .kingdom("Animalia")
            .scientificName("Osphranter rufus")
            .hints(hints)
            .build();
        List<NameSearch> hinted = search1.hintStream().collect(Collectors.toList());
        assertEquals(2, hinted.size());
        NameSearch result;
        result = hinted.get(0);
        assertEquals(search1, result);
        result = hinted.get(1);
        assertEquals("Animalia", result.getKingdom());
        assertEquals("Fabaceae", result.getFamily());
    }

    @Test
    public void testBare1() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .genus("Acacia")
            .specificEpithet("dealbata")
            .scientificName("Acacia dealbata")
            .build();
        List<NameSearch> bare = search1.bareStream().collect(Collectors.toList());
        assertEquals(1, bare.size());
        NameSearch result;
        result = bare.get(0);
        assertEquals(search1, result);
    }

    @Test
    public void testBare2() throws Exception {
        NameSearch search1 = NameSearch.builder()
            .genus("Acacia")
            .specificEpithet("dealbata")
            .build();
        List<NameSearch> bare = search1.bareStream().collect(Collectors.toList());
        assertEquals(2, bare.size());
        NameSearch result;
        result = bare.get(0);
        assertNotEquals(search1, result);
        assertEquals("Acacia dealbata", result.getScientificName());
        result = bare.get(1);
        assertEquals(search1, result);
    }

}
