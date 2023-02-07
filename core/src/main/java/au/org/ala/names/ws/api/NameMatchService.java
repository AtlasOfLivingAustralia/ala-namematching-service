package au.org.ala.names.ws.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Name matching interface.
 *
 * Clients implement this interface to provide a lookup for taxonomic information.
 */
public interface NameMatchService extends Closeable {
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
     * Bulk name search
     * <p>
     * Nulls are allowed in the searches.
     * If a null is present, then no search is conducted.
     * This allows a client to send a partially cached list of
     * requests to the server and just get lookups on the specific
     * elements needed.
     * </p>
     *
     * @param searches The search specifications. possibly containing nulls
     *
     * @return A list of matches, with success=false if not found and nulls for null requests
     *
     * @see #match(NameSearch)
     */
    List<NameUsageMatch> matchAll(List<NameSearch> searches);

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
     * @param style The search style to use
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
            String rank,
            SearchStyle style
    );

    /**
     * Search for a taxon with a given scientific name.
     * <p>
     * This method is vulnerable to ambiguity issues, such as homonyms
     * or pro-parte synonyms.
     * </p>
     *
     * @param scientificName The scientific name of the taxon
     * @param style The search style to use
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch match(String scientificName, SearchStyle style);

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
     * <p>
     * See {@link #get(String, Boolean)}. By default, synonyms are not followed.
     * </p>
     *
     * @param taxonID The taxon identifier
      *
     * @return A matching taxon, with success=false if not found
     */
    default NameUsageMatch get(String taxonID) {
        return this.get(taxonID, false);
    }

    /**
     * Get taxon information via a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @param follow Follow syonynms to return the accepted taxon
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch get(String taxonID, Boolean follow);

    /**
     * Get the scientific name for a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @param follow Follow syonynms to return the accepted taxon
     *
     * @return The matching name, or null for not found
     */
    String getName(String taxonID, Boolean follow);

    /**
     * Bulk lookup for taxon identifiers
     *
     * @param taxonIDs The list of taxon identifiers
     *
     * @return A corresponding list of name match results
     * @param follow Follow synonyms to the accepted taxon
     *
     * @see #get(String, Boolean)
     */
    List<NameUsageMatch> getAll(List<String> taxonIDs, Boolean follow);

    /**
     * Bulk lookup for scientific names for taxon identifiers.
     *
     * @param taxonIDs The taxon identifiers
     * @param follow Follow syonynms to return the accepted taxon
     *
     * @return A list containing the matching name The matching taxon, or null for not found
     */
    List<String> getAllNames(List<String> taxonIDs, Boolean follow);

    /**
     * Check to see if a given name is in the index for this rank.
     *
     * @param name The name The name
     * @param rank The rank The expected rank
     *
     * @return True if this name appears to be in the index, false otherwise and null if not checked
     */
    Boolean check(String name, String rank);

    /**
     * Lookup a partial name for autocomplete
     *
     * @param query The partial name
     * @param max The maximum number of results
     * @param includeSynonyms Include synonyms, as well as accepted names, in the results
     *
     * @return A map of partial matches
     */
    List<Map> autocomplete(String query, Integer max, Boolean includeSynonyms);


    /**
     * Search for the correct taxon identifier for a supplied identifier.
     * <p>
     * If a taxon has an 'alias' identifer, then this method can be used to find the used identifier
     * </p>
     *
     * @param id The identifier
     *
     * @return The correct LSID, null or empty for a missing id
     */
    String searchForLsidById(String id);

    /**
     * Get the taxon identifier corresponding to a sciencitfic name
     *
     * @param name The scientific name
     *
     * @return The corresponding taxon identifier
     */
    String searchForLSID(String name);

    /**
     * Get a list of taxon identifiers corresponding to a list of names
     *
     * @param taxaQueries The queries
     *
     * @return A list of matching identifiers
     */
    List<String> getGuidsForTaxa(List<String> taxaQueries);

    /**
     * Get the full list of common names associated with a taxon.
     *
     * @param lsid The taxon identifier
     * @param max The maximum number of results
     *
     * @return A set of common names
     */
    Set<String> getCommonNamesForLSID(String lsid, Integer max);
}
