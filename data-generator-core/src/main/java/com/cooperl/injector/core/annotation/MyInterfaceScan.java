package com.cooperl.injector.core.annotation;

import com.cooperl.injector.core.config.MyInterfaceScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({MyInterfaceScanRegistrar.class})
public @interface MyInterfaceScan {

    String[] value() default {};
}