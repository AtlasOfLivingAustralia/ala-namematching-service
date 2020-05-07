package au.org.ala.names.ws.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = NameSearch.NameSearchBuilder.class)
@Value
@Builder
@EqualsAndHashCode
public class NameSearch {
   public String kingdom;
   public String phylum;
   public String clazz;
   public String order;
   public String family;
   public String genus;
   public String species;
   public String subspecies;
   public String specificEpithet;
   public String infraspecificEpithet;
   public String authorship;
   public String rank;
   public String verbatimTaxonRank;
   public String taxonConceptID;
   public String taxonID;
   public String genericName;
   public String scientificNameAuthorship;
   public String scientificName;

   @JsonPOJOBuilder(withPrefix = "")
   public static class NameSearchBuilder {}
}