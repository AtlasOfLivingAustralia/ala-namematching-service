package au.org.ala.names.ws.core;

import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.model.RankType;
import au.org.ala.names.search.ALANameSearcher;
import au.org.ala.util.TestUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SpeciesGroupsUtilTest extends TestUtils {
    // Shared across all test methods — ALANameSearcher is expensive to open and
    // does not implement Closeable, so one instance per suite is the right approach.
    private static ALANameSearcher sharedSearcher;
    private static NameSearchConfiguration sharedConfiguration;

    private SpeciesGroupsUtil speciesGroupsUtil;

    @BeforeClass
    public static void setUpClass() throws Exception {
        sharedConfiguration = new NameSearchConfiguration();
        sharedConfiguration.setIndex("/data/lucene/namematching-20210811-3"); // Assumed to be there
        sharedConfiguration.setGroups(SpeciesGroupsUtilTest.class.getResource("test-groups-1.json"));
        sharedConfiguration.setSubgroups(SpeciesGroupsUtilTest.class.getResource("test-subgroups-1.json"));
        sharedSearcher = new ALANameSearcher(sharedConfiguration.getIndex());
    }

    @AfterClass
    public static void tearDownClass() {
        // ALANameSearcher does not implement Closeable; null out to allow GC of the
        // index reader between test suites rather than holding it for JVM lifetime.
        sharedSearcher = null;
    }

    @Before
    public void setUp() throws Exception {
        this.speciesGroupsUtil = SpeciesGroupsUtil.getInstance(sharedConfiguration, sharedSearcher);
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

    @Test
    public void testGetGroup1() throws Exception {
        int left = this.getLeft("Osphranter rufus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Mammals"), groups);
    }

    @Test
    public void testGetGroup2() throws Exception {
        int left = this.getLeft("Dromaius novaehollandiae");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Animals", "Birds"), groups);
    }

    @Test
    public void testGetGroup3() throws Exception {
        int left = this.getLeft("Acacia dealbata");
        List<String> groups = this.speciesGroupsUtil.getSpeciesGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Plants", "Angiosperms", "Dicots"), groups);
    }

    @Test
    public void testGetSubgroup1() throws Exception {
        int left = this.getLeft("Osphranter rufus");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Herbivorous Marsupials"), groups);
    }

    @Test
    public void testGetSubgroup2() throws Exception {
        int left = this.getLeft("Anas superciliosa");
        List<String> groups = this.speciesGroupsUtil.getSpeciesSubGroups(left);
        assertNotNull(groups);
        assertEquals(Arrays.asList("Ducks, Geese, Swans"), groups);
    }
}
