package com.example.e2e.entity;

import com.cooperl.injector.core.annotation.TestData;
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
