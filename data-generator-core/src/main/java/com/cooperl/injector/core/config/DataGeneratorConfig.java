package com.cooperl.injector.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "data-generator")
public class DataGeneratorConfig {

    private Boolean pluralRessources = false;

    public Boolean getPluralRessources() {
        return pluralRessources;
    }

}
