package au.org.ala.ws;

import org.junit.Test;

import java.net.URL;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

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
}
