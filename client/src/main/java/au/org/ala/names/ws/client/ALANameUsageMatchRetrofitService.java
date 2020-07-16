package au.org.ala.names.ws.client;

import au.org.ala.names.ws.api.NameSearch;
import au.org.ala.names.ws.api.NameUsageMatch;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * ALA name matching Retrofit Service client.
 */
interface ALANameUsageMatchRetrofitService {

    @POST("/api/searchByClassification")
    @Headers({"Content-Type: application/json"})
    Call<NameUsageMatch> match(@Body NameSearch nameMatch);
}
