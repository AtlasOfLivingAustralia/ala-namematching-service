package au.org.ala.names.ws.core;

import au.org.ala.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpeciesGroupsUtilTest extends TestUtils {
    private NameSearchConfiguration configuration;
    private SpeciesGroupsUtil speciesGroupsUtil;

    @Before
    public void setUp() throws Exception {
        this.configuration = new NameSearchConfiguration();
        this.configuration.setIndex("/data/lucene/namematching-20210811"); // Assumed to be there
        this.configuration.setGroups(this.getClass().getResource("test-groups-1.json"));
        this.configuration.setSubgroups(this.getClass().getResource("test-subgroups-1.json"));
        this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(configuration);
    }

    @Test
    public void testGetGroup1() throws Exception {
        // Osphranter rufus
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(85347);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetGroup2() throws Exception {
        // Dromaius novaehollandiae
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(62481);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Birds"), groups);
    }

    @Test
    public void testGetGroup3() throws Exception {
        // Acacia dealbata
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(732274);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Angiosperms", "Dicots"), groups);
    }

    @Test
    public void testGetSubgroup1() throws Exception {
        // Osphranter rufus
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(85347);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Herbivorous Marsupials"), groups);
    }

    @Test
    public void testGetSubgroup2() throws Exception {
        // Anas superciliosa
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(62250);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Ducks, Geese, Swans"), groups);
    }
}
