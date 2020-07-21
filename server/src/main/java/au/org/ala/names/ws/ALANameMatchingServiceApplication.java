package au.org.ala.names.ws;

import au.org.ala.names.ws.health.NameSearchHealthCheck;
import au.org.ala.names.ws.resources.NameSearchResource;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.bundles.redirect.PathRedirect;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

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
        bootstrap.addBundle(new RedirectBundle(
                new PathRedirect(ImmutableMap.<String, String>builder()
                        .put("/", "/swagger")
                        .put("/index.htm", "/swagger")
                        .put("/index.html", "/swagger")
                        .build())
        ));
        bootstrap.addBundle(new SwaggerBundle<ALANameMatchingServiceConfiguration>() {
            @Override
            protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(ALANameMatchingServiceConfiguration configuration) {
                 return configuration.getSwagger();
            }
        });
    }

    @Override
    public void run(final ALANameMatchingServiceConfiguration configuration,
                    final Environment environment) {
        final NameSearchResource resource = new NameSearchResource(configuration.getSearch());
        environment.jersey().register(resource);
        environment.healthChecks().register("namesearch", new NameSearchHealthCheck(resource));
    }
}
