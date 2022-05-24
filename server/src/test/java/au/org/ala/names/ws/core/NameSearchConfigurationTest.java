package au.org.ala.names.ws.core;

import au.org.ala.util.TestUtils;
import au.org.ala.ws.DataCacheConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.Test;

import java.io.StringWriter;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class NameSearchConfigurationTest extends TestUtils {
    @Test
    public void testReadJSON1() throws Exception {
        ObjectMapper om = new ObjectMapper();
        NameSearchConfiguration configuration = om.readValue(this.getClass().getResource("name-search-config-1.json"), NameSearchConfiguration.class);
        assertEquals("/data/lucene/namematching-20210811-3", configuration.getIndex());
        assertEquals("http://nowhere.com/groups", configuration.getGroups().toExternalForm());
        assertEquals("http://nowhere.com/subgroups", configuration.getSubgroups().toExternalForm());
        assertEquals(true, configuration.getCache().isEnableJmx());
    }

    @Test
    public void testWriteJSON1() throws Exception {
        NameSearchConfiguration configuration = new NameSearchConfiguration();
        configuration.setIndex("/data/lucene/namematching-20210811-3");
        configuration.setGroups(new URL("http://nowhere.com/groups"));
        configuration.setSubgroups(new URL("http://nowhere.com/subgroups"));
        configuration.setCache(DataCacheConfiguration.builder().enableJmx(true).build());
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        System.out.println(writer.toString());
        assertEquals(writer.toString(), this.getResource("name-search-config-1.json"));
    }

}
