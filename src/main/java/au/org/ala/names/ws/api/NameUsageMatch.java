package au.org.ala.names.ws.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@JsonDeserialize(builder = NameUsageMatch.NameUsageMatchBuilder.class)
@Value
@Builder
public class NameUsageMatch {

    boolean success;
    String scientificName;
    String scientificNameAuthorship;
    String taxonConceptID;
    String rank;
    Integer rankID;
    Integer lft;
    Integer rgt;
    String matchType;
    String kingdom;
    String kingdomID;
    String phylum;
    String phylumID;
    String classs;
    String classID;
    String order;
    String orderID;
    String family;
    String familyID;
    String genus;
    String genusID;
    String species;
    String speciesID;
    String vernacularName;
    List<String> speciesGroup;
    List<String> speciesSubgroup;


    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final NameUsageMatch FAIL = NameUsageMatch.builder().success(false).build();
}

