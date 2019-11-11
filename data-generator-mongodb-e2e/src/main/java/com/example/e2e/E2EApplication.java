package com.example.e2e;

import com.cooperl.injector.core.annotation.EnableDataGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@EnableDataGenerator(value = "com.example.e2e")
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.e2e", "com.cooperl.injector"})
public class E2EApplication {

	public static void main(String[] args) {
		SpringApplication.run(E2EApplication.class, args);
	}

}
