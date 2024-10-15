package au.org.ala.names.ws.client.v2;

import au.org.ala.bayesian.Trace;
import au.org.ala.names.ws.api.SearchStyle;
import au.org.ala.names.ws.api.v2.NameMatchService;
import au.org.ala.names.ws.api.v2.NameSearch;
import au.org.ala.names.ws.api.v2.NameUsageMatch;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

/**
 * ALA name matching Retrofit Service client.
 *
 * @see NameMatchService
 */
interface ALANameUsageMatchRetrofitService {

    @POST("/api/v2/taxonomy/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Body NameSearch search, @Query("trace") Trace.TraceLevel trace);

    @POST("/api/v2/taxonomy/searchAllByClassification")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> matchAll(@Body List<NameSearch> searches, @Query("trace") Trace.TraceLevel trace);

    @GET("/api/v2/taxonomy/searchByClassification")
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
            @Query("continent") String continent,
            @Query("country") String country,
            @Query("stateProvince") String stateProvince,
            @Query("islandGroup") String islandGroup,
            @Query("island") String island,
            @Query("waterBody") String waterBody,
            @Query("style") SearchStyle style,
            @Query("trace") Trace.TraceLevel trace
    );

    @GET("/api/v2/taxonomy/search")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Query("q") String scientificName, @Query("style") SearchStyle style, @Query("trace") Trace.TraceLevel trace);

    @GET("/api/v2/taxonomy/searchByVernacularName")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> matchVernacular(@Query("vernacularName") String vernacularName, @Query("trace") Trace.TraceLevel trace);

    @GET("/api/v2/taxonomy/getByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> get(@Query("taxonID") String taxonID, @Query("follow") boolean follow);

    @POST("/api/v2/taxonomy/getAllByTaxonID")
    @Headers({"Content-Type: application/json"})
    Call<List<NameUsageMatch>> getAll(@Query("taxonIDs") List<String> taxonIDs, @Query("follow") boolean follow);
}
