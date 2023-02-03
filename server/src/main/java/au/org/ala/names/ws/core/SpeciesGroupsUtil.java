package au.org.ala.names.ws.core;

import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A resource that contains mappings from taxa onto species groups.
 */
@Slf4j
public class SpeciesGroupsUtil {
    /**
     * Making resources is fairly expensive. Reuse existing instances
     */
    private static final Map<NameSearchConfiguration, SpeciesGroupsUtil> managerCache = new HashMap<>();

    /**
     * The name index used to match names to actual taxon entries
     */
    @Getter
    private final ALANameSearcher nameIndex;
    /**
     * The list of possible species groups
     */
    @Getter
    private final List<SpeciesGroup> speciesGroups;
    /**
     * The list of possible species subgroups
     */
    @Getter
    private final List<SpeciesGroup> speciesSubgroups;

    /**
     * Construct for a name index configuration
     *
     * @param configuration The name index configuration
     * @throws IllegalArgumentException if unable to open any of the resources specified in the configuration, which makes an invalid configuration
     */
    private SpeciesGroupsUtil(NameSearchConfiguration configuration) throws IllegalArgumentException {
        try {
            this.nameIndex = new ALANameSearcher(configuration.getIndex());
            this.speciesGroups = this.readSpeciesGroups(configuration.getGroups());
            this.speciesSubgroups = this.readSpeciesSubgroups(configuration.getSubgroups());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid name searcher configuration", ex);
        }
    }

    /**
     * Retrieve species groups.
     */
    private List<SpeciesGroup> readSpeciesGroups(URL source) throws Exception {

        List<SpeciesGroup> groups = new ArrayList<SpeciesGroup>();

        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> groupsConfig = om.readValue(source, List.class);

        for (Map<String, Object> config : groupsConfig) {
            String speciesGroup = config.getOrDefault("name", "").toString();
            String rank = config.getOrDefault("rank", "").toString();
            List<String> values = (List<String>) config.getOrDefault("included", new ArrayList<String>());
            List<String> excludedValues = (List<String>) config.getOrDefault("excluded", new ArrayList<String>());
            String parent = (String) config.getOrDefault("parent", "");
            groups.add(createSpeciesGroup(speciesGroup, rank, values, excludedValues, parent));
            log.info("Species group: {} _ {} _ {} _ {} _ {}", speciesGroup, rank, values, excludedValues, parent);
        }
        return groups;
    }

    /**
     * Retrieve subgroups to use when indexing records.
     */
    private List<SpeciesGroup> readSpeciesSubgroups(URL source) throws Exception {
        List<SpeciesGroup> subgroups = new ArrayList<SpeciesGroup>();
        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> list = om.readValue(source, List.class);

        for (Map<String, Object> map : list) {
            String parent = (String) map.getOrDefault("parent", "");
            String defaultRank = (String) map.getOrDefault("rank", "order");
            List<Map<String, Object>> taxaList = (List<Map<String, Object>>) map.getOrDefault("taxa", new ArrayList<Map<String, String>>());
            for (Map<String, Object> taxaMap : taxaList) {
                String name = ((String) taxaMap.getOrDefault("name", "")).trim();
                String rank = ((String) taxaMap.getOrDefault("rank", defaultRank)).trim();
                List<String> included = (List<String>) taxaMap.getOrDefault("included", new ArrayList<String>());
                List<String> excluded = (List<String>) taxaMap.getOrDefault("excluded", new ArrayList<String>());
                subgroups.add(createSpeciesGroup(name, rank, included, excluded, parent));
            }
        }
        return subgroups;
    }


    private SpeciesGroup createSpeciesGroup(String title, String rank, List<String> values, List<String> excludedValues, String parent) throws Exception {

        List<LftRgtValues> lftRgts = new ArrayList<LftRgtValues>();

        if (excludedValues != null && !excludedValues.isEmpty()) {
            for (String excludedValue : excludedValues) {

                NameSearchResult snr = nameIndex.searchForRecord(excludedValue);

                if (snr != null) {
                    if (snr.isSynonym())
                        snr = nameIndex.searchForRecordByLsid(snr.getAcceptedLsid());
                    if (snr != null && snr.getLeft() != null && snr.getRight() != null) {
                        lftRgts.add(
                                LftRgtValues.builder()
                                        .lft(Integer.parseInt(snr.getLeft()))
                                        .rgt(Integer.parseInt(snr.getRight()))
                                        .tobeIncluded(false)
                                        .build()
                        );
                    }
                }
            }
        }

        for (String v : values) {

            NameSearchResult snr = this.nameIndex.searchForRecord(v, au.org.ala.names.model.RankType.getForName(rank));

            if (snr != null) {
                if (snr.isSynonym())
                    snr = this.nameIndex.searchForRecordByLsid(snr.getAcceptedLsid());
                if (snr != null && snr.getLeft() != null && snr.getRight() != null) {
                    lftRgts.add(
                            LftRgtValues.builder()
                                    .lft(Integer.parseInt(snr.getLeft()))
                                    .rgt(Integer.parseInt(snr.getRight()))
                                    .tobeIncluded(true).build()
                    );
                }
            }
        }

        return SpeciesGroup.builder()
                .name(title)
                .rank(rank)
                .values(values)
                .lftRgtValues(lftRgts)
                .parent(parent)
                .build();
    }

    /**
     * Returns all the species groups to which the supplied left right values belong
     */
    public List<String> getSpeciesGroups(Integer lft) throws Exception {
        return getGenericGroups(lft, getSpeciesGroups());
    }

    public List<String> getSpeciesSubGroups(Integer lft) throws Exception {
        return getGenericGroups(lft, getSpeciesSubgroups());
    }

    private List<String> getGenericGroups(Integer lft, List<SpeciesGroup> groupingList) {
        List<String> matchedGroups = new ArrayList<String>();
        if (lft != null) {
            for (SpeciesGroup sg : groupingList) {
                if (sg.isPartOfGroup(lft)) {
                    matchedGroups.add(sg.name);
                }
            }
        }
        return matchedGroups;
    }

    /**
     * Get an instance of the species group resource, based on configuration.
     * <p>
     * It's a bit of a pain to import the groups.
     * So keep a copy available.
     * </p>
     *
     * @param configuration The configuration
     * @return An species group resource
     * @throws Exception if unable to load the resource
     */
    synchronized public static SpeciesGroupsUtil getInstance(NameSearchConfiguration configuration) throws Exception {
        return managerCache.computeIfAbsent(configuration, c -> new SpeciesGroupsUtil(c));
    }
}
