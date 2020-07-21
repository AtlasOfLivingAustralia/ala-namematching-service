package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.ws.ClientException;
import retrofit2.HttpException;

import java.io.Closeable;

public interface ALANameMatchService extends Closeable {
    /**
     * Find a matching taxon based on a search specification.
     * <p>
     * Individual parameters in the search may be null, depending on the information available.
     * What is required is the ability to infer a scientific or vernacular name from the information provided.
     * </p>
     *
     * @param search The search specification
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch match(NameSearch search);

    /**
     * Find a mataching taxon based on the Linnaean hierarchy.
     *
     * @see #match(NameSearch)
     *
     * @param scientificName The scientific name
     * @param kingdom The Linnaean kingdom name
     * @param phylum The Linnaean phylum name
     * @param clazz The Linnaean class name
     * @param order The Linnaean order name
     * @param family The Linnaean family name
     * @param genus The Linnaean genus name
     * @param specificEpithet The specific epithet (species component of a binomial name)
     * @param infraspecificEpithet The infraspecific epithet (subspecies, variety etc component of a trinomial name)
     * @param rank The Linnaean rank name
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch match(
            String scientificName,
            String kingdom,
            String phylum,
            String clazz,
            String order,
            String family,
            String genus,
            String specificEpithet,
            String infraspecificEpithet,
            String rank
    );

    /**
     * Search for a taxon with a given scientific name.
     * <p>
     * This method is vulnerable to ambiguity issues, such as homonyms
     * or pro-parte synonyms.
     * </p>
     *
     * @param scientificName The scientific name of the taxon
     *
     * @return A matching taxon, with success=false if not found
     *
     * @throws HttpException to propagate an error from the service
     * @throws ClientException if unable to contact the service
     */
    NameUsageMatch match(String scientificName);

    /**
     * Search for a taxon with a given vernacular (common) name.
     * <p>
     * Vernacular names do not reliably map onto individual taxa.
     * A "best-effort" result is returned.
     * </p>
     *
     * @param vernacularName The vernacular name to search for
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch matchVernacular(String vernacularName);

    /**
     * Get taxon information via a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch get(String taxonID);

}
