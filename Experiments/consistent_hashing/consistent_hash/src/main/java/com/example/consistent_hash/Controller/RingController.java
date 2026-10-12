package com.example.consistent_hash.Controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.consistent_hash.NodeManager;
import com.example.consistent_hash.Hashing.PhysicalNode;
import com.example.consistent_hash.Hashing.VirtualNode;
import com.example.consistent_hash.Model.Student;
import com.example.consistent_hash.Repository.StudentRepository;

@RestController
public class RingController {

    private final NodeManager nodeManager;
    private final StudentRepository studentRepository;

    public RingController(NodeManager nodeManager,
                          StudentRepository studentRepository) {
        this.nodeManager = nodeManager;
        this.studentRepository = studentRepository;
    }

    /**
     * Displays the current hash ring.
     */
    @GetMapping("/ring")
    public List<VirtualNode> getRing() {
        return nodeManager.getRing().getRingSnapshot();
    }

    /**
     * Displays the number of student records
     * stored on each MongoDB node.
     */
    @GetMapping("/distribution")
    public Map<String, Integer> getDistribution() {

        Map<String, Integer> distribution = new LinkedHashMap<>();

        for (Map.Entry<String, PhysicalNode> entry :
                nodeManager.getAllNodes().entrySet()) {

            String nodeName = entry.getKey();

            PhysicalNode node = entry.getValue();

            List<Student> students =
                    studentRepository.findAll(node.getMongoTemplate());

            distribution.put(nodeName, students.size());
        }

        return distribution;
    }
}
