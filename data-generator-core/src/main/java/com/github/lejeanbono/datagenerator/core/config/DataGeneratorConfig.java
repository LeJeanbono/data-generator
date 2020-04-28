package com.github.lejeanbono.datagenerator.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataGeneratorConfig {

    @Value("${datagenerator.pluralRessources:false}")
    private boolean pluralRessources;

    @Value("${datagenerator.enabled:true}")
    private boolean enabled;

    public boolean isPluralRessources() {
        return pluralRessources;
    }

    public void setPluralRessources(boolean pluralRessources) {
        this.pluralRessources = pluralRessources;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Bean
    BeanGenerator beanGenerator() {
        return new BeanGenerator();
    }

}
