package au.org.ala.ws.load;

import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Collect time statistics.
 * <p>
 *     Statistics are kept in raw form so that various analythics can be calculated.
 * </p>
 *
 * @param <T> The type of measure being collected
 */
public class Statistics<T> {
    /** The start time */
    private long start;
    /** The stop time */
    private long stop;
    /** The collection of individual values */
    private List<T> values;
    /** The zero value */
    private T zero;
    /** The sum operation */
    private BinaryOperator<T> sum;
    /** The subtract operation */
    private BinaryOperator<T> sub;
    /** The multiply operation */
    private BinaryOperator<T> mul;
    /** The divide operation */
    private BinaryOperator<T> div;
    /** The square root operation */
    private UnaryOperator<T> sqrt;
    /** The cast operation */
    private Function<Integer, T> cast;
    /** The comparator */
    private Comparator<T> comparator;

    protected Statistics(List<T> values, T zero, BinaryOperator<T> sum, BinaryOperator<T> sub, BinaryOperator<T> mul, BinaryOperator<T> div, UnaryOperator<T> sqrt, Function<Integer, T> cast, Comparator<T> comparator) {
        this.values = values;
        this.zero = zero;
        this.sum = sum;
        this.sub = sub;
        this.mul = mul;
        this.div = div;
        this.sqrt = sqrt;
        this.cast = cast;
        this.comparator = comparator;
        this.start = -1L;
        this.stop = -1L;
    }

    /**
     * Create a statistics collector for long values.
     *
     * @return Long statistics
     */
    public static Statistics<Long> longStatistics() {
        return new Statistics<>(
            new ArrayList<>(),
            0L,
            (a, b) -> a + b,
            (a, b) -> a - b,
            (a, b) -> a * b,
            (a, b) -> a / b,
            (a -> (long) Math.sqrt(a)),
            (a -> (long) a),
            Long::compare
        );
    }

    /**
     * Timestamp the start of recordding
     */
    public void start() {
        this.start = System.currentTimeMillis();
    }

    /**
     * Timestamp the end of recording
     */
    public void stop() {
        this.stop = System.currentTimeMillis();
    }

    /**
     * Add a value to the statistics
     *
     * @param value The value
     */
    public void add(T value) {
        this.values.add(value);
    }

    /**
     * Provide a new set of statistics by merging two sets.
     *
     * @param other The other set to merge
     *
     * @return The merged set
     */
    public static <T> Statistics<T> merge(Statistics<T> one, Statistics<T> other) {
        List<T> merged = new ArrayList<>(one.values.size() + other.values.size());
        merged.addAll(one.values);
        merged.addAll(other.values);
        Statistics<T> stats = new Statistics<>(
            merged,
            one.zero,
            one.sum,
            one.sub,
            one.mul,
            one.div,
            one.sqrt,
            one.cast,
            one.comparator
        );
        stats.start = one.start <= 0 ? other.start : (other.start <= 0 ? one.start : Math.min(one.start, other.start));
        stats.stop = one.stop <= 0 ? other.stop : (other.stop <= 0 ? one.stop : Math.min(one.stop, other.stop));
        return stats;
    }

    /**
     * Get the number of entries
     *
     * @return The number of individual values
     */
    public T getN() {
        return this.cast.apply(this.values.size());
    }

    /**
     * Calculate the throughput in terms of items per second
     *
     * @return The throughput
     */
    public double getThroughput() {
        if (this.start <= 0 || this.stop <= 0)
            throw new IllegalStateException("No times recorded");
        return (this.values.size() * 1000.0) / (this.stop - this.start);
    }

    /**
     * Get the mean of the collected data
     *
     * @return The mean
     */
    public T getMean() {
        T n = this.getN();
        T sum = this.values.stream().reduce(this.zero, this.sum);
        return this.div.apply(sum, n);
    }

    /**
     * Get the standatd deviation of the collected data
     *
     * @return The standard deviation
     */
    public T getStdDev() {
        final T mean = this.getMean();
        T variance = this.values.stream()
            .map(a -> { T d = this.sub.apply(a, mean); return this.mul.apply(d, d); })
            .reduce(this.zero, this.sum);
        return this.sqrt.apply(this.div.apply(variance, this.getN()));
    }

    /**
     * Get the minimum value
     *
     * @return The minimum value
     */
    public Optional<T> getMin() {
        return this.values.stream().min(this.comparator);
    }

    /**
     * Get the maximum value
     *
     * @return The minimum value
     */
    public Optional<T> getMax() {
        return this.values.stream().max(this.comparator);
    }

    /**
     * Print out the statistics
     *
     * @param writer What to erite to
     *
     * @throws IOException if unable to write
     */
    public void report(Writer writer) throws IOException {
        DecimalFormat cFormat = new DecimalFormat("0");
        DecimalFormat tFormat = new DecimalFormat("0.00");
        writer.append("Samples,Mean,StdDev,Min,Max,Rate");
        writer.append("\n");
        writer.append(cFormat.format(this.getN()));
        writer.append(",");
        writer.append(cFormat.format(this.getMean()));
        writer.append(",");
        writer.append(cFormat.format(this.getStdDev()));
        writer.append(",");
        writer.append(cFormat.format(this.getMin().orElse(this.zero)));
        writer.append(",");
        writer.append(cFormat.format(this.getMax().orElse(this.zero)));
        writer.append(",");
        writer.append(tFormat.format(this.getThroughput()));
        writer.append("\n");
        writer.flush();
    }
}
