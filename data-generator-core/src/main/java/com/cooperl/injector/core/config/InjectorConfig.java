package com.cooperl.injector.core.config;

import lombok.NoArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class InjectorConfig {

    private List<String> beansClassName = new ArrayList<>();

    public InjectorConfig(List<BeanDefinition> beans) {
        for (BeanDefinition bean : beans) {
            beansClassName.add(bean.getBeanClassName());
        }
    }

    public void setBeansClassName(List<BeanDefinition> beans) {
        for (BeanDefinition bean : beans) {
            beansClassName.add(bean.getBeanClassName());
        }
    }

    public List<String> getBeansClassName() {
        return beansClassName;
    }

}
