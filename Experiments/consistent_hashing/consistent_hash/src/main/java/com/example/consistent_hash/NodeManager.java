package com.example.consistent_hash;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.example.consistent_hash.Hashing.ConsistentHashRing;
import com.example.consistent_hash.Hashing.PhysicalNode;

import org.springframework.beans.factory.annotation.Value;

@Component
public class NodeManager {

    // Stores all registered physical nodes
    private final Map<String, PhysicalNode> physicalNodes =
            new ConcurrentHashMap<>();

    // Consistent Hash Ring
    private final ConsistentHashRing ring;

    /**
     * Initializes the hash ring with the configured number
     * of virtual nodes.
     */
    public NodeManager(
            @Value("${hashring.virtual-nodes:100}")
            int virtualNodeCount) {

        this.ring = new ConsistentHashRing(virtualNodeCount);
    }

    /**
     * Registers a new physical node and adds it to the hash ring.
     */
    public void registerNode(String name,
                             String uri,
                             MongoTemplate template) {

        PhysicalNode node =
                new PhysicalNode(name, uri, template);

        physicalNodes.put(name, node);

        ring.addNode(name);
    }

    /**
     * Removes a node only from the hash ring.
     * The metadata is retained temporarily so that
     * migration can still access its MongoTemplate.
     */
    public void removeFromRing(String name) {
        ring.removeNode(name);
    }

    /**
     * Removes a node completely from the registry
     * after migration is finished.
     */
    public void removeFromRegistry(String name) {
        physicalNodes.remove(name);
    }

    /**
     * Returns a specific physical node.
     */
    public PhysicalNode getNode(String name) {
        return physicalNodes.get(name);
    }

    /**
     * Returns all registered physical nodes.
     */
    public Map<String, PhysicalNode> getAllNodes() {
        return physicalNodes;
    }

    /**
     * Returns the Consistent Hash Ring.
     */
    public ConsistentHashRing getRing() {
        return ring;
    }
}
