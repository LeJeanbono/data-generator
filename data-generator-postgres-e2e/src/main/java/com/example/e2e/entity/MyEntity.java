package com.example.e2e.entity;

import com.github.lejeanbono.datagenerator.core.annotation.TestData;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@TestData
@Entity
public class MyEntity {

    @Id
    private String id;

    private String name;

}
