package com.example.consistent_hash.Repository;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import com.example.consistent_hash.Model.Student;

@Repository
public class StudentRepository {

    public void save(MongoTemplate mt,
                     Student s){
        mt.save(s);
    }

    public Student findById(
            MongoTemplate mt,
            String id){

        return mt.findById(id,Student.class);
    }

    public List<Student> findAll(
            MongoTemplate mt){

        return mt.findAll(Student.class);
    }

    public void delete(MongoTemplate mongoTemplate, String id) {
        Student student = mongoTemplate.findById(id, Student.class);
        if (student != null) {
            mongoTemplate.remove(student);
        }
    }
}