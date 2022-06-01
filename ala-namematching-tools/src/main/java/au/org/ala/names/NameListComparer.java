package au.org.ala.names;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.client.ALANameUsageMatchServiceClient;
import au.org.ala.ws.ClientConfiguration;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compare a list of existing names with what the index comes up with an produce a report.
 *
 * @author Doug Palmer &lt;Doug.Palmer@csiro.au&gt;
 */
@Slf4j
public class NameListComparer {
    private static String[][] TERMS = {
            { "originalId", "Species", "taxonConceptID", "taxon_concept_lsid", "taxonID" },
            { "originalScientificName", "Species Name", "scientificName", "taxon_name", "raw_taxon_name" },
            { "originalScientificNameAuthorship", "Scientific Name Authorship", "scientificNameAuthorship" },
            { "originalRank", "Taxon Rank", "taxonRank", "rank", "taxonomicRank" },
            { "originalKingdom", "Kingdom", "kingdom" },
            { "originalPhylum",  "Phylum", "phylum" },
            { "originalClass", "Class", "class", "class_", "_class" },
            { "originalOrder", "Order", "order" },
            { "originalFamily", "Family", "family" },
            { "originalGenus", "Genus", "genus" },
            { "originalVernacular", "Vernacular Name", "raw_common_name","vernacularName", "taxon_common_name" }
    };


    @Parameter(names = {"--service", "-s"}, description="URL of namematching service")
    private String service = "http://localhost:9179";
    @Parameter(names = {"--output", "-o"}, description="Output file to write to, otherwise stdout")
    private String outputFile;
    @Parameter(names = {"--tabs" }, description="Use tab-delimited input")
    private boolean tabs = false;
    @Parameter(names = {"--limit" }, description="Limit to first n rows")
    private int limit = -1;
    @Parameter
    private String inputFile;


    private CSVReader names;
    private CSVWriter output;
    private ALANameUsageMatchServiceClient client;
    private Map<String, Integer> columnMap;
    private Map<String, Integer> termMap;
    private List<String> additional;

    public NameListComparer() {
    }

    protected String getColumn(String[] row, String column) {
        Integer pos = this.termMap.get(column);

        if (pos == null)
            pos = columnMap.get(column);
        if (pos != null && pos.intValue() < row.length) {
            String value = row[pos.intValue()];

            return StringUtils.isBlank(value) ? null : value;
        }
        return null;
    }

    protected String mapTerm(String column) {
        for (String[] term: TERMS) {
            String original = term[0];
            for (int i = 1; i < term.length; i++)
                if (column.equals(term[i]))
                    return original;
        }
        return null;
    }

    protected void readHeader() throws IOException, CsvValidationException {
        String[] header = names.readNext();
        int i = 0;

        this.columnMap = new HashMap<String, Integer>();
        this.termMap = new HashMap<String, Integer>();
        this.additional = new ArrayList<>();
        for (String column: header) {
            column = column.trim();
            this.columnMap.put(column, i);
            String original = mapTerm(column);
            if (original != null)
                termMap.put(original, i);
            else
                additional.add(column);
            i++;
        }
    }

    protected void writeHeader() throws IOException {
        // Basic required columns
        List<String> columns = new ArrayList<>();
        columns.addAll(Arrays.asList(
                "originalId",
                "id",
                "originalScientificName",
                "scientificName",
                "originalScientificNameAuthorship",
                "scientificNameAuthorship",
                "matchType",
                "originalRank",
                "rank",
                "originalKingdom",
                "kingdom",
                "originalPhylum",
                "phylum",
                "originalClass",
                "class",
                "originalOrder",
                "order",
                "originalFamily",
                "family",
                "originalGenus",
                "genus",
                "species",
                "originalVernacular",
                "errors"
        ));
        columns.addAll(additional);
        this.output.writeNext(columns.toArray(new String[columns.size()]));
    }

    public String[] match(String[] row) {
        NameUsageMatch match = null;
        String originalId = this.getColumn(row, "originalId");
        String originalScientificName = this.getColumn(row, "originalScientificName");
        String usedScientificName = originalScientificName;
        String originalScientificNameAuthorship = this.getColumn(row, "originalScientificNameAuthorship");
        String originalRank = this.getColumn(row, "originalRank");
        String originalKingdom = this.getColumn(row, "originalKingdom");
        String originalPhylum = this.getColumn(row, "originalPhylum");
        String originalClass = this.getColumn(row, "originalClass");
        String originalOrder = this.getColumn(row, "originalOrder");
        String originalFamily = this.getColumn(row, "originalFamily");
        String originalGenus = this.getColumn(row, "originalGenus");
        String originalVernacular = this.getColumn(row, "originalVernacularName");
        String id = null;
        String matchType = null;
        String scientificName = null;
        String scientificNameAuthorship = null;
        String rank = null;
        String kingdom = null;
        String phylum = null;
        String klass = null;
        String order = null;
        String family = null;
        String genus = null;
        String species = null;
        String errors = "";
        if (usedScientificName == null && originalGenus != null)
            usedScientificName =originalGenus;
        if (usedScientificName == null && originalFamily != null)
            usedScientificName = originalFamily;
        if (usedScientificName == null && originalOrder != null)
            usedScientificName = originalOrder;
        if (usedScientificName == null && originalClass != null)
            usedScientificName = originalClass;
        if (usedScientificName == null && originalPhylum != null)
            usedScientificName = originalPhylum;
        if (usedScientificName == null && originalKingdom != null)
            usedScientificName = originalKingdom;
        if (usedScientificName == null && originalVernacular == null)
            return null;
        try {
            if (usedScientificName != null && !usedScientificName.isEmpty()) {
                NameSearch search = NameSearch.builder()
                        .scientificName(usedScientificName)
                        .genus(originalGenus)
                        .family(originalFamily)
                        .order(originalOrder)
                        .clazz(originalClass)
                        .phylum(originalPhylum)
                        .kingdom(originalKingdom)
                        .rank(originalRank)
                        .scientificNameAuthorship(originalScientificNameAuthorship)
                        .build();
                match = this.client.match(search);
                errors = errors + String.join(" ", match.getIssues().stream().filter(s -> !"noIssue".equals(s)).collect(Collectors.toList()));
             }
            if ((match == null || !match.isSuccess()) && originalVernacular != null) {
                match = this.client.matchVernacular(originalVernacular);
                errors = errors + " vernacular";
            }
            if (match != null && match.isSuccess()) {
                id = match.getTaxonConceptID();
                matchType = match.getMatchType();
                scientificName = match.getScientificName();
                scientificNameAuthorship = match.getScientificNameAuthorship();
                rank = match.getRank();
                kingdom = match.getKingdom();
                phylum = match.getPhylum();
                klass = match.getClasss();
                order = match.getOrder();
                family = match.getFamily();
                genus = match.getGenus();
                species = match.getSpecies();
            } else {
                matchType = "notFound";
            }
        } catch (Exception ex) {
            errors = errors + " exception:" + ex.getClass();
            log.error("Really bad exception " + ex);
        }
        List<String> values = new ArrayList<>(additional.size() + 30);
        values.addAll(Arrays.asList(
                originalId,
                id,
                originalScientificName,
                scientificName,
                originalScientificNameAuthorship,
                scientificNameAuthorship,
                matchType,
                originalRank,
                rank,
                originalKingdom,
                kingdom,
                originalPhylum,
                phylum,
                originalClass,
                klass,
                originalOrder,
                order,
                originalFamily,
                family,
                originalGenus,
                genus,
                species,
                originalVernacular,
                errors.trim()
        ));
        for (String column: additional)
            values.add(this.getColumn(row, column));
        return values.toArray(new String[values.size()]);
    }

    public void compare() throws IOException, CsvValidationException {
        String[] row, match;
        int count = 0;

        ClientConfiguration configuration = ClientConfiguration.builder().baseUrl(new URL(this.service)).build();
        this.client = new ALANameUsageMatchServiceClient(configuration);
        Reader ir = new FileReader(this.inputFile);
        RFC4180Parser parser = new RFC4180ParserBuilder().withSeparator(tabs ? '\t' : ',').build();
        this.names = new CSVReaderBuilder(ir).withCSVParser(parser).build();
        Writer ow = this.outputFile != null ? new FileWriter(this.outputFile) : new OutputStreamWriter(System.out);
        this.output = new CSVWriter(ow);

        this.readHeader();
        this.writeHeader();
        try {
            while ((row = this.names.readNext()) != null) {
                match = this.match(row);
                if (match != null)
                    this.output.writeNext(match);
                if (++count % 1000 == 0)
                    log.info("Processed " + count + " names");
                if (this.limit > 0 && count > this.limit)
                    return;
            }
        } catch (Exception ex) {
            log.error("Error at line " + count, ex);
        }
    }

    public void close() throws IOException {
        this.names.close();
        this.output.close();
    }

    public static void main(String... args) throws Exception {
        NameListComparer tests = new NameListComparer();
        JCommander jc = new JCommander();
        jc.addObject(tests);
        jc.parse(args);
        tests.compare();
        tests.close();
    }

}
