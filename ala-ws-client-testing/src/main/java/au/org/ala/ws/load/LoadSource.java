package au.org.ala.ws.load;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Supplier;

/**
 * A source of client invocations.
 * <p>
 *     Note that a load source may be used by multiple clients
 *     and, therefore, uses synchronized access to its values.
 * </p>
 */
abstract public class LoadSource<T> implements Iterator<Supplier<T>> {
    private static final Logger logger = LoggerFactory.getLogger(LoadSource.class);

    /** A source of random numbers */
    protected Random random;
    /** The client to call */
    private Object client;
    /** The method(s) to call on the client */
    private Method[] methods;

    public LoadSource(Random random, Object client, Method[] methods) {
        this.random = random;
        this.client = client;
        this.methods = methods;
    }

    /**
     * Always retuins true.
     * <p>
     *     We can always find another winvcation, even if we
     * </p>
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override
    public boolean hasNext() {
        return true;
    }

    /**
     * Returns the next invocation on the client
     *
     * @return the next element in the iteration
     */
    @Override
    synchronized public Supplier<T> next() {
        final Method method = this.methods[this.random.nextInt(this.methods.length)];
        final Object[] args = this.nextArgs(method);
        return () -> {
            try {
                return (T) method.invoke(this.client, args);
            } catch (Exception ex) {
                this.logger.error("Uable to invoke " + method + " on " + this.client, ex);
                throw new IllegalStateException(ex);
            }
        };
    }

    abstract public Object[] nextArgs(Method method);
}
