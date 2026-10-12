package com.example.consistent_hash.Controller;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.consistent_hash.NodeManager;
import com.example.consistent_hash.Service.MigrationService;
import com.mongodb.client.MongoClients;

import java.util.HashMap;
import java.util.Map;

/**
 * REST endpoints for dynamically adding and removing MongoDB storage
 * nodes at runtime, and for triggering the corresponding data migration.
 */
@RestController
@RequestMapping("/nodes")
public class NodeController {

    private final NodeManager nodeManager;
    private final MigrationService migrationService;

    public NodeController(NodeManager nodeManager,
                          MigrationService migrationService) {
        this.nodeManager = nodeManager;
        this.migrationService = migrationService;
    }

    /**
     * Adds a new MongoDB node to the hash ring and migrates
     * only the records that now belong to it.
     *
     * Example Request Body:
     * {
     *   "name": "node4",
     *   "uri": "mongodb://localhost:27021",
     *   "database": "studentdb_node4"
     * }
     */
    @PostMapping("/add")
    public Map<String, Object> addNode(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        String uri = request.get("uri");
        String database = request.get("database");

        MongoTemplate template =
                new MongoTemplate(MongoClients.create(uri), database);

        nodeManager.registerNode(name, uri, template);

        int migrated =
                migrationService.migrateAfterNodeAddition(name);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Node " + name + " added successfully");
        response.put("recordsMigrated", migrated);

        return response;
    }

    /**
     * Removes an existing MongoDB node from the ring,
     * migrates its records, and then removes it from the registry.
     */
    @DeleteMapping("/remove/{nodeName}")
    public Map<String, Object> removeNode(
            @PathVariable String nodeName) {

        // Remove from hash ring
        nodeManager.removeFromRing(nodeName);

        // Redistribute its records
        int migrated =
                migrationService.migrateBeforeNodeRemoval(nodeName);

        // Remove metadata
        nodeManager.removeFromRegistry(nodeName);

        Map<String, Object> response = new HashMap<>();
        response.put("message",
                "Node " + nodeName + " removed successfully");
        response.put("recordsMigrated", migrated);

        return response;
    }
}
