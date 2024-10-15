package au.org.ala.ws.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Abstract load tester.
 * <p>
 * This generates a number of workers that generates requests to a server for load management.
 * </p>
 */
public class LoadTester<T> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LoadTester.class);

    /** The total number of requestss */
    private int requests;
    /** The total request rate in milliseconds per request (0 for as fast as possible) */
    private int requestRate;
    /** The number of clients */
    private int clients;
    /** The invocation source */
    private LoadSource<T> source;
    /** The clients */
    private List<LoadClient<T>> loads;
    /** The run start time */
    private long started;
    /** The run stop time */
    private long stopped;

    public LoadTester(LoadSource<T> source, int requests, int requestRate, int clients) {
        this.source = source;
        this.requests = requests;
        this.requestRate = requestRate;
        this.clients = clients;
        final int rpc = (this.requests + this.clients - 1) / this.clients;
        final int fpc = this.requestRate * this.clients;
        this.loads = IntStream.range(0, this.clients).mapToObj(i -> new LoadClient<T>(this.source, i == 0 ? this.requests - rpc * (this.clients - 1) : rpc, fpc)).collect(Collectors.toList());
    }

    /**
     * Run the tests
     */
    @Override
    public void run() {
       List<Thread> runners = this.loads.stream().map(l -> new Thread(l)).collect(Collectors.toList());
       logger.info("Starting run");
       this.started = System.currentTimeMillis();
       runners.forEach(Thread::start);
       for (Thread runner: runners) {
           try {
               runner.join();
           } catch (InterruptedException ex) {
               logger.error("Interrupted!");
               return;
           }
       }
       this.stopped = System.currentTimeMillis();
       logger.info("Completed run");
    }

    /**
     * Create a header for the output
     *
     * @param writer The writer to write to
     *
     * @throws IOException if unable to writer
     */
    public static void reportHeader(Writer writer) throws IOException {
        writer.write("requests");
        writer.write(",");
        writer.write("requestRate");
        writer.write(",");
        writer.write("clients");
        writer.write(",");
        Statistics.reportHeader(writer);
        writer.write(",");
        writer.write("rate");
        writer.write("\n");
        writer.flush();
    }

    public void report(Writer writer) throws IOException {
        DecimalFormat tFormat = new DecimalFormat("0.00");
        writer.write(this.requests == 0 ? "" : Integer.toString(this.requests));
        writer.write(",");
        writer.write(this.requestRate == 0 ? "" : Integer.toString(this.requestRate));
        writer.write(",");
        writer.write(this.clients == 0 ? "" : Integer.toString(this.clients));
        writer.write(",");
        Optional<Statistics<Long>> stats = this.loads.stream().map(LoadClient::getStatistics).reduce((s1, s2) -> Statistics.merge(s1, s2));
        if (stats.isPresent()) {
            stats.get().report(writer);
        } else {
            logger.warn("No statistics collected");
        }
        writer.write(",");
        double rate = this.requests * 1000.0 / Math.max(this.stopped - this.started, 1.0);
        writer.write(tFormat.format(rate));
        writer.write("\n");
        writer.flush();
    }
}
