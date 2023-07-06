package au.org.ala.names.ws;

import au.org.ala.names.ws.core.NameSearchConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ALANameMatchingServiceConfiguration extends Configuration {
    /** The swagger UI configuration */
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    @Setter
    private SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();

    /** The openapi v3 configuration */
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    @Setter
    private SwaggerConfiguration swaggerConfiguration;

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
        // swagger UI
        this.swaggerBundleConfiguration.setTitle("ALA Namematching API");
        this.swaggerBundleConfiguration.setDescription("A taxonomy service that maps scientific name queries onto taxon concepts");
        this.swaggerBundleConfiguration.setContactUrl("https://ala.org.au");
        this.swaggerBundleConfiguration.setContactEmail("support@ala.org.au");
        this.swaggerBundleConfiguration.setResourcePackage("au.org.ala.names.ws.api,au.org.ala.names.ws.client,au.org.ala.names.ws.resources");
        this.swaggerBundleConfiguration.setLicense("Mozilla Public Licence 1.1");
        this.swaggerBundleConfiguration.setVersion("1.8.1");
        this.swaggerBundleConfiguration.getSwaggerViewConfiguration().setPageTitle("ALA Namematching API");

        // swagger openapi v3
        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("ALA Namematching API")
                .description("A taxonomy service that maps scientific name queries onto taxon concepts")
                .contact(new Contact().email("support@ala.org.au").url("https://ala.org.au"))
                .license(new License().name("Mozilla Public Licence 1.1"))
                .version("1.8.1");

        oas.info(info);
        swaggerConfiguration = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .resourcePackages(Stream.of("au.org.ala.names.ws")
                        .collect(Collectors.toSet()));
    }
}
