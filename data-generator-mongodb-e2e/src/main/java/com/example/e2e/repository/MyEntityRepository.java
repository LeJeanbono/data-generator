package com.example.e2e.repository;

import com.example.e2e.entity.MyEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "myentity", path = "myentity")
public interface MyEntityRepository extends MongoRepository<MyEntity, String> {
}
