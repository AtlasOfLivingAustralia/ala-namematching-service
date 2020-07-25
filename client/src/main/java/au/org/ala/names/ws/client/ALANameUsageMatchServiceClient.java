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
import java.util.Comparator;
import java.util.Objects;
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
     * Get taxon information via a specific taxon identifier.
     *
     * @param taxonID The taxon identifier
     * @return A matching taxon, with success=false if not found
     */
    @Override
    public NameUsageMatch get(String taxonID) {
        return this.call(this.alaNameUsageMatchService.get(taxonID));
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
     * @return The response
     *
     * @throws HttpException to propagate an error
     * @throws ClientException if unable to contact the service
     */
    private <T> T call(Call<T> call) throws HttpException, ClientException {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }
            log.debug("Response returned error - {}", response);
            throw new HttpException(response); // Propagates the failed response
        } catch (IOException ex) {
            throw new ClientException("Unable to contact service for " + call, ex);
        }
    }
}
