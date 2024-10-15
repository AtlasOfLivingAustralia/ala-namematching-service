package au.org.ala.names.ws.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Simple lookup interface
 *
 * Clients implement this interface to provide a lookup for names and taxon identifiers
 */
public interface LookupService extends Closeable {
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
