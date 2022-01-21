package au.org.ala.ws;

import au.org.ala.util.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.junit.Test;

import java.io.File;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;

public class ClientConfigurationTest extends TestUtils {
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

    @Test
    public void testWriteJson1() throws Exception {
        ClientConfiguration configuration = ClientConfiguration.builder()
                .baseUrl(new URL("http://localhost:8080"))
                .cacheDir(new File("/data/tmp/cache").toPath())
                .build();
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(this.getResource("client-config-1.json"), writer.toString());
    }

    @Test
    public void testWriteJson2() throws Exception {
        DataCacheConfiguration dataCache = DataCacheConfiguration.builder()
                .entryCapacity(20)
                .build();
        ClientConfiguration configuration = ClientConfiguration.builder()
                .baseUrl(new URL("http://localhost:8080"))
                .dataCache(dataCache)
                .build();
        ObjectMapper om = new ObjectMapper();
        om.enable(SerializationFeature.INDENT_OUTPUT);
        StringWriter writer = new StringWriter();
        om.writeValue(writer, configuration);
        // System.out.println(writer.toString());
        assertEquals(this.getResource("client-config-2.json"), writer.toString());
    }

    @Test
    public void testReadJson1() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ClientConfiguration configuration = om.readValue(this.getClass().getResource("client-config-1.json"), ClientConfiguration.class);
        assertEquals(new URL("http://localhost:8080"), configuration.getBaseUrl());
        assertEquals(30000, configuration.getTimeOut());
        assertTrue(configuration.isCache());
        assertEquals(new File("/data/tmp/cache").toPath(), configuration.getCacheDir());
        assertEquals(52428800, configuration.getCacheSize());
        assertNull(configuration.getDataCache());
    }

    @Test
    public void testReadJson2() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ClientConfiguration configuration = om.readValue(this.getClass().getResource("client-config-2.json"), ClientConfiguration.class);
        assertEquals(new URL("http://localhost:8080"), configuration.getBaseUrl());
        assertEquals(30000, configuration.getTimeOut());
        assertTrue(configuration.isCache());
        assertNull(configuration.getCacheDir());
        assertEquals(52428800, configuration.getCacheSize());
        assertNotNull(configuration.getDataCache());
        DataCacheConfiguration dataCache = configuration.getDataCache();
        assertFalse(dataCache.isEnableJmx());
        assertEquals(20, dataCache.getEntryCapacity());
        assertTrue(dataCache.isEternal());
        assertTrue(dataCache.isPermitNullValues());
        assertFalse(dataCache.isKeepDataAfterExpired());
        assertFalse(dataCache.isSuppressExceptions());
    }

    @Test
    public void testReadJson3() throws Exception {
        ObjectMapper om = new ObjectMapper();
        ClientConfiguration configuration = om.readValue(this.getClass().getResource("client-config-3.json"), ClientConfiguration.class);
        assertEquals(new URL("http://localhost:8080"), configuration.getBaseUrl());
        assertEquals(30000, configuration.getTimeOut());
        assertTrue(configuration.isCache());
        assertNull(configuration.getCacheDir());
        assertEquals(8800, configuration.getCacheSize());
        assertNotNull(configuration.getDataCache());
        DataCacheConfiguration dataCache = configuration.getDataCache();
        assertFalse(dataCache.isEnableJmx());
        assertEquals(100000, dataCache.getEntryCapacity());
        assertTrue(dataCache.isEternal());
        assertTrue(dataCache.isPermitNullValues());
        assertFalse(dataCache.isKeepDataAfterExpired());
        assertFalse(dataCache.isSuppressExceptions());
    }

    protected String testLoader(String key) {
        return key + ":loaded";
    }

}
