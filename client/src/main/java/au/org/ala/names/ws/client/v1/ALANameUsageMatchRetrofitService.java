package au.org.ala.names.ws.client.v1;

import au.org.ala.names.ws.api.v1.NameMatchService;
import au.org.ala.names.ws.api.v1.NameSearch;
import au.org.ala.names.ws.api.v1.NameUsageMatch;
import au.org.ala.names.ws.api.v1.SearchStyle;
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

    @POST("/v1/api/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Body NameSearch search);

    @POST("/v1/api/searchAllByClassification")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> matchAll(@Body List<NameSearch> searches);

    @GET("/v1/api/searchByClassification")
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

    @GET("/v1/api/search")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Query("q") String scientificName, @Query("style") SearchStyle style);

    @GET("/v1/api/searchByVernacularName")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> matchVernacular(@Query("vernacularName") String vernacularName);

    @GET("/v1/api/getByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> get(@Query("taxonID") String taxonID, @Query("follow") boolean follow);

    @POST("/v1/api/getAllByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> getAll(@Query("taxonIDs") List<String> taxonIDs, @Query("follow") boolean follow);

    @GET("/v1/api/getNameByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<String> getName(@Query("taxonID") String taxonID, @Query("follow") boolean follow);

    @POST("/v1/api/getAllNamesByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<List<String>> getAllNames(@Query("taxonIDs") List<String> taxonIDs, @Query("follow") boolean follow);

    @GET("/v1/api/check")
    @Headers({"Content-Type: application/json"})
    Call<Boolean> check(@Query("name") String name, @Query("rank") String rank);

    @GET("/v1/api/autocomplete")
    @Headers({"Content-Type: application/json"})
    Call<List<Map>> autocomplete(@Query("q") String query, @Query("max") Integer max, @Query("includeSynonyms") Boolean includeSyonyms);

    @GET("/v1/api/searchForLsidById")
    @Headers({"Content-Type: application/json"})
    Call<String> searchForLsidById(@Query("id") String id);

    @GET("/v1/api/searchForLSID")
    @Headers({"Content-Type: application/json"})
    Call<String> searchForLSID(@Query("name") String name);

    @POST("/v1/api/getGuidsForTaxa")
    @Headers({"Content-Type: application/json"})
    Call<List<String>> getGuidsForTaxa(@Body List<String> taxaQueries);

    @GET("/v1/api/getCommonNamesForLSID")
    @Headers({"Content-Type: application/json"})
    Call<Set<String>> getCommonNamesForLSID(@Query("lsid") String lsid, @Query("max") Integer max);

}
