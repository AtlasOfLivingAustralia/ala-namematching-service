package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.LookupService;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.client.v1.MatchResult;
import au.org.ala.ws.ClientConfiguration;
import au.org.ala.ws.ClientException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.cache2k.Cache;
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
public class ALALookupServiceClient implements LookupService {

    //Wrapped service
    private final ALALookupRetrofitService alaLookupService;

    private final OkHttpClient okHttpClient;

    // A data cache, if configured
    private final Optional<Cache<NameSearch, MatchResult>> matchCache;

    /**
     * Creates an instance using the provided configuration settings.
     *
     * @param configuration Rest client configuration
     *
     * @throws IOException if unable to build underlying services
     */
    public ALALookupServiceClient(ClientConfiguration configuration) throws IOException {
        this.okHttpClient = configuration.createClient();
        this.matchCache = configuration.buildDataCache(NameSearch.class, MatchResult.class)
                .map(b -> b.loader(k -> MatchResult.empty(k)).build());
        alaLookupService = configuration.createRetrofitClient(this.okHttpClient, ALALookupRetrofitService.class);
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
        return this.call(this.alaLookupService.check(name, rank));
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
        return this.call(this.alaLookupService.autocomplete(query, max, includeSynonyms));
    }

    /**
     * Search for a record with a specific LSID.
     *
     * @param id      The taxon identifier
     * @return The matching LSID or null.
     */
    @Override
    public String searchForLsidById(String id) {
        return this.call(this.alaLookupService.searchForLsidById(id));
    }

    /**
     * Search for an LSID with a scientific name.
     *
     * @param name      The scientific name.
     * @return The matching LSID or null.
     */
    @Override
    public String searchForLSID(String name) {
        return this.call(this.alaLookupService.searchForLSID(name));
    }

    /**
     * Search for a list of LSIDs with a list of scientificName or scientificName(kingdom).
     *
     * @param taxaQueries      The scientificName or scientificName(kingdom).
     * @return The list of matches. Each match is the LSID or null.
     */
    @Override
    public List<String> getGuidsForTaxa(List<String> taxaQueries) {
        return this.call(this.alaLookupService.getGuidsForTaxa(taxaQueries));
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
        return this.call(this.alaLookupService.getCommonNamesForLSID(lsid, max));
    }

    /**
     * Get the scientific name for a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @param follow  Follow syonynms to return the accepted taxon
     * @return The matching name, or null for not found
     */
    @Override
    public String getName(String taxonID, Boolean follow) {
        return this.call(this.alaLookupService.getName(taxonID, follow));
    }

    /**
     * Bulk lookup of scientific names for taxon identifiers.
     *
     * @param taxonIDs The taxon identifiers
     * @param follow   Follow syonynms to return the accepted taxon
     * @return A list containing the matching name The matching taxon, or null for not found
     */
    @Override
    public List<String> getAllNames(List<String> taxonIDs, Boolean follow) {
        return this.call(this.alaLookupService.getAllNames(taxonIDs, follow));
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
