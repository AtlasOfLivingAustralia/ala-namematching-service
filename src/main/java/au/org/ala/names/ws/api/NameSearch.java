package au.org.ala.names.ws.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@JsonDeserialize(builder = NameSearch.NameSearchBuilder.class)
@Value
@Builder
public class NameSearch {

    private boolean success;
    private String scientificName;
    private String scientificNameAuthorship;
    private String taxonConceptID;
    private String rank;
    private Integer rankID;
    private Integer left;
    private Integer right;
    private String matchType;
    private String kingdom;
    private String kingdomID;
    private String phylum;
    private String phylumID;
    private String classs;
    private String classID;
    private String order;
    private String orderID;
    private String family;
    private String familyID;
    private String genus;
    private String genusID;
    private String species;
    private String speciesID;
    private String vernacularName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NameSearchBuilder {}

    public static final NameSearch FAIL = NameSearch.builder().success(false).build();
}

