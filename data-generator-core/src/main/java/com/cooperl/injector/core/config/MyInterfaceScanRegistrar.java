package com.cooperl.injector.core.config;

import com.cooperl.injector.core.annotation.EnableDataGenerator;
import com.cooperl.injector.core.annotation.TestData;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyInterfaceScanRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // Get the EnableDataGenerator annotation attributes
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(EnableDataGenerator.class.getCanonicalName());

        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("value");

            if (basePackages.length == 0) {
                // If value attribute is not set, fallback to the package of the annotated class
                basePackages = new String[]{((StandardAnnotationMetadata) metadata).getIntrospectedClass().getPackage().getName()};
            }

            // using these packages, scan for interface annotated with MyCustomBean
            ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false, environment);
            provider.addIncludeFilter(new AnnotationTypeFilter(TestData.class));

            BeanDefinition bd = new RootBeanDefinition(InjectorConfig.class);
            List<BeanDefinition> beans = new ArrayList<>();
            // Scan all packages
            for (String basePackage : basePackages) {
                beans.addAll(provider.findCandidateComponents(basePackage));
            }
            bd.getConstructorArgumentValues().addGenericArgumentValue(beans);
            registry.registerBeanDefinition("injectorConfig", bd);
        }
    }
}