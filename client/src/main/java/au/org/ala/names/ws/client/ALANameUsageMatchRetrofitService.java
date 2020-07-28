package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameMatchService;
import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * ALA name matching Retrofit Service client.
 *
 * @see NameMatchService
 */
interface ALANameUsageMatchRetrofitService {

    @POST("/api/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Body NameSearch search);

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
            @Query("rank") String rank
    );

    @GET("/api/search")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Query("q") String scientificName);

    @GET("/api/searchByVernacularName")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> matchVernacular(@Query("vernacularName") String vernacularName);

    @GET("/api/getByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> get(@Query("taxonID") String taxonID);

    @GET("/api/check")
    @Headers({"Content-Type: application/json"})
    Call<Boolean> check(@Query("name") String name, @Query("rank") String rank);

}
