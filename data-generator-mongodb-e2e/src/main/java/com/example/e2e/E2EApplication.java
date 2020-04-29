package com.example.e2e;

import com.github.lejeanbono.datagenerator.core.annotation.EnableDataGenerator;
import com.github.lejeanbono.datagenerator.mongodb.configuration.DataGeneratorMongoDBConfig;
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
