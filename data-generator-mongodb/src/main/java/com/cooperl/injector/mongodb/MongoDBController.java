package com.cooperl.injector.mongodb;

import com.cooperl.injector.core.annotation.TestData;
import com.cooperl.injector.core.generator.Generator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("test/datas")
public class MongoDBController {

    private MongoTemplate mongoTemplate;

    private Generator generator;

    public MongoDBController(MongoTemplate mongoTemplate, Generator generator) {
        this.mongoTemplate = mongoTemplate;
        this.generator = generator;
    }

    @DeleteMapping
    public ResponseEntity removeDatas() throws ClassNotFoundException {
        for (Class c : this.generator.getAllClassAnnotated()) {
            mongoTemplate.dropCollection(c);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/{ressource}")
    public ResponseEntity addData(
            @PathVariable String ressource,
            @RequestParam(value = "number", defaultValue = "1") Integer number,
            @RequestBody Object body
    ) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {

        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(TestData.class));
        Object response = null;
        for (int i = 0; i < number; i++) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            Class c = this.generator.getClassOfRessource(ressource);
            Object pojo = objectMapper.convertValue(body, c);
            Object myPojo = this.generator.generateObject(c);
            myPojo = this.generator.merge(myPojo, pojo);
            mongoTemplate.save(myPojo);
            if (number > 1) {
                if (response == null) {
                    response = new ArrayList<>();
                }
                ((ArrayList) response).add(myPojo);
            } else {
                response = myPojo;
            }
        }
        return ResponseEntity.created(null).body(response);
    }

    @GetMapping(path = "/{ressource}")
    public ResponseEntity getDatas(@PathVariable String ressource) throws ClassNotFoundException {
        Class c = this.generator.getClassOfRessource(ressource);
        List result = mongoTemplate.findAll(c);
        return ResponseEntity.ok(result);
    }

    @GetMapping(path = "/{ressource}/{id}")
    public ResponseEntity getData(@PathVariable String ressource, @PathVariable String id) throws ClassNotFoundException {
        Class c = this.generator.getClassOfRessource(ressource);
        Object result = mongoTemplate.findById(id, c);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping(path = "/{ressource}/{id}")
    public ResponseEntity removeData(@PathVariable String ressource, @PathVariable String id) throws ClassNotFoundException {
        Class c = this.generator.getClassOfRessource(ressource);
        Object o = mongoTemplate.findById(id, c);
        if (o != null) {
            mongoTemplate.remove(o);
        }
        return ResponseEntity.noContent().build();
    }

}
