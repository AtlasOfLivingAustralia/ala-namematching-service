package au.org.ala.names.ws;

import au.org.ala.names.ws.health.NameSearchHealthCheck;
import au.org.ala.names.ws.resources.NameSearchResource;
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
        // swagger-ui assets
        (new AssetsBundle("/swagger-static", "/swagger-static", (String)null, "swagger-assets")).run(configuration, environment);

        environment.jersey().register(new SwaggerResource("",
                configuration.getSwaggerBundleConfiguration().getSwaggerViewConfiguration(),
                configuration.getSwaggerBundleConfiguration().getSwaggerOAuth2Configuration(),
                configuration.getSwaggerBundleConfiguration().getContextRoot()));


        environment.jersey().register(new OpenApiResource()
                .openApiConfiguration(configuration.getSwaggerConfiguration()));

        final NameSearchResource resource = new NameSearchResource(configuration.getSearch());
        environment.jersey().register(resource);
        environment.healthChecks().register("namesearch", new NameSearchHealthCheck(resource));
    }
}
