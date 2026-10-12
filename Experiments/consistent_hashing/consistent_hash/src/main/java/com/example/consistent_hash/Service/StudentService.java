package com.example.consistent_hash.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.consistent_hash.NodeManager;
import com.example.consistent_hash.Hashing.PhysicalNode;
import com.example.consistent_hash.Model.Student;
import com.example.consistent_hash.Repository.StudentRepository;

@Service
public class StudentService {

    private final NodeManager nodeManager;
    private final StudentRepository studentRepository;

    public StudentService(NodeManager nodeManager,
                          StudentRepository studentRepository) {
        this.nodeManager = nodeManager;
        this.studentRepository = studentRepository;
    }

    /**
     * Saves a student on the MongoDB node chosen by the hash ring.
     * Returns the node name where the student was stored.
     */
    public String addStudent(Student student) {

        String ownerNodeName =
                nodeManager.getRing().getNodeForKey(student.getId());

        PhysicalNode owner =
                nodeManager.getNode(ownerNodeName);

        studentRepository.save(
                owner.getMongoTemplate(),
                student
        );

        System.out.println(
                "[ROUTE] Student id=" + student.getId()
                        + " -> hash routed to "
                        + ownerNodeName
        );

        return ownerNodeName;
    }

    /**
     * Finds a student by ID.
     * The hash ring tells us exactly which node to query.
     */
    public Student getStudentById(String id) {

        String ownerNodeName =
                nodeManager.getRing().getNodeForKey(id);

        PhysicalNode owner =
                nodeManager.getNode(ownerNodeName);

        return studentRepository.findById(
                owner.getMongoTemplate(),
                id
        );
    }

    /**
     * Retrieves all students by querying every registered MongoDB node.
     */
    public List<Student> getAllStudents() {

        List<Student> allStudents = new ArrayList<>();

        nodeManager.getAllNodes().values().forEach(node ->
                allStudents.addAll(
                        studentRepository.findAll(
                                node.getMongoTemplate()
                        )
                )
        );

        return allStudents;
    }
}