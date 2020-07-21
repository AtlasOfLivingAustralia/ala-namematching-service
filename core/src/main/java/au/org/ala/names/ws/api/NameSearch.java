package au.org.ala.names.ws.api;

import au.org.ala.util.BasicNormaliser;
import au.org.ala.util.Normaliser;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

@JsonDeserialize(builder = NameSearch.NameSearchBuilder.class)
@Value
@Builder
@EqualsAndHashCode
@ApiModel(
   value = "Search Parameters",
   description = "A set of parameters that can be used to search for taxa. " +
           "The various entries, kingdom etc., refer to names in the Linnaean hierarchy. " +
           "The only strictly required thing is some sort of scientific name. " +
           "However, the name can be deduced from other information if not given."
)
public class NameSearch {
   /** Used to normalise request strings before processing */
   private static final Normaliser NORMALISER = new BasicNormaliser(true, true, true, false, false, true);


   @ApiModelProperty(
           value = "The Linnaean kingdom",
           example = "Animalia",
           notes = "http://rs.tdwg.org/dwc/terms/kingdom"
   )
   private String kingdom;
   @ApiModelProperty(
           value = "The Linnaean phylum",
           example = "Chordata",
           notes = "http://rs.tdwg.org/dwc/terms/phylum"
   )
   private String phylum;
   @ApiModelProperty(
           value = "The Linnaean class",
           example = "Aves",
           notes = "http://rs.tdwg.org/dwc/terms/class"
   )
   private String clazz;
   @ApiModelProperty(
           value = "The Linnaean order",
           example = "Anseriformes",
           notes = "http://rs.tdwg.org/dwc/terms/order"
   )
   private String order;
   @ApiModelProperty(
           value = "The Linnaean family",
           example = "Anatidae",
           notes = "http://rs.tdwg.org/dwc/terms/family"
   )
   private String family;
   @ApiModelProperty(
           value = "The Linnaean genus",
           example = "Anas",
           notes = "http://rs.tdwg.org/dwc/terms/genus"
   )
   private String genus;
   @ApiModelProperty(
           value = "The species part of a Linnaean binomial name",
           example = "superciliosa",
           notes = "http://rs.tdwg.org/dwc/terms/specificEptithet"
   )
   private String specificEpithet;
   @ApiModelProperty(
           value = "The below-species (subspecies, variety, form etc.) part of a Linnaean binomial name",
           example = "superciliosa",
           notes = "http://rs.tdwg.org/dwc/terms/infraspecificEptithet"
   )
   private String infraspecificEpithet;
   @ApiModelProperty(
           value = "The Linnaean rank of the expected result",
           example = "subspecies",
           notes = "http://rs.tdwg.org/dwc/terms/taxonRank"
   )
   private String rank;
   @ApiModelProperty(
           value = "The Linnaean rank of the expected result, as supplied",
           example = "SubSpecies",
           notes = "http://rs.tdwg.org/dwc/terms/verbatimTaxonRank"
   )
   private String verbatimTaxonRank;
   @ApiModelProperty(
           value = "The expected taxon concept (placement in a taxonomy)",
           example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
           notes = "http://rs.tdwg.org/dwc/terms/taxonConceptID"
   )
   private String taxonConceptID;
   @ApiModelProperty(
           value = "The expected taxon identifier",
           example = "urn:lsid:biodiversity.org.au:afd.taxon:7d8e4927-90d6-40ba-a1e9-d6e917d2270b",
           notes = "http://rs.tdwg.org/dwc/terms/taxonID"
   )
   private String taxonID;
   @ApiModelProperty(
           value = "The scientific name authorship (with the scientific name, corresponds to the taxon concept)",
           example = "Gmelin, 1789",
           notes = "http://rs.tdwg.org/dwc/terms/scientificNameAuthorship"
   )
   private String scientificNameAuthorship;
   @ApiModelProperty(
           value = "The scientific name",
           example = "Anas superciliosa superciliosa",
           notes = "http://rs.tdwg.org/dwc/terms/scientificName"
   )
   private String scientificName;
   @ApiModelProperty(
           value = "The vernacular name",
           example = "Grey Duck",
           notes = "http://rs.tdwg.org/dwc/terms/vernacularName"
   )
   private String vernacularName;

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
              .vernacularName(NORMALISER.normalise(this.vernacularName))
              .build();
    }

   @JsonPOJOBuilder(withPrefix = "")
   public static class NameSearchBuilder {}
}