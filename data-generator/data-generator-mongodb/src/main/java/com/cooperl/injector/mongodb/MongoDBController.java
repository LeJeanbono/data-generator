package com.cooperl.injector.mongodb;

import com.cooperl.injector.core.generator.Generator;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("test/datas")
public class MongoDBController {

    private MongoTemplate mongoTemplate;

    private Generator generator;

    public MongoDBController(
            MongoTemplate mongoTemplate,
            Generator generator
    ) {
        this.mongoTemplate = mongoTemplate;
        this.generator = generator;
    }

    @DeleteMapping
    public ResponseEntity<?> removeDatas() {
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
        if (number > 1) {
            List<Object> response = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                Object generated = this.generator.generateObject(body, ressource);
                mongoTemplate.save(generated);
                response.add(generated);
            }
            return ResponseEntity.created(null).body(response);
        } else {
            Object generated = this.generator.generateObject(body, ressource);
            mongoTemplate.save(generated);
            return ResponseEntity.created(null).body(generated);
        }
    }

    @GetMapping(path = "/{ressource}")
    public ResponseEntity<?> getDatas(
            @PathVariable String ressource
    ) {
        Class<?> c = this.generator.getClassOfRessource(ressource);
        List<?> result = mongoTemplate.findAll(c);
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/{ressource}/{id}")
    public ResponseEntity<?> getData(
            @PathVariable String ressource,
            @PathVariable String id
    ) {
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
        Class<?> c = this.generator.getClassOfRessource(ressource);
        Object o = mongoTemplate.findById(id, c);
        if (o != null) {
            mongoTemplate.remove(o);
        }
        return ResponseEntity.noContent().build();
    }

}
