package au.org.ala.names.ws;

import au.org.ala.names.ws.core.NameSearchConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ALANameMatchingServiceConfiguration extends Configuration {
    /** The swagger configuration */
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    @Setter
    private SwaggerBundleConfiguration swagger = new SwaggerBundleConfiguration();
    /** The name search configuration */
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    @Setter
    private NameSearchConfiguration search = new NameSearchConfiguration();

    /**
     * Construct with default setttings.
     * <p>
     * The settings can be overridden in the
     * </p>
     */
    public ALANameMatchingServiceConfiguration()  {
        this.swagger.setTitle("ALA Namematching API");
        this.swagger.setDescription("A taxonomy service that maps scientific name queries onto taxon concepts");
        this.swagger.setContactUrl("https://ala.org.au");
        this.swagger.setContactEmail("support@ala.org.au");
        this.swagger.setResourcePackage("au.org.ala.names.ws.api,au.org.ala.names.ws.client,au.org.ala.names.ws.resources");
        this.swagger.setLicense("Mozilla Public Licence 1.1");
        this.swagger.setVersion("1.4-SNAPSHOT");
        this.swagger.getSwaggerViewConfiguration().setPageTitle("ALA Namematching API");
    }
}
