package com.cooperl.injector.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataGeneratorConfig {

    @Value("data-generator.plural-ressources")
    private Boolean pluralRessources;

    public Boolean getPluralRessources() {
        return pluralRessources;
    }

}
