package au.org.ala.names.ws.api;

import au.org.ala.vocab.IsDefinedBy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@JsonDeserialize(builder = NameUsageMatch.NameUsageMatchBuilder.class)
@Value
@Builder
@EqualsAndHashCode
public class NameUsageMatch {

    boolean success;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/scientificName")
    String scientificName;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/scientificNameAuthorship")
    String scientificNameAuthorship;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/taxonConceptID")
    String taxonConceptID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/taxonRank")
    String rank;
    Integer rankID;
    Integer lft;
    Integer rgt;
    String matchType;
    String nameType;
    String synonymType;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/kingdom")
    String kingdom;
    String kingdomID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/phylum")
    String phylum;
    String phylumID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/class")
    String classs;
    String classID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/order")
    String order;
    String orderID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/family")
    String family;
    String familyID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/genus")
    String genus;
    String genusID;
    @IsDefinedBy("http://rs.gbif.org/terms/1.0/species")
    String species;
    String speciesID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/vernacularName")
    String vernacularName;
    List<String> speciesGroup;
    List<String> speciesSubgroup;
    List<String> issues;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final NameUsageMatch FAIL = NameUsageMatch.builder().success(false).issues(Collections.singletonList("noMatch")).build();
}

