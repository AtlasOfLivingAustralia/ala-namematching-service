package au.org.ala.names.ws.core;

import au.org.ala.names.model.NameSearchResult;
import au.org.ala.names.search.ALANameSearcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpeciesGroupsUtil {

    ALANameSearcher nameindex;
    List<SpeciesGroup> speciesGroups;
    List<SpeciesGroup> speciesSubgroups;

    private SpeciesGroupsUtil(){ }

    public static SpeciesGroupsUtil getInstance() throws Exception {
        return new SpeciesGroupsUtil().init();
    }

    private SpeciesGroupsUtil init() throws Exception {
        try {
            nameindex = new ALANameSearcher("/data/lucene/namematching");
            this.speciesGroups = initSpeciesGroups();
            this.speciesSubgroups = initSpeciesSubgroups();
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialise searcher: " + e.getMessage(), e);
        }
        return this;
    }

    public ALANameSearcher nameIndex() {
        return nameindex;
    }

    public List<SpeciesGroup> getSpeciesGroups() throws Exception {
        return speciesGroups;
    }

    public List<SpeciesGroup> getSpeciesSubgroups() throws Exception {
        return speciesSubgroups;
    }

    /**
     * Retrieve species groups.
     */
    private List<SpeciesGroup> initSpeciesGroups() throws Exception {

        List<SpeciesGroup> groups = new ArrayList<SpeciesGroup>();

        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> groupsConfig = om.readValue(new FileInputStream("/data/ala-namematching-service/config/groups.json"), List.class);

        for (Map<String, Object> config: groupsConfig){
            String speciesGroup = config.getOrDefault("name", "").toString();
            String rank = config.getOrDefault("rank", "").toString();
            List<String> values = (List<String>) config.getOrDefault("included", new ArrayList<String>());
            List<String> excludedValues = (List<String>) config.getOrDefault("excluded", new ArrayList<String>());
            String parent = (String) config.getOrDefault("parent", "");
            groups.add(createSpeciesGroup(speciesGroup, rank, values, excludedValues, parent));
            log.info("Species group: " + speciesGroup + " _ " + rank + " _ " +  values + " _ " +  excludedValues + " _ " +  parent);
        }


        return groups;
    }

    /**
     * Subgroups to use when indexing records.
     */
    private List<SpeciesGroup> initSpeciesSubgroups() throws Exception {

        List<SpeciesGroup> subgroups = new ArrayList<SpeciesGroup>();
        ObjectMapper om = new ObjectMapper();
        List<Map<String, Object>> list = om.readValue(new FileInputStream("/data/ala-namematching-service/config/subgroups.json"), List.class);

        for (Map<String, Object> map : list){
            String parentGroup = (String) map.getOrDefault("speciesGroup", "");

            if (map.containsKey("taxonRank")) {
                String rank = (String) map.getOrDefault("taxonRank", "class");
                List<Map<String, String>> taxaList = (List<Map<String, String>>) map.getOrDefault("taxa", new ArrayList<Map<String, String>>());
                for (Map<String,String> taxaMap : taxaList){

                    String name = taxaMap.getOrDefault("name", "").trim();
                    List<String> taxa = new ArrayList<String>();
                    taxa.add(name);

                    subgroups.add(createSpeciesGroup(
                            taxaMap.getOrDefault("common", "").trim(),
                            rank,
                            taxa,
                            new ArrayList<String>(),
                            parentGroup
                    ));
                }
            } else {
                List<Map<String, String>> taxaList = (List<Map<String, String>>) map.getOrDefault("taxa", new ArrayList<Map<String, String>>());

                for (Map<String,String> taxaMap : taxaList){

                    List<SpeciesGroup> groups = getSpeciesGroups();

                    //search for the sub group in the species group
                    SpeciesGroup selectedGroup = null;
                    for (SpeciesGroup g : groups){
                        if(g.name.equalsIgnoreCase(taxaMap.getOrDefault("name", "NONE"))){
                            selectedGroup = g;
                        }
                    }
                    if (selectedGroup != null) {
                        subgroups.add(createSpeciesGroup(
                                taxaMap.getOrDefault("common", "").trim(),
                                selectedGroup.rank,
                                selectedGroup.values,
                                selectedGroup.excludedValues,
                                parentGroup));
                    }
                }
            }
        }
        return subgroups;
    }


    private SpeciesGroup createSpeciesGroup(String title, String rank,  List<String> values,  List<String> excludedValues, String parent) throws Exception {

        List<LftRgtValues> lftRgts = new ArrayList<LftRgtValues>();
        ALANameSearcher nameIndex = nameIndex();

        List<String> namesToLookup = new ArrayList<String>();

        for (String v: values) {

            NameSearchResult snr = nameIndex.searchForRecord(v, au.org.ala.names.model.RankType.getForName(rank));

            if (snr != null) {
                if (snr.isSynonym())
                    snr = nameIndex.searchForRecordByLsid(snr.getAcceptedLsid());
                if (snr != null && snr.getLeft() != null && snr.getRight() != null) {
                    lftRgts.add(
                            LftRgtValues.builder()
                                    .lft(Integer.parseInt(snr.getLeft()))
                                    .rgt(Integer.parseInt(snr.getRight()))
                                    .tobeIncluded(true).build()
                    );
                } else {
                    lftRgts.add(
                            LftRgtValues.builder()
                                    .lft(-1)
                                    .rgt(-1)
                                    .tobeIncluded(false).build()
                    );
                }
            } else {
                lftRgts.add(
                        LftRgtValues.builder()
                                .lft(-1)
                                .rgt(-1)
                                .tobeIncluded(false).build()
                );
            }
        }

        for (String v: excludedValues) {

            NameSearchResult snr = nameIndex.searchForRecord(v, au.org.ala.names.model.RankType.getForName(rank));

            if (snr != null) {
                if (snr.isSynonym())
                    snr = nameIndex.searchForRecordByLsid(snr.getAcceptedLsid());
                if (snr != null && snr.getLeft() != null && snr.getRight() != null) {
                    lftRgts.add(
                            LftRgtValues.builder()
                                    .lft(Integer.parseInt(snr.getLeft()))
                                    .rgt(Integer.parseInt(snr.getRight()))
                                    .tobeIncluded(false).build()
                    );
                } else {
                    lftRgts.add(
                            LftRgtValues.builder()
                                    .lft(-1)
                                    .rgt(-1)
                                    .tobeIncluded(false).build()
                    );
                }
            } else {
                lftRgts.add(
                        LftRgtValues.builder()
                                .lft(-1)
                                .rgt(-1)
                                .tobeIncluded(false).build()
                );
            }
        }

        return SpeciesGroup.builder()
                .name(title)
                .rank(rank)
                .values(values)
                .excludedValues(excludedValues)
                .lftRgtValues(lftRgts)
                .parent(parent)
                .build();
    }

    /**
     * Returns all the species groups to which the supplied left right values belong
     */
    public List<String> getSpeciesGroups(Integer lft) throws Exception {
        return getGenericGroups(lft,  getSpeciesGroups());
    }

    public List<String> getSpeciesSubGroups(Integer lft) throws Exception {
        return getGenericGroups(lft, getSpeciesSubgroups());
    }

    private List<String> getGenericGroups(Integer lft, List<SpeciesGroup> groupingList) {
        List<String> matchedGroups = new ArrayList<String>();
        for(SpeciesGroup sg : groupingList) {
            if (sg.isPartOfGroup(lft)) {
                matchedGroups.add(sg.name);
            }
        }
        return matchedGroups;
    }
}
