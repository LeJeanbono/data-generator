package com.github.lejeanbono.datagenerator.mongodb.controller;

import com.github.lejeanbono.datagenerator.core.config.DataGeneratorConfig;
import com.github.lejeanbono.datagenerator.core.generator.Generator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("test/datas")
public class MongoDBController {

    private final MongoTemplate mongoTemplate;

    private final Generator generator;

    private final DataGeneratorConfig dataGeneratorConfig;

    public MongoDBController(
            MongoTemplate mongoTemplate,
            Generator generator,
            DataGeneratorConfig dataGeneratorConfig
    ) {
        this.mongoTemplate = mongoTemplate;
        this.generator = generator;
        this.dataGeneratorConfig = dataGeneratorConfig;
    }

    @DeleteMapping
    public ResponseEntity<?> removeDatas() {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        for (Class<?> c : this.generator.getAllClassAnnotated()) {
            mongoTemplate.dropCollection(c);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{ressource}")
    public ResponseEntity<?> addData(
            @PathVariable String ressource,
            @RequestParam(value = "number", defaultValue = "1") Integer number,
            @RequestBody Map<String, Object> body
    ) {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (number > 1) {
            List<Object> response = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                Object generated = this.generator.generateObject(body, ressource);
                mongoTemplate.save(generated);
                response.add(generated);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            Object generated = this.generator.generateObject(body, ressource);
            mongoTemplate.save(generated);
            return ResponseEntity.status(HttpStatus.CREATED).body(generated);
        }
    }

    @GetMapping(path = "/{ressource}")
    public ResponseEntity<?> getDatas(
            @PathVariable String ressource
    ) {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Class<?> c = this.generator.getClassOfRessource(ressource);
        List<?> result = mongoTemplate.findAll(c);
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/{ressource}/{id}")
    public ResponseEntity<?> getData(
            @PathVariable String ressource,
            @PathVariable String id
    ) {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Class<?> c = this.generator.getClassOfRessource(ressource);
        Object result = mongoTemplate.findById(id, c);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(path = "/{ressource}/{id}")
    public ResponseEntity<?> removeData(
            @PathVariable String ressource,
            @PathVariable String id
    ) {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Class<?> c = this.generator.getClassOfRessource(ressource);
        Object o = mongoTemplate.findById(id, c);
        if (o != null) {
            mongoTemplate.remove(o);
        }
        return ResponseEntity.noContent().build();
    }

}
