package au.org.ala.names.ws.core;

import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.model.RankType;
import au.org.ala.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SpeciesGroupsUtilTest extends TestUtils {
    private NameSearchConfiguration configuration;
    private SpeciesGroupsUtil speciesGroupsUtil;

    @Before
    public void setUp() throws Exception {
        this.configuration = new NameSearchConfiguration();
        this.configuration.setIndex("/data/lucene/namematching-20210811-3"); // Assumed to be there
        this.configuration.setGroups(this.getClass().getResource("test-groups-1.json"));
        this.configuration.setSubgroups(this.getClass().getResource("test-subgroups-1.json"));
        this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(configuration);
    }

    protected int getLeft(String name)  throws Exception {
        NameSearchResult result =  this.speciesGroupsUtil
                        .getNameIndex()
                        .searchForRecord(name, RankType.SPECIES);
        if (result == null)
            throw new IllegalStateException("Expecting result for " + name);
        if (result.getAcceptedLsid() != null) {
            result = this.speciesGroupsUtil
                    .getNameIndex()
                    .searchForRecordByLsid(result.getAcceptedLsid());
        }
        if (result == null)
            throw new IllegalStateException("Expecting accepted result for " + name);
        return Integer.parseInt(result.getLeft());
    }

    protected SpeciesGroup getGroup(String name) {
        return this.speciesGroupsUtil.getSpeciesGroups().stream().filter(sg -> sg.name.equals(name)).findFirst().get();
    }

    protected SpeciesGroup getSubGroup(String name) {
        return this.speciesGroupsUtil.getSpeciesSubgroups().stream().filter(sg -> sg.name.equals(name)).findFirst().get();
    }

    // Ensure separate species groups are separate
    @Test
    public void testLoad1() throws Exception {
        SpeciesGroup mosses = this.getGroup("Mosses");
        SpeciesGroup ferns = this.getGroup("Ferns And Allies");
        assertNotNull(mosses);
        assertNotNull(ferns);
        assertFalse(mosses.overlaps(ferns));
    }

    // Ensure separate species subgroups are separate
    @Test
    public void testLoad2() throws Exception {
        SpeciesGroup monocots = this.getSubGroup("Monocots");
        SpeciesGroup dicots = this.getSubGroup("Dicots");
        assertNotNull(monocots);
        assertNotNull(dicots);
        assertFalse(monocots.overlaps(dicots));
    }

    @Test
    public void testGetGroup1() throws Exception {
        int left = this.getLeft("Thylacinus cynocephalus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetSubGroup1() throws Exception {
        int left = this.getLeft("Thylacinus cynocephalus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Carnivorous Marsupials"), groups);
    }

    @Test
    public void testGetGroup2() throws Exception {
        int left = this.getLeft("Osphranter rufus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetSubGroup2() throws Exception {
        int left = this.getLeft("Osphranter rufus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Herbivorous Marsupials"), groups);
    }


    @Test
    public void testGetGroup3() throws Exception {
        int left = this.getLeft("Notoryctes caurinus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetSubGroup3() throws Exception {
        int left = this.getLeft("Notoryctes caurinus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Marsupial Moles"), groups);
    }


    @Test
    public void testGetGroup4() throws Exception {
        int left = this.getLeft("Perameles gunnii");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetSubGroup4() throws Exception {
        int left = this.getLeft("Perameles gunnii");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Bandicoots, Bilbies"), groups);
    }


    @Test
    public void testGetGroup5() throws Exception {
        int left = this.getLeft("Crocidura trichura");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetSubGroup5() throws Exception {
        int left = this.getLeft("Crocidura trichura");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup6() throws Exception {
        int left = this.getLeft("Anas superciliosa");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Birds"), groups);
    }

    @Test
    public void testGetSubGroup6() throws Exception {
        int left = this.getLeft("Anas superciliosa");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Ducks, Geese, Swans"), groups);
    }


    @Test
    public void testGetGroup7() throws Exception {
        int left = this.getLeft("Apus pacificus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Birds"), groups);
    }

    @Test
    public void testGetSubGroup7() throws Exception {
        int left = this.getLeft("Apus pacificus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Hummingbirds, Swifts"), groups);
    }

    @Test
    public void testGetGroup8() throws Exception {
        int left = this.getLeft("Dromaius novaehollandiae");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Birds"), groups);
    }

    @Test
    public void testGetSubGroup8() throws Exception {
        int left = this.getLeft("Dromaius novaehollandiae");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup9() throws Exception {
        int left = this.getLeft("Chelodina longicollis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Reptiles"), groups);
    }

    @Test
    public void testGetSubGroup9() throws Exception {
        int left = this.getLeft("Chelodina longicollis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup10() throws Exception {
        int left = this.getLeft("Litoria burrowsae");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Amphibians"), groups);
    }

    @Test
    public void testGetSubGroup10() throws Exception {
        int left = this.getLeft("Litoria burrowsae");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup11() throws Exception {
        int left = this.getLeft("Anguilla anguilla");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Fishes"), groups);
    }

    @Test
    public void testGetSubGroup11() throws Exception {
        int left = this.getLeft("Anguilla anguilla");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup12() throws Exception {
        int left = this.getLeft("Carditella mawsoni");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Molluscs"), groups);
    }

    @Test
    public void testGetSubGroup12() throws Exception {
        int left = this.getLeft("Carditella mawsoni");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }
    @Test
    public void testGetGroup13() throws Exception {
        int left = this.getLeft("Australobranchipus gilgaiphila");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Arthropods", "Crustaceans"), groups);
    }

    @Test
    public void testGetSubGroup13() throws Exception {
        int left = this.getLeft("Australobranchipus gilgaiphila");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup14() throws Exception {
        int left = this.getLeft("Xenolepisma monteithi");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Arthropods", "Insects"), groups);
    }

    @Test
    public void testGetSubGroup14() throws Exception {
        int left = this.getLeft("Xenolepisma monteithi");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup15() throws Exception {
        int left = this.getLeft("Siphonotus brevicornis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Arthropods"), groups);
    }

    @Test
    public void testGetSubGroup15() throws Exception {
        int left = this.getLeft("Siphonotus brevicornis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup16() throws Exception {
        int left = this.getLeft("Acacia dealbata");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Flowering Plants"), groups);
    }

    @Test
    public void testGetSubGroup16() throws Exception {
        int left = this.getLeft("Acacia dealbata");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Dicots"), groups);
    }

    // Issue 53
    @Test
    public void testGetGroup17() throws Exception {
        int left = this.getLeft("Asparagus officinalis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Flowering Plants"), groups);
    }

    @Test
    public void testGetSubGroup17() throws Exception {
        int left = this.getLeft("Asparagus officinalis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Monocots"), groups);
    }

    @Test
    public void testGetGroup18() throws Exception {
        int left = this.getLeft("Cycas arenicola");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Conifers, Cycads"), groups);
    }

    @Test
    public void testGetSubGroup18() throws Exception {
        int left = this.getLeft("Cycas arenicola");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }


    @Test
    public void testGetGroup19() throws Exception {
        int left = this.getLeft("Equisetum arvense");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Ferns And Allies"), groups);
    }

    @Test
    public void testGetSubGroup19() throws Exception {
        int left = this.getLeft("Equisetum arvense");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup20() throws Exception {
        int left = this.getLeft("Brachymenium cellulare");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Mosses"), groups);
    }

    @Test
    public void testGetSubGroup20() throws Exception {
        int left = this.getLeft("Brachymenium cellulare");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup21() throws Exception {
        int left = this.getLeft("Antromycopsis leucopogonis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Fungi"), groups);
    }

    @Test
    public void testGetSubGroup21() throws Exception {
        int left = this.getLeft("Antromycopsis leucopogonis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup22() throws Exception {
        int left = this.getLeft("Dinophysis acuminata");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Chromista"), groups);
    }

    @Test
    public void testGetSubGroup22() throws Exception {
        int left = this.getLeft("Dinophysis acuminata");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup23() throws Exception {
        int left = this.getLeft("Parvicorbicula socialis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Protozoa"), groups);
    }

    @Test
    public void testGetSubGroup23() throws Exception {
        int left = this.getLeft("Parvicorbicula socialis");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup24() throws Exception {
        int left = this.getLeft("Caldisericum exile");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Bacteria"), groups);
    }

    @Test
    public void testGetSubGroup24() throws Exception {
        int left = this.getLeft("Caldisericum exile");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

    @Test
    public void testGetGroup25() throws Exception {
        int left = this.getLeft("Plagioselmis prolonga");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Chromista", "Algae"), groups);
    }

    @Test
    public void testGetSubGroup25() throws Exception {
        int left = this.getLeft("Plagioselmis prolonga");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList(), groups);
    }

}
