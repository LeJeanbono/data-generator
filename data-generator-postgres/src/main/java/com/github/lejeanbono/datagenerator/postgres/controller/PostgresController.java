package com.github.lejeanbono.datagenerator.postgres.controller;

import com.github.lejeanbono.datagenerator.core.config.DataGeneratorConfig;
import com.github.lejeanbono.datagenerator.core.generator.Generator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("test/datas")
@Transactional
public class PostgresController {

    private final EntityManager entityManager;

    private final Generator generator;

    private final DataGeneratorConfig dataGeneratorConfig;

    public PostgresController(
            EntityManager entityManager,
            Generator generator,
            DataGeneratorConfig dataGeneratorConfig
    ) {
        this.entityManager = entityManager;
        this.generator = generator;
        this.dataGeneratorConfig = dataGeneratorConfig;
    }

    @DeleteMapping
    public ResponseEntity<?> removeDatas() {
        if (!dataGeneratorConfig.isEnabled()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        for (Class<?> c : this.generator.getAllClassAnnotated()) {
            entityManager.createQuery("DELETE FROM " + c.getSimpleName()).executeUpdate();
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
                entityManager.persist(generated);
                response.add(generated);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            Object generated = this.generator.generateObject(body, ressource);
            entityManager.persist(generated);
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
        List<?> result = entityManager.createQuery("SELECT a FROM " + c.getSimpleName() + " a").getResultList();
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
        Object result = entityManager.find(c, id);
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
        Object o = entityManager.find(c, id);
        if (o != null) {
            entityManager.remove(o);
        }
        return ResponseEntity.noContent().build();
    }

}
