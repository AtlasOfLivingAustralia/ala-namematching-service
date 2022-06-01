package au.org.ala.ws.load;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * A source that reads in values from a CSV file and then provides random elements from the file
 */
public class ListSource<T> extends LoadSource<T> {
    private Map<String, Integer> argMap;
    private List<String[]> values;

    public ListSource(Random random, Object client, Method[] methods, Map<String, Integer> argMap, List<String[]> values) {
        super(random, client, methods);
        this.argMap = argMap;
        this.values = values;
    }

    /**
     * Get the next set of arguments.
     * <p>
     * A random entry from the array is chosen and converted into a set of arguments that match
     * the method being used.
     * </p>
     *
     * @param method The method
     *
     * @return A selected set of arguments.
     */
    @Override
    public Object[] nextArgs(Method method) {
        String[] row = this.values.get(this.random.nextInt(this.values.size()));
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < args.length; i++) {
            Parameter parameter = method.getParameters()[i];
            Integer p = this.argMap.get(parameter.getName());
            Object val = p == null ? null : row[p];
            if (val != null && !parameter.getType().isAssignableFrom(val.getClass())) {
                if (parameter.getType() == Integer.class || parameter.getType() == int.class)
                    val = Integer.parseInt((String) val);
                else if (parameter.getType() == Boolean.class || parameter.getType() == boolean.class)
                    val = Boolean.parseBoolean((String) val);
                else
                    throw new IllegalStateException("Unable to convert " + val + " to " + parameter.getType());
            }
            args[i] = val;
        }
        return args;
    }

    /**
     * Create a set of valuies from reading a CSV file.
     * <p>
     * The CSV file is expected to contain a header with parameter names.
     * </p>
     *
     * @param reader The CSV file reader
     * @param client The client object
     * @param clazz The class of object returned by the methods
     * @param methods The methods to type
     * @param <T> The type of object returned by the methods
     *
     * @return A list source matching the CSV file
     *
     * @throws IOException id unable to read the file
     * @throws CsvValidationException If unabnle to parse the file
     */
    public static <T> ListSource<T> fromCsv(Reader reader, Object client, Class<T> clazz, Method... methods) throws IOException, CsvValidationException {
        CSVReaderBuilder builder = new CSVReaderBuilder(reader);
        CSVReader csv = builder.build();
        final String[] header = csv.readNext();
        Map<String, Integer> argMap = new HashMap<>();
        for(int i = 0; i < header.length; i++)
            argMap.put(header[i], i);
        List<String[]> values = new ArrayList<>();
        String[] row;
        while ((row = csv.readNext()) != null) {
            values.add(row);
        }
        return new ListSource<>(new Random(), client, methods, argMap, values);



    }
}
