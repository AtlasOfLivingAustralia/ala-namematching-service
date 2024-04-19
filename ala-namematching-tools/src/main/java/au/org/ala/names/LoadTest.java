package au.org.ala.names;


import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.client.v1.ALANameUsageMatchServiceClient;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.load.ListSource;
import au.org.ala.ws.load.LoadSource;
import au.org.ala.ws.load.LoadTester;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Load test for a running instance of the application.
 */
public class LoadTest {
    private static final Logger logger = LoggerFactory.getLogger(au.org.ala.names.LoadTest.class);

    @Parameter(names = { "--suite", "-s"}, description="URL of a CSV suite of tests to run")
    private URL suite = null;
    @Parameter(names = { "--standard" }, description="Run a set of standard tests")
    private boolean standard = false;
    @Parameter(names = {"--service", "-u"}, description="URL of namematching service (default if a suite is defined)")
    private URL service;
    @Parameter(names = {"--requests", "-n"}, description="Number of reuests to make (default if a suite is defined)")
    private int requests = 10000;
    @Parameter(names = {"--clients", "-c"}, description="Number of parallel clients")
    private int clients = 1;
    @Parameter(names = {"--rps", "-r"}, description="Number of requests per second, 0 for as fast as possible")
    private int rps = 0;
    @Parameter(names = {"--args", "-a"}, description="URL of Csv file of taxonomic names to test")
    private URL args = LoadTest.class.getResource("/names-args.csv");

    @SneakyThrows
    public LoadTest() {
        this.service = new URL("https://namematching-ws-test.ala.org.au");
    }

    public void run() {
        try {
            if (this.standard) {
                this.suite = this.getClass().getResource("/standard-suite.csv");
            }
            ClientConfiguration configuration = ClientConfiguration.builder().baseUrl(this.service).cache(false).dataCache(null).build();
            ALANameUsageMatchServiceClient client = new ALANameUsageMatchServiceClient(configuration);
            LoadSource<NameUsageMatch> source = this.buildSource(client);
            List<LoadTester<NameUsageMatch>> testers = this.buildSuite(source);
            try (Writer results = new OutputStreamWriter(System.out)) {
                LoadTester.reportHeader(results);
                for (LoadTester<NameUsageMatch> tester : testers) {
                    tester.run();
                    tester.report(results);
                }
            }
            client.close();
        } catch (Exception ex) {
            logger.error("Unable to run tests", ex);
        }
    }

    protected LoadSource<NameUsageMatch> buildSource(ALANameUsageMatchServiceClient client) throws IOException, NoSuchMethodException, CsvValidationException {
        try (Reader reader = new InputStreamReader(this.args.openStream(), Charset.forName("UTF-8"))) {
            Method method1 = client.getClass().getMethod("match", String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class, String.class);
            return ListSource.fromCsv(reader, client, NameUsageMatch.class, method1);
        }
    }

    protected List<LoadTester<NameUsageMatch>> buildSuite(LoadSource source) throws IOException, CsvValidationException {
        if (this.suite == null) {
            return Collections.singletonList(new LoadTester<>(source, this.requests, this.rps == 0 ? 0 : 1000 / rps, this.clients));
        }
        List<LoadTester<NameUsageMatch>> tests = new ArrayList<>();
        try (Reader reader = new InputStreamReader(this.suite.openStream(), Charset.forName("UTF-8"))) {
            CSVReaderBuilder builder = new CSVReaderBuilder(reader);
            CSVReader csv = builder.build();
            final String[] header = csv.readNext();
            String[] row;
            while ((row = csv.readNext()) != null) {
                int requests = this.requests;
                int rps = this.rps;
                int clients = this.clients;
                for (int i = 0; i < header.length; i++) {
                    if (StringUtils.isBlank(row[i]))
                        continue;
                    if (header[i].equalsIgnoreCase("requests")) {
                        requests = Integer.parseInt(row[i]);
                    } else if (header[i].equalsIgnoreCase("rps")) {
                        rps = Integer.parseInt(row[i]);
                    } else if (header[i].equalsIgnoreCase("clients")) {
                        clients = Integer.parseInt(row[i]);
                    }
                }
                tests.add(new LoadTester<>(source, requests, rps == 0 ? 0 : 1000 / rps, clients));
            }
        }
        return tests;
    }

    public static void main(String... args) {
        LoadTest tests = new LoadTest();
        JCommander jc = new JCommander();
        jc.addObject(tests);
        jc.parse(args);
        tests.run();
    }
}
