package au.org.ala.names.ws;

import au.org.ala.names.ws.resources.NameSearchResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

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
    }

    @Override
    public void run(final ALANameMatchingServiceConfiguration configuration,
                    final Environment environment) {
        final NameSearchResource resource = new NameSearchResource(configuration.getSearch());
        environment.jersey().register(resource);
    }
}
