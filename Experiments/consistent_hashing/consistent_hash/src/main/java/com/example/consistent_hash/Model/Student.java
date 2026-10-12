package com.example.consistent_hash.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;

@Document(collection="students")
@Data
@AllArgsConstructor
public class Student {

    @Id
    private String id;

    private String name;
    private String department;
    private double cgpa;

}