package com.example.consistent_hash.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.example.consistent_hash.NodeManager;

import com.mongodb.client.MongoClients;

@Configuration
public class MongoNodeConfig {

    @Bean
    CommandLineRunner initializeNodes(NodeManager nodeManager) {

        return args -> {

            MongoTemplate node1 = new MongoTemplate(
                    MongoClients.create("mongodb://localhost:27018"),
                    "studentdb_node1");

            MongoTemplate node2 = new MongoTemplate(
                    MongoClients.create("mongodb://localhost:27019"),
                    "studentdb_node2");

            MongoTemplate node3 = new MongoTemplate(
                    MongoClients.create("mongodb://localhost:27020"),
                    "studentdb_node3");

            nodeManager.registerNode(
                    "node1",
                    "mongodb://localhost:27018",
                    node1);

            nodeManager.registerNode(
                    "node2",
                    "mongodb://localhost:27019",
                    node2);

            nodeManager.registerNode(
                    "node3",
                    "mongodb://localhost:27020",
                    node3);

            System.out.println("====================================");
            System.out.println("MongoDB Nodes Registered Successfully");
            System.out.println("node1 -> localhost:27018");
            System.out.println("node2 -> localhost:27019");
            System.out.println("node3 -> localhost:27020");
            System.out.println("====================================");
        };
    }
}
