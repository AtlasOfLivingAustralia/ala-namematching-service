package au.org.ala.names;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Provide summary statistics from data produced by the {@link NameListComparer}.
 * <p>
 * Generates multiple mappings for multiple files to allow comparison.
 * </p>
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 */
@Slf4j
public class NameListStats {


    @Parameter(names = {"--output", "-o"}, description="Output file to write to, otherwise stdout")
    private String outputFile;
    @Parameter(names = {"--tabs" }, description="Use tab-delimited input")
    private boolean tabs = false;
    @Parameter
    private List<String> inputFiles;

    private List<Accumulator> accumulators;

    public NameListStats() {
        this.accumulators = new ArrayList<>();
        this.accumulators.add(new Accumulator("Match Type", "matchType", null));
        this.accumulators.add(new Accumulator("Rank", "rank", null));
        this.accumulators.add(new Accumulator("Errors", "errors", Pattern.compile("\\s+")));
    }


    protected void readHeader(String dataset, CSVReader reader) throws IOException, CsvValidationException {
        String[] header = reader.readNext();

        for (Accumulator accumulator: this.accumulators)
            accumulator.locateHeader(dataset, header);
    }

    public void collect() throws IOException, CsvValidationException {
        for (String in: this.inputFiles) {
            File inputFile = new File(in);
            if (!inputFile.exists()) {
                log.error("File " + in + " does not exist");
            }
            String dataset = inputFile.getName();
            this.collect(dataset, inputFile);
        }
    }

    public void collect(String dataset, File inputFile) throws IOException, CsvValidationException {
        String[] row;
        int count = 0;

        Reader ir = new FileReader(inputFile);
        RFC4180Parser parser = new RFC4180ParserBuilder().withSeparator(tabs ? '\t' : ',').build();
        CSVReader reader = new CSVReaderBuilder(ir).withCSVParser(parser).build();

        log.info("Processing " + inputFile + " as " + dataset);
        this.readHeader(dataset, reader);
        try {
            while ((row = reader.readNext()) != null) {
                for (Accumulator accumulator: this.accumulators)
                    accumulator.process(dataset, row);
                count++;
                if (count % 10000 == 0)
                    log.info("Processed " + count + " names");
            }
        } catch (Exception ex) {
            log.error("Error at line " + count, ex);
        }
        reader.close();
    }

    public void writeStats() throws IOException {
        Writer ow = this.outputFile != null ? new FileWriter(this.outputFile) : new OutputStreamWriter(System.out);
        CSVWriter output = new CSVWriter(ow);

        for (Accumulator accumulator: this.accumulators) {
            accumulator.writeStats(output);
        }
        output.close();
    }

    public static void main(String... args) throws Exception {
        NameListStats stats = new NameListStats();
        JCommander jc = new JCommander();
        jc.addObject(stats);
        jc.parse(args);
        stats.collect();
        stats.writeStats();
    }

    private static class Accumulator {
        private String title;
        private String header;
        private Pattern split;
        private Map<String, Integer> columns;
        private Map<String, Map<String, Integer>> counts;

        public Accumulator(String title, String header, Pattern split) {
            this.title = title;
            this.header = header;
            this.split = split;
            this.counts = new HashMap<>();
            this.columns = new HashMap<>();
        }

        public void locateHeader(String dataset, String[] headers) {
            for (int i = 0; i < headers.length; i++) {
                if (this.header.equals(headers[i])) {
                    this.columns.put(dataset, i);
                    return;
                }
            }
            log.warn("Column " + this.header + " not found in " + dataset);
            this.columns.put(dataset, -1);
        }

        synchronized public void process(String dataset, String[] row) {
            int column = this.columns.getOrDefault(dataset, -1);
            String[] values;
            if (column < 0)
                return;
            String value = row.length > column ? row[column] : "";
            if (value == null)
                value = "";
            if (this.split != null) {
                values = this.split.split(value);
            } else {
                values = new String[] { value };
            }
            for (String val: values) {
                val = val.trim();
                Map<String, Integer> datasetCounts = this.counts.computeIfAbsent(val, k -> new HashMap<>());
                datasetCounts.put(dataset, datasetCounts.getOrDefault(dataset, 0) + 1);
            }
        }

        public void writeStats(CSVWriter writer) {
            String[] values = new String[this.columns.size() + 1];
            List<String> datasets = this.columns.keySet().stream().sorted().collect(Collectors.toList());
            List<String> keys = this.counts.keySet().stream().sorted().collect(Collectors.toList());
            Arrays.fill(values, "");
            writer.writeNext(values);
            values[0] = title;
            for (int i = 0; i < datasets.size(); i++)
                values[i + 1] = datasets.get(i);
            writer.writeNext(values);
            for (String key: keys) {
                values[0] = key;
                Map<String, Integer> datasetCounts = this.counts.get(key);
                for (int i = 0; i < datasets.size(); i++) {
                    values[i + 1] = datasetCounts.getOrDefault(datasets.get(i), 0).toString();
                }
                writer.writeNext(values);
            }
         }
    }

}
