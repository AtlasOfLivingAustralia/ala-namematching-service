package au.org.ala.names.ws;

import au.org.ala.names.ws.core.NameSearchConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ALANameMatchingServiceConfiguration extends Configuration {
    /** The name search configuration */
    @Valid
    @NotNull
    @JsonProperty
    @Getter
    @Setter
    private NameSearchConfiguration search = new NameSearchConfiguration();
}
