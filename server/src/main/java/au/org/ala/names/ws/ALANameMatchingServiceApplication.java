package au.org.ala.names.ws;

import au.org.ala.names.ws.core.NameSearchConfiguration;
import au.org.ala.names.ws.health.ResourceHealthCheck;
import au.org.ala.names.ws.resources.*;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.bundles.redirect.PathRedirect;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.federecio.dropwizard.swagger.SwaggerResource;
import io.swagger.converter.ModelConverters;
import io.swagger.jackson.ModelResolver;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

public class ALANameMatchingServiceApplication extends Application<ALANameMatchingServiceConfiguration> {

    public static void main(final String[] args) throws Exception {
        new ALANameMatchingServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "ALANameMatchingService";
    }

    @Override
    public void initialize(final Bootstrap<ALANameMatchingServiceConfiguration> bootstrap) {
        // swagger ui
        bootstrap.addBundle(new RedirectBundle(
                new PathRedirect(ImmutableMap.<String, String>builder()
                        .put("/", "/swagger")
                        .put("/index.htm", "/swagger")
                        .put("/index.html", "/swagger")
                        .put("/swagger.json", "/openapi.json")
                        .put("/openapi/openapi.json", "/openapi.json")
                        .put("/openapi/openapi.yaml", "/openapi.yaml")
                        .put("/openapi", "/swagger")
                        .build())
        ));
        bootstrap.addBundle(new ViewBundle());
        ModelConverters.getInstance().addConverter(new ModelResolver(bootstrap.getObjectMapper()));
    }

    @Override
    public void run(final ALANameMatchingServiceConfiguration configuration,
                    final Environment environment) {

        try {
            // swagger-ui assets
            (new AssetsBundle("/swagger-static", "/swagger-static", (String)null, "swagger-assets")).run(configuration, environment);

            environment.jersey().register(new SwaggerResource("",
                    configuration.getSwaggerBundleConfiguration().getSwaggerViewConfiguration(),
                    configuration.getSwaggerBundleConfiguration().getSwaggerOAuth2Configuration(),
                    configuration.getSwaggerBundleConfiguration().getContextRoot()));


            environment.jersey().register(new OpenApiResource()
                    .openApiConfiguration(configuration.getSwaggerConfiguration()));

            final NameSearchConfiguration ns = configuration.getSearch();
            final TaxonomyResource taxonomyResource = new TaxonomyResource(ns);
            final LocationResource locationResource = new LocationResource(ns, taxonomyResource);
            final LookupResource lookupResource = new LookupResource(ns, taxonomyResource);
            final NameSearchResourceV1 v1Resource = new NameSearchResourceV1(ns, taxonomyResource);
            final NameSearchResourceV2 v2Resource = new NameSearchResourceV2(ns, taxonomyResource, locationResource);
            environment.jersey().register(lookupResource);
            environment.jersey().register(v1Resource);
            environment.jersey().register(v2Resource);
            environment.jersey().register(locationResource);
            environment.healthChecks().register("taxonomy", new ResourceHealthCheck(taxonomyResource));
            environment.healthChecks().register("location", new ResourceHealthCheck(locationResource));
            environment.healthChecks().register("lookup", new ResourceHealthCheck(lookupResource));
            environment.healthChecks().register("v1", new ResourceHealthCheck(v1Resource));
            environment.healthChecks().register("v2", new ResourceHealthCheck(v2Resource));
            environment.lifecycle().manage(taxonomyResource);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
