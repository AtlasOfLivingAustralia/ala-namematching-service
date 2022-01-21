package au.org.ala.ws;

import au.org.ala.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cache2k.Cache;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DataCacheConfigurationTest extends TestUtils {
    @Test
    public void testReadJSON1() throws Exception {
        ObjectMapper om = new ObjectMapper();
        DataCacheConfiguration configuration = om.readValue(this.getClass().getResource("cache-config-1.json"), DataCacheConfiguration.class);
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
        DataCacheConfiguration configuration = om.readValue(this.getClass().getResource("cache-config-2.json"), DataCacheConfiguration.class);
        assertEquals(20000, configuration.getEntryCapacity());
        assertEquals(false, configuration.isEnableJmx());
        assertEquals(true, configuration.isEternal());
        assertEquals(false, configuration.isKeepDataAfterExpired());
        assertEquals(true, configuration.isPermitNullValues());
        assertEquals(false, configuration.isSuppressExceptions());
    }

    @Test
    public void testWriteJSON1() throws Exception {
        DataCacheConfiguration configuration = DataCacheConfiguration.builder()
                .entryCapacity(20000)
                .enableJmx(true)
                .eternal(false)
                .keepDataAfterExpired(true)
                .permitNullValues(false)
                .suppressExceptions(true)
                .build();
        ObjectMapper om = new ObjectMapper();
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(writer.toString(), this.getResource("cache-config-1.json"));
    }

    @Test
    public void testWriteJSON2() throws Exception {
        DataCacheConfiguration configuration = DataCacheConfiguration.builder()
                .entryCapacity(20000)
                .build();
        ObjectMapper om = new ObjectMapper();
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(writer.toString(), this.getResource("cache-config-2.json"));
    }

    @Test
    public void testCreateBuilder1() throws Exception {
        DataCacheConfiguration configuration = DataCacheConfiguration.builder()
                .entryCapacity(20000)
                .build();
        Cache<String, String> cache = configuration.cacheBuilder(String.class, String.class).loader(k -> k + "X").build();
        assertNull(cache.peek("hello"));
        Assert.assertEquals("helloX", cache.get("hello"));
        Assert.assertEquals("helloX", cache.peek("hello"));
        Assert.assertEquals("fredX", cache.get("fred"));
    }


}
