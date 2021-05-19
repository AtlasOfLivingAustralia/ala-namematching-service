package au.org.ala.ws.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
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
       runners.forEach(Thread::start);
       for (Thread runner: runners) {
           try {
               runner.join();
           } catch (InterruptedException ex) {
               logger.error("Interrupted!");
               return;
           }
       }
       logger.info("Completed run");
       Optional<Statistics<Long>> stats = this.loads.stream().map(LoadClient::getStatistics).reduce((s1, s2) -> Statistics.merge(s1, s2));
       if (stats.isPresent()) {
           try {
               stats.get().report(new OutputStreamWriter(System.out));
           } catch (IOException ex) {
               logger.error("Unable to write statistics", ex);
           }
       } else {
           logger.warn("No statistics collected");
       }
    }
}
