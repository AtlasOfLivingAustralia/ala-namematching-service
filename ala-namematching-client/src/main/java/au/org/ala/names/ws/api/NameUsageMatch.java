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
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/rankID")
    Integer rankID;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/left")
    Integer lft;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/right")
    Integer rgt;
    @IsDefinedBy("au.org.ala.names.model.MatchType")
    String matchType;
    @IsDefinedBy("org.gbif.api.vocabulary.NameType")
    String nameType;
    @IsDefinedBy("au.org.ala.names.model.SynonymType")
    String synonymType;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/kingdom")
    String kingdom;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/kingdomID")
    String kingdomID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/phylum")
    String phylum;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/phylumID")
    String phylumID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/class")
    String classs;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/classID")
    String classID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/order")
    String order;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/orderID")
    String orderID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/family")
    String family;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/familyID")
    String familyID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/genus")
    String genus;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/genusID")
    String genusID;
    @IsDefinedBy("http://rs.gbif.org/terms/1.0/species")
    String species;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/speciesID")
    String speciesID;
    @IsDefinedBy("http://rs.tdwg.org/dwc/terms/vernacularName")
    String vernacularName;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/speciesGroup")
    List<String> speciesGroup;
    @IsDefinedBy("http://id.ala.org.au/terms/1.0/speciesSubgroup")
    List<String> speciesSubgroup;
    @IsDefinedBy("au.org.ala.names.model.ErrorType")
    List<String> issues;

    @JsonPOJOBuilder(withPrefix = "")
    public static class NameUsageMatchBuilder {}

    public static final NameUsageMatch FAIL = NameUsageMatch.builder().success(false).issues(Collections.singletonList("noMatch")).build();
}

