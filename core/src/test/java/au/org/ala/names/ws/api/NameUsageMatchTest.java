package au.org.ala.names.ws.api;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NameUsageMatchTest {
    @Test
    public void testCheck1() throws Exception {
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Fabales")
            .family("Polygalaceae")
            .genus("Polygala")
            .scientificName("Polygala arvensis")
            .build();
        NameSearch search = NameSearch.builder()
            .kingdom("Plantae")
            .scientificName("Polygala arvensis")
            .build();
        assertTrue(match.check(search));
    }

    @Test
    public void testCheck2() throws Exception {
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Caryophyllales")
            .family("Chenopodiaceae")
            .genus("Sarcocornia")
            .scientificName("Sarcocornia quinqueflora")
            .build();
        NameSearch search = NameSearch.builder()
            .kingdom("Plantae")
            .genus("Arthrocnemum")
            .scientificName("Arthrocnemum heptiflorum")
            .build();
        assertTrue(match.check(search));
    }

    @Test
    public void testCheck3() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae"));
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Caryophyllales")
            .family("Chenopodiaceae")
            .genus("Sarcocornia")
            .scientificName("Sarcocornia quinqueflora")
            .build();
        NameSearch search = NameSearch.builder()
            .genus("Arthrocnemum")
            .scientificName("Arthrocnemum heptiflorum")
            .hints(hints)
            .build();
        assertTrue(match.check(search));
    }

    @Test
    public void testCheck4() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Animalia"));
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Caryophyllales")
            .family("Chenopodiaceae")
            .genus("Sarcocornia")
            .scientificName("Sarcocornia quinqueflora")
            .build();
        NameSearch search = NameSearch.builder()
            .genus("Arthrocnemum")
            .scientificName("Arthrocnemum heptiflorum")
            .hints(hints)
            .build();
        assertFalse(match.check(search));
    }

    @Test
    public void testCheck5() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Fungi", "Plantae"));
        hints.put("family", Arrays.asList("Fabaceae", "Chenopodiaceae"));
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Caryophyllales")
            .family("Chenopodiaceae")
            .genus("Sarcocornia")
            .scientificName("Sarcocornia quinqueflora")
            .build();
        NameSearch search = NameSearch.builder()
            .genus("Arthrocnemum")
            .scientificName("Arthrocnemum heptiflorum")
            .hints(hints)
            .build();
        assertTrue(match.check(search));
    }

    @Test
    public void testCheck6() throws Exception {
        Map<String, List<String>> hints = new HashMap<>();
        hints.put("kingdom", Arrays.asList("Plantae"));
        hints.put("family", Arrays.asList("Fabaceae"));
        NameUsageMatch match = NameUsageMatch.builder()
            .kingdom("Plantae")
            .phylum("Charophyta")
            .classs("Equisetopsida")
            .order("Caryophyllales")
            .family("Chenopodiaceae")
            .genus("Sarcocornia")
            .scientificName("Sarcocornia quinqueflora")
            .build();
        NameSearch search = NameSearch.builder()
            .genus("Arthrocnemum")
            .scientificName("Arthrocnemum heptiflorum")
            .hints(hints)
            .build();
        assertFalse(match.check(search));
    }

}
