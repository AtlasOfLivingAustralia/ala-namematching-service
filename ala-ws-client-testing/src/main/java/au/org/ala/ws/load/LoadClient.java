package au.org.ala.ws.load;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * A client that makes a request of the server and measures the
 */
public class LoadClient<T> implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LoadClient.class);

    public static final int REPORT_INTERVAL = 1000;

    /** The number of requests to make */
    private int requests;
    /** The desired request rate in milliseconds per request (0 for as fast as possible) */
    private int requestRate;
    /** The source object to generate requests */
    private LoadSource<T> source;
    /** The run statistics */
    @Getter
    private Statistics<Long> statistics;

    public LoadClient(LoadSource<T> source, int requests, int requestRate) {
        this.source = source;
        this.requests = requests;
        this.requestRate = requestRate;
        this.statistics = Statistics.longStatistics();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try {
            this.statistics.start();
            for (int i = 0; i < this.requests; i++) {
                if (!this.source.hasNext()) {
                    logger.warn("Early termination after " + i + " requests, no more calls available");
                    return;
                }
                Supplier<T> call = this.source.next();
                long start = System.currentTimeMillis();
                call.get();
                long time = System.currentTimeMillis() - start;
                this.statistics.add(time);
                if (this.requestRate > 0 && time < this.requestRate)
                    Thread.sleep(this.requestRate - time);
                if ((i + 1) % REPORT_INTERVAL == 0)
                    logger.info("Run " + (i + 1) + " tests");
            }
            this.statistics.stop();
        } catch (InterruptedException ex) {
            logger.error("Interrupted - exiting");
        }
    }


}
