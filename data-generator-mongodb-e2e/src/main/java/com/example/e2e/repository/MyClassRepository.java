package com.example.e2e.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "myclass", path = "myclass")
public class MyClassRepository {
}
