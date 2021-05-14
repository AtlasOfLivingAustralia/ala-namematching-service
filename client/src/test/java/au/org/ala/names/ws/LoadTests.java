package au.org.ala.names.ws;


import au.org.ala.names.ws.client.ALANameUsageMatchServiceClient;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.load.ListSource;
import au.org.ala.ws.load.LoadSource;
import au.org.ala.ws.load.LoadTester;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.common.base.Charsets;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * Load test for a running instance of the application.
 */
public class LoadTests {
    private static final Logger logger = LoggerFactory.getLogger(LoadTests.class);

    @Parameter(names = {"--service", "-s"}, description="URL of namematching service")
    private URL service;
    @Parameter(names = {"--requests", "-n"}, description="Number of reuests to make")
    private int requests = 10000;
    @Parameter(names = {"--clients", "-c"}, description="Number of parallel clients")
    private int clients = 1;
    @Parameter(names = {"--rps", "-r"}, description="Number of requests per second, 0 for as fast as possible")
    private int rps = 0;
    @Parameter(names = {"--args", "-a"}, description="URL of Csv file of taxonomic names to test")
    private URL args = LoadTests.class.getResource("/names-checklist.csv");

    @SneakyThrows
    public LoadTests() {
        this.service = new URL("https://namematching-ws-test.ala.org.au");
    }

    public void run() {
        try {
            ClientConfiguration configuration = ClientConfiguration.builder().baseUrl(this.service).build();
            ALANameUsageMatchServiceClient client = new ALANameUsageMatchServiceClient(configuration);
            Reader reader = new InputStreamReader(this.args.openStream(), Charsets.UTF_8);
            Method method1 = client.getClass().getMethod("match", String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
            LoadSource<Object> source = ListSource.fromCsv(reader, client, method1);
            reader.close();
            LoadTester<Object> tester = new LoadTester<>(source, this.requests, this.rps == 0 ? 0 : 1000 / rps, this.clients);
            tester.run();
            client.close();
        } catch (Exception ex) {
            logger.error("Unable to run tests", ex);
        }
    }

    public static void main(String... args) {
        LoadTests tests = new LoadTests();
        JCommander jc = new JCommander();
        jc.addObject(tests);
        jc.parse(args);
        tests.run();
    }
}
