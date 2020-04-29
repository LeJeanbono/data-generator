package com.example.e2e.entity;

import com.github.lejeanbono.datagenerator.core.annotation.TestData;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@TestData
@Document
public class MyEntity {

    @Id
    private String id;

    private String name;

}
