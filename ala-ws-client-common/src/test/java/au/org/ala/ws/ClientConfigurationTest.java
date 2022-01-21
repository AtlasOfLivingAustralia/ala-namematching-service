package au.org.ala.ws;

import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public class ClientConfigurationTest {
    @Test
    public void testBuild1() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        builder.baseUrl(new URL("http://localhost:90"));
        ClientConfiguration configuration = builder.build();
        assertEquals("http://localhost:90", configuration.getBaseUrl().toExternalForm());
        assertEquals(30000, configuration.getTimeOut());
        assertEquals(true, configuration.isCache());
        assertEquals(50 * 1024 * 1024, configuration.getCacheSize());
        assertEquals(null, configuration.getCacheDir());
    }

    @Test
    public void testBuild2() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        builder.timeOut(5000);
        ClientConfiguration configuration = builder.build();
        assertEquals(5000, configuration.getTimeOut());
    }

    @Test
    public void testBuild3() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        builder.cacheDir(Paths.get("file:/tmp"));
        ClientConfiguration configuration = builder.build();
        assertEquals("file:/tmp", configuration.getCacheDir().toString());
    }

    @Test
    public void testBuild4() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        builder.cache(false);
        ClientConfiguration configuration = builder.build();
        assertEquals(false, configuration.isCache());
    }

    @Test
    public void testBuild5() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        builder.cacheSize(2000);
        ClientConfiguration configuration = builder.build();
        assertEquals(2000, configuration.getCacheSize());
    }

    @Test
    public void testBuild6() throws Exception {
        ClientConfiguration.ClientConfigurationBuilder builder = ClientConfiguration.builder();
        DataCacheConfiguration dataCache = DataCacheConfiguration.builder().build();
        builder.dataCache(dataCache);
        ClientConfiguration configuration = builder.build();
        Optional<Cache2kBuilder<String, String>> cacheBuilder = configuration.buildDataCache(String.class, String.class);
        assertNotNull(cacheBuilder);
        Optional<Cache<String, String>> cache = cacheBuilder.map(b -> b.loader(k -> k + ":loaded").build());
        assertNotNull(cache);
        assertTrue(cache.isPresent());
        assertEquals("a:loaded", cache.get().get("a"));
        cache.get().clearAndClose();
    }

    protected String testLoader(String key) {
        return key + ":loaded";
    }

}
