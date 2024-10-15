package au.org.ala.names.ws.health;

import com.codahale.metrics.health.HealthCheck;

/**
 * Health check for the name search resource, to make
 * sure that nothing has gone away by accident.
 */
public class ResourceHealthCheck extends HealthCheck {
    private Checkable resource;

    public ResourceHealthCheck(Checkable resource) {
        this.resource = resource;
    }

    @Override
    protected Result check() throws Exception {
        if (this.resource.check()) {
            return Result.builder()
                    .healthy()
                    .withDetail("metrics", this.resource.getMetrics())
                    .build();
        } else {
            return Result.unhealthy("Name search unable to search index");
        }
    }
}
