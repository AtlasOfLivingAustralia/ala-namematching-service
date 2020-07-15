package au.org.ala.names.ws.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import au.org.ala.util.TestUtils;
import org.cache2k.Cache;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.*;

public class CacheConfigurationTest extends TestUtils {
    @Test
    public void testReadJSON1() throws Exception {
        ObjectMapper om = new ObjectMapper();
        CacheConfiguration configuration = om.readValue(this.getClass().getResource("cache-config-1.json"), CacheConfiguration.class);
        assertEquals(20000, configuration.getEntryCapacity());
        assertEquals(true, configuration.isEnableJmx());
        assertEquals(false, configuration.isEternal());
        assertEquals(true, configuration.isKeepDataAfterExpired());
        assertEquals(false, configuration.isPermitNullValues());
        assertEquals(true, configuration.isSuppressExceptions());
    }

    @Test
    public void testReadJSON2() throws Exception {
        ObjectMapper om = new ObjectMapper();
        CacheConfiguration configuration = om.readValue(this.getClass().getResource("cache-config-2.json"), CacheConfiguration.class);
        assertEquals(20000, configuration.getEntryCapacity());
        assertEquals(false, configuration.isEnableJmx());
        assertEquals(true, configuration.isEternal());
        assertEquals(false, configuration.isKeepDataAfterExpired());
        assertEquals(true, configuration.isPermitNullValues());
        assertEquals(false, configuration.isSuppressExceptions());
    }

    @Test
    public void testWriteJSON1() throws Exception {
        CacheConfiguration configuration = new CacheConfiguration();
        configuration.setEntryCapacity(20000);
        configuration.setEnableJmx(true);
        configuration.setEternal(false);
        configuration.setKeepDataAfterExpired(true);
        configuration.setPermitNullValues(false);
        configuration.setSuppressExceptions(true);
        ObjectMapper om = new ObjectMapper();
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(writer.toString(), this.getResource("cache-config-1.json"));
    }

    @Test
    public void testWriteJSON2() throws Exception {
        CacheConfiguration configuration = new CacheConfiguration();
        configuration.setEntryCapacity(20000);
        ObjectMapper om = new ObjectMapper();
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(writer.toString(), this.getResource("cache-config-2.json"));
    }

    @Test
    public void testCreateBuilder1() throws Exception {
        CacheConfiguration configuration = new CacheConfiguration();
        configuration.setEntryCapacity(20000);
        Cache<String, String> cache = configuration.builder(String.class, String.class).loader(k -> k + "X").build();
        assertNull(cache.peek("hello"));
        assertEquals("helloX", cache.get("hello"));
        assertEquals("helloX", cache.peek("hello"));
        assertEquals("fredX", cache.get("fred"));
    }


}
