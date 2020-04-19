package com.example.e2e;

import com.cooperl.injector.core.annotation.EnableDataGenerator;
import com.cooperl.injector.mongodb.configuration.DataGeneratorMongoDBConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@EnableDataGenerator
@SpringBootApplication
@Import(DataGeneratorMongoDBConfig.class)
public class E2EApplication {

	public static void main(String[] args) {
		SpringApplication.run(E2EApplication.class, args);
	}

}
