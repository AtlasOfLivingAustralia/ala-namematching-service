package au.org.ala.names.ws.api.v2;

import au.org.ala.bayesian.Trace;
import au.org.ala.names.ws.api.SearchStyle;

import java.io.Closeable;
import java.util.List;

/**
 * Location matching interface.
 *
 * Clients implement this interface to provide a lookup for locality information.
 */
public interface LocationMatchService extends Closeable {
    /**
     * Find a matching location based on a search specification.
     * <p>
     * Individual parameters in the search may be null, depending on the information available.
     * </p>
     *
     * @param search The search specification
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    LocationMatch match(LocationSearch search, Trace.TraceLevel trace);

    /**
     * Bulk location search
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
     * @see #match(LocationSearch, Trace.TraceLevel)
     */
    List<LocationMatch> matchAll(List<LocationSearch> searches, Trace.TraceLevel trace);

    /**
     * Find a matching location based on location names
     *
     * @see #match(LocationSearch, Trace.TraceLevel)
     *
     * @param locationID A known location identifier
     * @param locality The location name
     * @param continent The continent name
     * @param country The country name
     * @param stateProvince The state or province name
     * @param islandGroup The island group
     * @param island The island name
     * @param waterBody The water body name
     * @param style The search style to use
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    LocationMatch match(
            String locationID,
            String location,
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
     * Search for a location with a given locality name.
     * <p>
     * This method is vulnerable to ambiguity issues.
     * </p>
     *
     * @param locality The name of the location
     * @param style The search style to use
     * @param trace The trace level for debugging
     *
     * @return A matching taxon, with success=false if not found
     */
    LocationMatch match(String locality, SearchStyle style, Trace.TraceLevel trace);

    /**
     * Get location information via a specific taxon identifier.
     * <p>
     * See {@link #get(String, Boolean)}. By default, synonyms are not followed.
     * </p>
     *
     * @param locationID The location identifier
      *
     * @return A matching taxon, with success=false if not found
     */
    default LocationMatch get(String locationID) {
        return this.get(locationID, false);
    }

    /**
     * Get taxon information via a specific taxon identifier.
     *
     * @param locationID The locationidentifier
     * @param follow Follow syonynms to return the accepted location
     *
     * @return A matching taxon, with success=false if not found
     */
    LocationMatch get(String locationID, Boolean follow);

    /**
     * Bulk lookup for taxon identifiers
     *
     * @param locationIDs The list of location identifiers
     *
     * @return A corresponding list of name match results
     * @param follow Follow synonyms to the accepted location
     *
     * @see #get(String, Boolean)
     */
    List<LocationMatch> getAll(List<String> locationIDs, Boolean follow);
}
