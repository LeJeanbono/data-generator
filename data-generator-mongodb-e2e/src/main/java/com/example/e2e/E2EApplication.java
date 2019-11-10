package com.example.e2e;

import com.cooperl.injector.core.annotation.EnableDataGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDataGenerator(value = "")
@SpringBootApplication
public class E2EApplication {

	public static void main(String[] args) {
		SpringApplication.run(E2EApplication.class, args);
	}

}
