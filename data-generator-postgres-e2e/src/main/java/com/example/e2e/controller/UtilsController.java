package com.example.e2e.controller;

import com.example.e2e.entity.Configuration;
import com.example.e2e.repository.MyEntityRepository;
import com.github.lejeanbono.datagenerator.core.config.DataGeneratorConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("utils")
@RequiredArgsConstructor
public class UtilsController {

    private final MyEntityRepository myEntityRepository;

    private final DataGeneratorConfig dataGeneratorConfig;

    @DeleteMapping("/cleanAll")
    public ResponseEntity<?> cleanAll() {
        myEntityRepository.deleteAll();
        Configuration configuration = new Configuration();
        dataGeneratorConfig.setEnabled(configuration.isEnabled());
        dataGeneratorConfig.setPluralRessources(configuration.isPluralRessources());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/config")
    public ResponseEntity<?> changeConfig(@RequestBody Configuration configuration) {
        dataGeneratorConfig.setEnabled(configuration.isEnabled());
        dataGeneratorConfig.setPluralRessources(configuration.isPluralRessources());
        return ResponseEntity.noContent().build();
    }
}
