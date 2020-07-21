package au.org.ala.names.ws.health;

import au.org.ala.names.ws.resources.NameSearchResource;
import com.codahale.metrics.health.HealthCheck;

/**
 * Health check for the name search resource, to make
 * sure that nothing has gone away by accident.
 */
public class NameSearchHealthCheck extends HealthCheck {
    private NameSearchResource resource;

    public NameSearchHealthCheck(NameSearchResource resource) {
        this.resource = resource;
    }

    @Override
    protected Result check() throws Exception {
        if (this.resource.check())
            return Result.healthy();
        else
            return Result.unhealthy("Name search unable to search index");
    }
}
