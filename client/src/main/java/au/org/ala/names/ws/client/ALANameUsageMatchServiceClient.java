package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameMatchService;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.ClientException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class ALANameUsageMatchServiceClient implements NameMatchService {

    //Wrapped service
    private final ALANameUsageMatchRetrofitService alaNameUsageMatchService;

    private final OkHttpClient okHttpClient;

    /**
     * Creates an instance using the provided configuration settings.
     * 
     * @param configuration Rest client configuration
     *                      
     * @throws IOException if unable to build underlying services
     */
    public ALANameUsageMatchServiceClient(ClientConfiguration configuration) throws IOException {
        this.okHttpClient = configuration.createClient();
        alaNameUsageMatchService = configuration.createRetrofitClient(this.okHttpClient, ALANameUsageMatchRetrofitService.class);
    }

    /**
     * Search for a match for a search key.
     * 
     * @param search The search key
     *
     * @return The matching result
     *
     * @see NameMatchService#match(NameSearch)
      */
    @Override
    public NameUsageMatch match(NameSearch search)  {
        return this.call(this.alaNameUsageMatchService.match(search));
    }

    /**
     * Find a mataching taxon based on the Linnaean hierarchy.
     *
     * @param scientificName       The scientific name
     * @param kingdom              The Linnaean kingdom name
     * @param phylum               The Linnaean phylum name
     * @param clazz                The Linnaean class name
     * @param order                The Linnaean order name
     * @param family               The Linnaean family name
     * @param genus                The Linnaean genus name
     * @param specificEpithet      The specific epithet (species component of a binomial name)
     * @param infraspecificEpithet The infraspecific epithet (subspecies, variety etc component of a trinomial name)
     * @param rank                 The Linnaean rank name
     * @return A matching taxon, with success=false if not found
     * @see #match(NameSearch)
     */
    @Override
    public NameUsageMatch match(String scientificName, String kingdom, String phylum, String clazz, String order, String family, String genus, String specificEpithet, String infraspecificEpithet, String rank) {
        return this.call(this.alaNameUsageMatchService.match(scientificName, kingdom, phylum, clazz, order, family, genus, specificEpithet, infraspecificEpithet, rank));
    }

    /**
     * Search for a taxon with a given scientific name.
     * <p>
     * This method is vulnerable to ambiguity issues, such as homonyms
     * or pro-parte synonyms.
     * </p>
     *
     * @param scientificName The scientific name of the taxon
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch match(String scientificName) {
        return this.call(this.alaNameUsageMatchService.match(scientificName));
    }

    /**
     * Search for a taxon with a given vernacular (common) name.
     * <p>
     * Vernacular names do not reliably map onto individual taxa.
     * A "best-effort" result is returned.
     * </p>
     *
     * @param vernacularName The vernacular name to search for
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch matchVernacular(String vernacularName) {
        return this.call(this.alaNameUsageMatchService.matchVernacular(vernacularName));
    }

    /**
     * Set a set of valid names for a specific rank.
     * <p>
     * This is really only intended for hihger ranks, such as kingdom.
     * If an empty set is returned, then it should be assumed that any name is valid.
     * </p>
     *
     * @param rank The rank
     * @return The list of valid names, empty for any
     */
    @Override
    public Boolean check(String name, String rank) {
        return this.call(this.alaNameUsageMatchService.check(name, rank));
    }

    /**
     * Autocomplete search
     *
     * @param query             The beginning of the names to match (scientific or vernacular)
     * @param max               The maximum number of matches to return
     * @param includeSynonyms   Include synonym matches
     * @return  The list of autocomplete matches.
     */
    @Override
    public List<Map> autocomplete(String query, Integer max, Boolean includeSynonyms) {
        return this.call(this.alaNameUsageMatchService.autocomplete(query, max, includeSynonyms));
    }

    /**
     * Search for a record with a specific LSID.
     *
     * @param lsid      The taxon identifier
     * @return The matching LSID or null.
     */
    @Override
    public String searchForLsidById(String id) {
        return this.call(this.alaNameUsageMatchService.searchForLsidById(id));
    }

    /**
     * Search for an LSID with a scientific name.
     *
     * @param name      The scientific name.
     * @return The matching LSID or null.
     */
    @Override
    public String searchForLSID(String name) {
        return this.call(this.alaNameUsageMatchService.searchForLSID(name));
    }

    /**
     * Search for a list of LSIDs with a list of scientificName or scientificName(kingdom).
     *
     * @param taxaQueries      The scientificName or scientificName(kingdom).
     * @return The list of matches. Each match is the LSID or null.
     */
    @Override
    public List<String> getGuidsForTaxa(List<String> taxaQueries) {
        return this.call(this.alaNameUsageMatchService.getGuidsForTaxa(taxaQueries));
    }

    /**
     * Search for a list of vernacular names for an LSID.
     *
     * @param lsid      The taxon identifier
     * @param max       The maximum number of vernaculars to return
     * @return The list of matches. Each match is the LSID or null.
     */
    @Override
    public Set<String> getCommonNamesForLSID(String lsid, Integer max) {
        return this.call(this.alaNameUsageMatchService.getCommonNamesForLSID(lsid, max));
    }

    /**
     * Get taxon information via a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch get(String taxonID, boolean follow) {
        return this.call(this.alaNameUsageMatchService.get(taxonID, follow));
    }

    /**
     * Bulk lookup of taxon information for a list of taxon identifiers.
     *
     * @param taxonIDs The list of taxon identifiers
     * @param follow Follow synonyms to the accepted taxon
     *
     * @return The list of matches, will fail results for no match.
     */
    @Override
    public List<NameUsageMatch> getAll(List<String> taxonIDs, boolean follow) {
        return this.call(this.alaNameUsageMatchService.getAll(taxonIDs, follow));
    }

    /**
     * Get the scientific name for a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @param follow  Follow syonynms to return the accepted taxon
     * @return The matching name, or null for not found
     */
    @Override
    public String getName(String taxonID, boolean follow) {
        return this.call(this.alaNameUsageMatchService.getName(taxonID, follow));
    }

    /**
     * Bulk lookup of scientific names for taxon identifiers.
     *
     * @param taxonIDs The taxon identifiers
     * @param follow   Follow syonynms to return the accepted taxon
     * @return A list containing the matching name The matching taxon, or null for not found
     */
    @Override
    public List<String> getAllNames(List<String> taxonIDs, boolean follow) {
        return this.call(this.alaNameUsageMatchService.getAllNames(taxonIDs, follow));
    }

    @Override
    public void close() throws IOException {
        if (Objects.nonNull(okHttpClient) && Objects.nonNull(okHttpClient.cache())) {
            File cacheDirectory = okHttpClient.cache().directory();
            if (cacheDirectory.exists()) {
                try (Stream<File> files = Files.walk(cacheDirectory.toPath())
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)) {
                    files.forEach(File::delete);
                }
            }
        }
    }

    /**
     * Make a call to the web service and return teh result
     *
     * @param call The HTTP call
     *
     * @param <T> The type of response expected
     *
     * @return The response, returns null if a 204 (no content) result is returned from the server
     *
     * @throws HttpException to propagate an error
     * @throws ClientException if unable to contact the service
     */
    private <T> T call(Call<T> call) throws HttpException, ClientException {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful()) {
                return response.body();
            }
            log.debug("Response returned error - {}", response);
            throw new HttpException(response); // Propagates the failed response
        } catch (IOException ex) {
            throw new ClientException("Unable to contact service for " + call, ex);
        }
    }
}
