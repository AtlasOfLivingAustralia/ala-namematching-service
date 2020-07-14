package au.org.ala.names.ws.api;

import au.org.ala.util.BasicNormaliser;
import au.org.ala.util.Normaliser;
import au.org.ala.vocab.IsDefinedBy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = NameSearch.NameSearchBuilder.class)
@Value
@Builder
@EqualsAndHashCode
@IsDefinedBy("http://rs.tdwg.org/dwc/terms/Taxon")
public class NameSearch {
   /** Used to normalise request strings before processing */
   private static final Normaliser NORMALISER = new BasicNormaliser(true, true, true, false, false, true);

   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/kingdom")
   public String kingdom;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/phylum")
   public String phylum;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/class")
   public String clazz;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/order")
   public String order;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/family")
   public String family;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/genus")
   public String genus;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/specificEpithet")
   public String specificEpithet;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/infraspecificEpithet")
   public String infraspecificEpithet;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/taxonRank")
   public String rank;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/verbatimTaxonRank")
   public String verbatimTaxonRank;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/taxonConceptID")
   public String taxonConceptID;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/taxonID")
   public String taxonID;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/scientificNameAuthorship")
   public String scientificNameAuthorship;
   @IsDefinedBy("http://rs.tdwg.org/dwc/terms/scientificName")
   public String scientificName;

   /**
    * Get a version of this that has been normalised.
    * <p>
    * Normalised means no strange characters, surrounding quotes stripped
    * and everything neatly cleaned up and nice.
    * </p>
    *
    * @return A normalised copy of the search
    */
   public NameSearch normalised() {
       return NameSearch.builder()
              .kingdom(NORMALISER.normalise(this.kingdom))
              .phylum(NORMALISER.normalise(this.phylum))
              .clazz(NORMALISER.normalise(this.clazz))
              .order(NORMALISER.normalise(this.order))
              .genus(NORMALISER.normalise(this.genus))
              .specificEpithet(NORMALISER.normalise(this.specificEpithet))
              .infraspecificEpithet(NORMALISER.normalise(this.infraspecificEpithet))
              .rank(NORMALISER.normalise(this.rank))
              .verbatimTaxonRank(NORMALISER.normalise(this.verbatimTaxonRank))
              .taxonConceptID(this.taxonConceptID) // Not text
              .taxonID(this.taxonID)
              .scientificNameAuthorship(NORMALISER.normalise(this.scientificNameAuthorship))
              .scientificName(NORMALISER.normalise(this.scientificName))
              .build();
    }

   @JsonPOJOBuilder(withPrefix = "")
   public static class NameSearchBuilder {}
}