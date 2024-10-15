package au.org.ala.names.ws.client.v2;

import au.org.ala.bayesian.Trace;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v2.NameMatchService;
import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import au.org.ala.names.ws.client.Result;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ALANameUsageMatchServiceClient implements NameMatchService {

    //Wrapped service
    private final ALANameUsageMatchRetrofitService alaNameUsageMatchService;

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
    public ALANameUsageMatchServiceClient(ClientConfiguration configuration) throws IOException {
        this.okHttpClient = configuration.createClient();
        this.matchCache = configuration.buildDataCache(NameSearch.class, MatchResult.class)
                .map(b -> b.loader(k -> MatchResult.empty(k)).build());
        alaNameUsageMatchService = configuration.createRetrofitClient(this.okHttpClient, ALANameUsageMatchRetrofitService.class);
    }

    /**
     * Search for a match for a search key.
     *
     * @param search The search key
     *
     * @return The matching result
     *
     * @see NameMatchService#match(NameSearch, au.org.ala.bayesian.Trace.TraceLevel)
      */
    @Override
    public NameUsageMatch match(NameSearch search, Trace.TraceLevel trace)  {
        if (!this.matchCache.isPresent() || (trace != null && trace != Trace.TraceLevel.NONE))
            return this.call(this.alaNameUsageMatchService.match(search, trace));
        MatchResult result = this.matchCache.get().get(search);
        return this.resolve(result);
    }

    // Resolve a result and return the match
    protected NameUsageMatch resolve(MatchResult result) {
        if (result.getValue() == null) {
            NameUsageMatch match = this.call(this.alaNameUsageMatchService.match(result.getKey(), Trace.TraceLevel.NONE));
            result.setValue(match);
            this.matchCache.get().put(result.getKey(), result);
        }
        return result.getValue();
    }

    /**
     * Search for a match for a list of search keys.
     *
     * @param searches The search keys
     *
     * @return The matching results
     *
     * @see NameMatchService#matchAll(List, au.org.ala.bayesian.Trace.TraceLevel)
     */
    @Override
    public List<NameUsageMatch> matchAll(List<NameSearch> searches, Trace.TraceLevel trace)  {
        if (!this.matchCache.isPresent() || (trace != null && trace != Trace.TraceLevel.NONE))
            return this.call(this.alaNameUsageMatchService.matchAll(searches, trace));
        List<MatchResult> results = searches.stream()
                .map(s -> this.matchCache.get().get(s))
                .collect(Collectors.toList());
        return this.resolve(results);
    }

    // Resolve a list of results and return the matches
    // Uses a bulk query
    protected List<NameUsageMatch> resolve(List<MatchResult> results) {
        if (results.stream().allMatch(Result::isSet)) {
            return results.stream().map(Result::getValue).collect(Collectors.toList());
        }
        final List<NameSearch> query = results.stream().map(r -> r.isSet() ? null : r.getKey()).collect(Collectors.toList());
        final List<NameUsageMatch> values = this.call(this.alaNameUsageMatchService.matchAll(query, Trace.TraceLevel.NONE));
        final List<NameUsageMatch> matches = new ArrayList<>(query.size());
        final int len = Math.min(query.size(), values.size());
        for (int i = 0; i < len; i++) {
            NameUsageMatch match = values.get(i);
            MatchResult result = results.get(i);
            if (match != null) {
                result.setValue(match);
                this.matchCache.get().put(result.getKey(), result);
            }
            matches.add(result.getValue());
        }
        return matches;
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
     * @param continent The continent where the observation took place
     * @param country The country where the observation took place
     * @param stateProvince The state or province where the observation took place
     * @param islandGroup The island group where the observation took place
     * @param island The island where the observation took place
     * @param waterBody The water body (ocean, sea, bay etc.) where the observation took place
     * @param style                The search style (defaults to {@link SearchStyle#MATCH}
     * @param trace The trace level (defaults to {@link Trace.TraceLevel#NONE}
     * @return A matching taxon, with success=false if not found
     * @see #match(NameSearch, au.org.ala.bayesian.Trace.TraceLevel)
     */
    @Override
    public NameUsageMatch match(
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
            Trace.TraceLevel trace) {
        return this.call(this.alaNameUsageMatchService.match(
                scientificName,
                kingdom,
                phylum,
                clazz,
                order,
                family,
                genus,
                specificEpithet,
                infraspecificEpithet,
                rank,
                continent,
                country,
                stateProvince,
                islandGroup,
                island,
                waterBody,
                style,
                trace));
    }

    /**
     * Search for a taxon with a given scientific name.
     * <p>
     * This method is vulnerable to ambiguity issues, such as homonyms
     * or pro-parte synonyms.
     * </p>
     *
     * @param scientificName The scientific name of the taxon
     * @param style The search style (defaults to {@link SearchStyle#MATCH}
     * @param trace The trace level (defaults to {@link Trace.TraceLevel#NONE}
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch match(String scientificName, SearchStyle style, Trace.TraceLevel trace) {
        return this.call(this.alaNameUsageMatchService.match(scientificName, style, trace));
    }

    /**
     * Search for a taxon with a given vernacular (common) name.
     * <p>
     * Vernacular names do not reliably map onto individual taxa.
     * A "best-effort" result is returned.
     * </p>
     *
     * @param vernacularName The vernacular name to search for
     * @param trace The trace level (defaults to {@link Trace.TraceLevel#NONE}
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch matchVernacular(String vernacularName, Trace.TraceLevel trace) {
        return this.call(this.alaNameUsageMatchService.matchVernacular(vernacularName, trace));
    }

    /**
     * Get taxon information via a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch get(String taxonID, Boolean follow) {
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
    public List<NameUsageMatch> getAll(List<String> taxonIDs, Boolean follow) {
        return this.call(this.alaNameUsageMatchService.getAll(taxonIDs, follow));
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
