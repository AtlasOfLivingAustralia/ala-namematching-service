package au.org.ala.names.ws.api.v2;

import au.org.ala.bayesian.Trace;
import au.org.ala.names.ws.api.SearchStyle;

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
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch match(NameSearch search, Trace.TraceLevel trace);

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
     * @param trace The trace level for debugging
     *
     * @return A list of matches, with success=false if not found and nulls for null requests
     *
     * @see #match(NameSearch, au.org.ala.bayesian.Trace.TraceLevel)
     */
    List<NameUsageMatch> matchAll(List<NameSearch> searches, Trace.TraceLevel trace);

    /**
     * Find a mataching taxon based on the Linnaean hierarchy.
     *
     * @see #match(NameSearch, au.org.ala.bayesian.Trace.TraceLevel)
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
     * @param continent The continent where the observation took place
     * @param country The country where the observation took place
     * @param stateProvince The state or province where the observation took place
     * @param islandGroup The island group where the observation took place
     * @param island The island where the observation took place
     * @param waterBody The water body (ocean, sea, bay etc.) where the observation took place
     * @param style The search style to use
     * @param trace The trace level for debugging
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
            String continent,
            String country,
            String stateProvince,
            String islandGroup,
            String island,
            String waterBody,
            SearchStyle style,
            Trace.TraceLevel trace
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
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch match(String scientificName, SearchStyle style, Trace.TraceLevel trace);

    /**
     * Search for a taxon with a given vernacular (common) name.
     * <p>
     * Vernacular names do not reliably map onto individual taxa.
     * A "best-effort" result is returned.
     * </p>
     *
     * @param vernacularName The vernacular name to search for
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    NameUsageMatch matchVernacular(String vernacularName, Trace.TraceLevel trace);

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
}
