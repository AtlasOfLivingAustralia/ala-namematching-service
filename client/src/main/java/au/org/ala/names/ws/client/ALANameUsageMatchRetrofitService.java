package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameMatchService;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import au.org.ala.names.ws.api.SearchStyle;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ALA name matching Retrofit Service client.
 *
 * @see NameMatchService
 */
interface ALANameUsageMatchRetrofitService {

    @POST("/api/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Body NameSearch search);

    @POST("/api/searchAllByClassification")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> matchAll(@Body List<NameSearch> searches);

    @GET("/api/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(
            @Query("scientificName") String scientificName,
            @Query("kingdom") String kingdom,
            @Query("phylum") String phylum,
            @Query("class") String clazz,
            @Query("order") String order,
            @Query("family") String family,
            @Query("genus") String genus,
            @Query("specificEpithet") String specificEpithet,
            @Query("infraspecificEpithet") String infraspecificEpithet,
            @Query("rank") String rank,
            @Query("style")SearchStyle style
    );

    @GET("/api/search")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Query("q") String scientificName, @Query("style") SearchStyle style);

    @GET("/api/searchByVernacularName")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> matchVernacular(@Query("vernacularName") String vernacularName);

    @GET("/api/getByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> get(@Query("taxonID") String taxonID, @Query("follow") boolean follow);

    @POST("/api/getAllByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> getAll(@Query("taxonIDs") List<String> taxonIDs, @Query("follow") boolean follow);

    @GET("/api/getNameByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<String> getName(@Query("taxonID") String taxonID, @Query("follow") boolean follow);

    @POST("/api/getAllNamesByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<List<String>> getAllNames(@Query("taxonIDs") List<String> taxonIDs, @Query("follow") boolean follow);

    @GET("/api/check")
    @Headers({"Content-Type: application/json"})
    Call<Boolean> check(@Query("name") String name, @Query("rank") String rank);

    @GET("/api/autocomplete")
    @Headers({"Content-Type: application/json"})
    Call<List<Map>> autocomplete(@Query("q") String query, @Query("max") Integer max, @Query("includeSynonyms") Boolean includeSyonyms);

    @GET("/api/searchForLsidById")
    @Headers({"Content-Type: application/json"})
    Call<String> searchForLsidById(@Query("id") String id);

    @GET("/api/searchForLSID")
    @Headers({"Content-Type: application/json"})
    Call<String> searchForLSID(@Query("name") String name);

    @POST("/api/getGuidsForTaxa")
    @Headers({"Content-Type: application/json"})
    Call<List<String>> getGuidsForTaxa(@Body List<String> taxaQueries);

    @GET("/api/getCommonNamesForLSID")
    @Headers({"Content-Type: application/json"})
    Call<Set<String>> getCommonNamesForLSID(@Query("lsid") String lsid, @Query("max") Integer max);

}
