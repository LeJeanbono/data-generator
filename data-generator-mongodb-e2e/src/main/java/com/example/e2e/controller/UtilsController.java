package com.example.e2e.controller;

import com.example.e2e.repository.MyEntityRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("utils")
@RequiredArgsConstructor
public class UtilsController {

    private final MyEntityRepository myEntityRepository;

    @DeleteMapping("/cleanAll")
    public ResponseEntity<?> cleanAll() {
        myEntityRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
