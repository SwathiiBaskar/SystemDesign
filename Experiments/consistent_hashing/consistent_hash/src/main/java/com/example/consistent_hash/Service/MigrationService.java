package com.example.consistent_hash.Service;

import org.springframework.stereotype.Service;

import com.example.consistent_hash.NodeManager;
import com.example.consistent_hash.Hashing.PhysicalNode;
import com.example.consistent_hash.Model.Student;
import com.example.consistent_hash.Repository.StudentRepository;

import java.util.List;
import java.util.Map;

/**
 * Handles data migration whenever the set of MongoDB nodes changes.
 */
@Service
public class MigrationService {

    private final NodeManager nodeManager;
    private final StudentRepository studentRepository;

    public MigrationService(NodeManager nodeManager,
                            StudentRepository studentRepository) {
        this.nodeManager = nodeManager;
        this.studentRepository = studentRepository;
    }

    /**
     * Called after a new node has been added to the hash ring.
     * Moves only those records whose owner changes.
     */
    public int migrateAfterNodeAddition(String newNodeName) {

        int migratedCount = 0;

        for (Map.Entry<String, PhysicalNode> entry :
                nodeManager.getAllNodes().entrySet()) {

            String currentNodeName = entry.getKey();

            if (currentNodeName.equals(newNodeName)) {
                continue; // New node has no data yet
            }

            PhysicalNode sourceNode = entry.getValue();

            List<Student> students =
                    studentRepository.findAll(sourceNode.getMongoTemplate());

            for (Student student : students) {

                String correctOwner =
                        nodeManager.getRing().getNodeForKey(student.getId());

                if (!correctOwner.equals(currentNodeName)) {

                    PhysicalNode destination =
                            nodeManager.getNode(correctOwner);

                    studentRepository.save(
                            destination.getMongoTemplate(),
                            student);

                    studentRepository.delete(
                            sourceNode.getMongoTemplate(),
                            student.getId());

                    migratedCount++;

                    System.out.println(
                            "[MIGRATE] Student id=" + student.getId()
                                    + " moved from "
                                    + currentNodeName
                                    + " to "
                                    + correctOwner
                    );
                }
            }
        }

        return migratedCount;
    }

    /**
     * Called after a node has been removed from the hash ring,
     * but before removing it from the registry.
     */
    public int migrateBeforeNodeRemoval(String leavingNodeName) {

        PhysicalNode leavingNode =
                nodeManager.getNode(leavingNodeName);

        if (leavingNode == null) {
            return 0;
        }

        List<Student> students =
                studentRepository.findAll(leavingNode.getMongoTemplate());

        int migratedCount = 0;

        for (Student student : students) {

            String newOwner =
                    nodeManager.getRing().getNodeForKey(student.getId());

            PhysicalNode destination =
                    nodeManager.getNode(newOwner);

            studentRepository.save(
                    destination.getMongoTemplate(),
                    student);

            studentRepository.delete(
                    leavingNode.getMongoTemplate(),
                    student.getId());

            migratedCount++;

            System.out.println(
                    "[MIGRATE] Student id=" + student.getId()
                            + " moved from removed node "
                            + leavingNodeName
                            + " to "
                            + newOwner
            );
        }

        return migratedCount;
    }
}
