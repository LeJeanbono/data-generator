package com.cooperl.injector.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataGeneratorConfig {

    @Value("${datagenerator.pluralRessources:false}")
    private Boolean pluralRessources;

    public Boolean getPluralRessources() {
        return pluralRessources;
    }

    @Bean
    BeanGenerator beanGenerator() {
        return new BeanGenerator();
    }

}
