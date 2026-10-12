package com.example.consistent_hash.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.consistent_hash.Model.Student;
import com.example.consistent_hash.Service.StudentService;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * Creates a new student.
     * If no ID is supplied, a random UUID is generated.
     */
    @PostMapping
    public Map<String, Object> addStudent(@RequestBody Student student) {

        if (student.getId() == null || student.getId().isBlank()) {
            student.setId(UUID.randomUUID().toString());
        }

        String ownerNode = studentService.addStudent(student);

        Map<String, Object> response = new HashMap<>();
        response.put("student", student);
        response.put("storedOnNode", ownerNode);

        return response;
    }

    /**
     * Retrieves all students from all MongoDB nodes.
     */
    @GetMapping
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    /**
     * Retrieves a student by ID.
     */
    @GetMapping("/{id}")
    public Student getStudent(@PathVariable String id) {
        return studentService.getStudentById(id);
    }
}